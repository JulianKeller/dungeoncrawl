import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.cj.jdbc.MysqlDataSource;
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
	
	//MySQL instance variables
	MysqlDataSource datasource;
	Connection conn;
	Statement s;
	ResultSet rs;
	
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
	}
	
	private void dbClose() throws SQLException{
		rs.close();
		s.close();
		conn.close();
	}
	
	public ItemManager() throws SQLException{
		dbConnect(); //connect to data source
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
}
