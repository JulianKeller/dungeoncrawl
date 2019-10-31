import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
		System.out.println("Connecting to database...");
		dbConnect(); //connect to data source
		System.out.println("Resetting database...");
		dbReset();
		System.out.println("Done.");
		this.game = game;
		rand = new Random();
		
		//testing
		plant(5);
		
		ArrayList<Item> items = restore();
		
		System.out.println("Searching items on world...");
		for( Item i : items ){
			System.out.println("It is "+i.getMaterial()+" "+i.getType()+" of "+i.getEffect());
		}
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
					+"', "+(int) vec.getX()+", "+(int) vec.getY()+", "+item.isCursed()+", "+item.isIdentified()+")";
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
	
	public ArrayList<Item> restore() throws SQLException{
		//returns a list of items that belong to the world
		//they should be rendered by whatever is calling this function
		
		ArrayList<Item> items = new ArrayList<Item>();
		//get all items owned by the world
		String q = "select * from item where oid = 0"; //could also include screen boundaries
		rs = s.executeQuery(q);
		
		int id = 0;
		int oid = 0;
		String effect = "";
		String type = "";
		String material = "";
		boolean cursed = false;
		boolean identified = false;
		Vector wc = new Vector(0, 0);
		
		
		//ResultSet.next() moves the pointer to the next row
		// or returns false if there are no more rows
		while( rs.next() ){
			for( int column = 1; column <= 9; column++){
				//the column index starts at 1
				switch( column ){
				case 1:
					id = rs.getInt(column);
					break;
				case 2:
					oid = rs.getInt(column);
					break;
				case 3:
					effect = rs.getString(column);
					break;
				case 4:
					type = rs.getString(column);
					break;
				case 5:
					material = rs.getString(column);
					break;
				case 6:
					cursed = rs.getBoolean(column);
					break;
				case 7:
					identified = rs.getBoolean(column);
					break;
				case 8:
					wc.setX(rs.getInt(column));
					break;
				case 9:
					wc.setY(rs.getInt(column));
					break;
				}
				
			}
			items.add(new Item(wc, false, id, oid, effect, type, material, cursed, identified));
		}
		
		return items;
	}
	
	public ResultSet getInventory(int playerID){
		/**
		 * Returns a ResultSet object containing rows from the item table
		 *   owned by the player with given ID.
		 */
		String q = "select * from item where oid = "+playerID;
		try {
			return s.executeQuery(q);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
