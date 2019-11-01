import java.util.ArrayList;
import java.util.Random;

import jig.Vector;
public class ItemManager {  

	private ArrayList<Item> worldItems;
	private Main game;
	private Random rand;
	
	public ItemManager(Main game){
		worldItems = new ArrayList<Item>();
		this.game = game;
		rand = new Random();
	}
	
	public void give(int itemID, int playerID){
		//give an item with given id to the player with given id
		for( Item i : worldItems ){
			if( i.getID() == itemID ){
				//add to the player's inventory
			}
		}

	}
	
	public void take(int itemID, int playerID, Vector wc){
		
	}
	
	private int currentItemID = 0;
	
	public void plant(int numItems){
		int maxx = game.ScreenWidth/game.tileW;
		int maxy = game.ScreenHeight/game.tileH;
		
		rand.setSeed(System.nanoTime());
		
		while( numItems > 0 ){
			Vector wc = new Vector( rand.nextInt(maxx), rand.nextInt(maxy) );
			
			//create a random item at the given position
			worldItems.add(new Item(wc, false, currentItemID, 0));
			
			currentItemID++;
		}

	}
	
	public ArrayList<Item> itemsInRegion(Vector min, Vector max){
		//return items in the specified region
		ArrayList<Item> items = new ArrayList<Item>();
		for( Item i : worldItems ){
			Vector wc = i.getWorldCoordinates();
			if( min.getX() <= wc.getX() && wc.getX() <= max.getX()){
				if( min.getY() <= wc.getY() && wc.getY() <= max.getY() ){
					items.add(i);
				}
			}
		}
		return items;
	}
	
}
