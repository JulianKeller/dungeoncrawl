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
				for( Character c : game.characters ){
					if( c.getPid() == playerID ){
						c.addItem(i);
						worldItems.remove(i);
						return;
					}
				}
			}
		}

	}
	
	public void take(int itemID, int playerID, Vector wc){
		//take an item from the player and place it at the given coordinates
		// unless the coordinate is null
		for( Character c : game.characters ){
			if( c.getPid() == playerID ){
				Item i = c.discardItem(itemID);
				
				
				
				//if the wc is not null, place the item on the world
				if( wc != null ){
					i.setWorldCoordinates(wc);
					//set the owner to the world
					i.setOID(0);
					worldItems.add(i);
				}
			}
		}
	}
	
	private int currentItemID = 0;
	
	public void plant(int numItems, int[][] map){
		int maxcol = game.ScreenWidth/game.tilesize;
		int maxrow = game.ScreenHeight/game.tilesize;
		
		rand.setSeed(System.nanoTime());
		
		//int[][] potionAt = new int[game.map.length][game.map.length];
		
		
		while( numItems > 0 ){
			
			int col = rand.nextInt(maxcol);
			int row = rand.nextInt(maxrow);
			//Vector wc = new Vector( rand.nextInt(maxx), rand.nextInt(maxy) );
			while( map[row][col] == 1 ){
				col = rand.nextInt(maxcol);
				row = rand.nextInt(maxrow);
			}
			Vector wc = new Vector( row, col );
			
			//create a random item at the given position
			worldItems.add(new Item(wc, false, currentItemID, 0));
			
			
			currentItemID++;
			
			numItems--;
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
	
	/**
	 * Returns the item on the given tile or null if tile is empty.
	 * Works with world/tile coordinates, not screen coordinates. 
	 * @param tile
	 * @return null or the item on the tile
	 */
	public Item getItemAt(Vector tile){
		for( Item itm : worldItems ){
			if( itm.getWorldCoordinates().getX() == tile.getX() ){
				if( itm.getWorldCoordinates().getY() == tile.getY() ){
					return itm;
				}
			}
		}
		return null;
	}
	
}
