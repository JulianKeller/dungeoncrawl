package client;

import java.util.ArrayList;
import java.util.Random;

import org.newdawn.slick.SlickException;

import jig.Vector;
public class ItemManager {  

	private ArrayList<Item> worldItems;
	private Main game;
	private Random rand;
	
	private final int uniquePotions = 5; //the number of different potion images
	
	//0: blue, 1: orange, 2: pink, 3: red, 4: yellow
	private String[] identifiedPotionEffects;
	
	public ItemManager(Main game){
		worldItems = new ArrayList<Item>();
		this.game = game;
		rand = new Random();
		
		identifiedPotionEffects = new String[uniquePotions];
		//populate this list ahead of time
		for( int i = 0; i < uniquePotions; i++ ){
			String effect = Main.PotionEffects[ rand.nextInt(Main.PotionEffects.length)];
			for( int j = 0; j < identifiedPotionEffects.length; j++ ){
				if( identifiedPotionEffects[j] != null && identifiedPotionEffects[j].equals(effect) ){
					i--;
					break;
				}
				if( j == identifiedPotionEffects.length-1 ){
					identifiedPotionEffects[i] = effect;
				}
			}
			
		}
	}
	
	public void give(int itemID, int playerID){
		//give an item with given id to the player with given id
		for( Item i : worldItems ){
			if( i.getID() == itemID ){
				if(i.isLocked() ){
					return;
				}
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
	
	public void give( Item i, Character c ){
		c.addItem(i);
	}
	
	public void take(int itemID, int playerID, Vector wc, boolean use){
		//take an item from the player and place it at the given coordinates
		// unless the coordinate is null
		for( Character c : game.characters ){
			if( c.getPid() == playerID ){
				Item i = c.discardItem(itemID, use);
				
				
				
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
	
	public void take(Item i, Character c, Vector wc, boolean use ){
		if( wc != null ){
			i.setWorldCoordinates(wc);
			i.setOID(0);
			worldItems.add(i);
		}
		
		c.discardItem(i.getID(), use);
	}
	
	private int currentItemID = 0;
	
	public int getCurrentItemID(){
		return currentItemID;
	}
	public void updateCurrentItemID(){
		currentItemID++;
	}
	
	public void plant(int numItems, int[][] map) throws SlickException{
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
			Item i = new Item(wc, false, currentItemID, 0);
			
			//check if the item is a potion and set its effect
			//0: blue, 1: orange, 2: pink, 3: red, 4: yellow
			if( i.getType().equals("Potion") ){
				if( i.getMaterial().equals("Blue") ){
					//use the stored effect
					i.setEffect(identifiedPotionEffects[0]);
				}else if( i.getMaterial().equals("Orange")){
					//use the stored effect
					i.setEffect(identifiedPotionEffects[1]);
				}else if( i.getMaterial().equals("Pink")){
					//use the stored effect
					i.setEffect(identifiedPotionEffects[2]);
				}else if( i.getMaterial().equals("Red")){
					//use the stored effect
					i.setEffect(identifiedPotionEffects[3]);
				}else if( i.getMaterial().equals("Yellow") ){
					//use the stored effect
					i.setEffect(identifiedPotionEffects[4]);
				}else{
					//this would suggest that the list of potion colors in this class
					//  is incomplete
					throw new SlickException("Error: invalid potion color "+i.getMaterial());
				}
			}

			worldItems.add(i);

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
	
	public Item getWorldItemByID(int id){
		for( Item i : worldItems ){
			if( i.getID() == id ){
				return i;
			}
		}
		return null;
	}
	
	public ArrayList<Item> getWorldItems(){
		return worldItems;
	}
	
}
