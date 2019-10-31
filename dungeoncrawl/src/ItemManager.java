import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import com.mysql.cj.jdbc.MysqlDataSource;

import jig.Vector;
public class ItemManager {
	/**
	 * This class will handle all database operations to do with items.
	 * It will also deal with rendering item sprites to the screen
	 *   in the case of items owned by the world.
	 *   
	 * Required operations:
	 * give - take an item off of the world and give it to a player
	 * take - take an item from a player and destroy it
	 * stow - remove item sprites from parts of the world which are not visible
	 * restore - put stowed items back
	 * plant - create new items and corresponding database entries
	 * 
	 * This class will not handle the effects of using items as those should be handled
	 *   by the players who own them.
	 *   
	 * MySQL connection code based on https://stackoverflow.com/questions/2839321/connect-java-to-a-mysql-database
	 */
	
	//reference to game
	Main game;
	
	//random
	Random rand;
	
	//MySQL instance variables
	MysqlDataSource datasource;
	Connection conn;
	Statement s;
	ResultSet rs;
	
	String dbName = "dungeoncrawl";
	
	private void dbConnect() throws SQLException{
		//set up data source
		if( datasource == null ){
			datasource = new MysqlDataSource();
			datasource.setUser("root");
			datasource.setPassword("password");
			datasource.setServerName("localhost");
			datasource.setPort(3306);
		}
		
		//create connection object
		conn = datasource.getConnection();
		
		//create a statement to execute queries with
		s = conn.createStatement();
		
		String q = "use "+dbName;
		s.executeQuery(q);
	}
	
	private void dbClose() throws SQLException{
		rs.close();
		s.close();
		conn.close();
	}
	
	private void dbReset() throws SQLException{
		if( datasource == null ){
			System.out.println("Cannot reset null database.");
			return;
		}
		
		String q = "drop database "+dbName;
		s.executeUpdate(q);
		q = "create database "+dbName;
		s.executeUpdate(q);
		q = "use "+dbName;
		s.executeUpdate(q);

		try {
			executeSQLFile("database/createTables.sql", s);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//insert world as player
		q = "insert into player values(0, '', '', 0)";
		s.executeUpdate(q);
	}
	
	public ItemManager(Main game) throws SQLException{
		dbConnect(); //connect to data source
		dbReset();
		this.game = game;
		rand = new Random();
		
		//testing
		plant(5);
	}
	
	public void give(int itemID, int playerID) throws SQLException{
		//give an item to a player
		String q = "update item set oid = "+playerID+"where iid = "+itemID;
		rs = s.executeQuery(q);
	}
	
	public void take(int itemID) throws SQLException{
		//set the item's owner to -1, destroying it in effect
		String q = "update item set oid = -1 where iid = "+itemID;
		rs = s.executeQuery(q);
	}
	
	private int currentItemID = 0;
	
	public void plant(int numItems) throws SQLException{
		//add a new item to the map at random position
		int maxx = game.ScreenWidth/game.tileW;
		int maxy = game.ScreenHeight/game.tileH;
		
		rand.setSeed(System.nanoTime());
		
		
		
		while( numItems > 0 ){
			//create a new item object with random properties
			Vector vec = new Vector(rand.nextInt(maxx), rand.nextInt(maxy));
			Item item = new Item(vec, false, currentItemID, 0);
			currentItemID++;
			
			//add its values to the database
			String q = "insert into item values("
					+currentItemID+","+item.getOID()+",'"+item.getEffect()+"','"+item.getType()+"','"+item.getMaterial()
					+"', "+(int) vec.getX()+", "+(int) vec.getY()+")";
			s.executeUpdate(q);
			
			numItems--;
		}
	}
	
	private void executeSQLFile(String filepath, Statement s) throws IOException, FileNotFoundException{
		FileInputStream in = new FileInputStream(filepath);
		
		//build a string until a semicolon is encountered
		String q = "";
		int ch = -1;
		while(true){
			try {
				ch = in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}  
			if( ch == -1 ){
				//end of file
				break;
			}else if( ch == 59 ){
				//semicolon; end of statement
				q = q + (char) ch;
				try {
					s.execute(q);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				q = "";
			}else if( ch != 10 ){
				//if not a newline
				q = q + (char) ch;
			}
		}
		
		//close input stream
		in.close();
		
	}
	
	public void restore(int itemID) throws SQLException{
		//add an item to the map with values from the database
		String q = "select * from item where iid = "+itemID;
		rs = s.executeQuery(q);
		
		int id = 0;
		int oid = 0;
		String effect = "";
		String type = "";
		String material = "";
		boolean cursed = false;
		boolean identified = false;
		Vector wc = new Vector(0, 0);
		
		
		int column = 0;
		while( rs.next() ){
			switch( column ){
			case 0:
				id = rs.getInt(1);
				break;
			case 1:
				oid = rs.getInt(1);
				break;
			case 2:
				effect = rs.getString(1);
				break;
			case 3:
				type = rs.getString(1);
				break;
			case 4:
				material = rs.getString(1);
				break;
			case 5:
				cursed = rs.getBoolean(1);
				break;
			case 6:
				identified = rs.getBoolean(1);
				break;
			case  7:
				wc.setX(rs.getInt(1));
				break;
			case 8:
				wc.setY(rs.getInt(1));
				break;
			}
			column++;
		}
		
		Item item = new Item(wc, false, id, oid, effect, type, material, cursed, identified);
	}
}
