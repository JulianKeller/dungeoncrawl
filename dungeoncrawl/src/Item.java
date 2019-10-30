import jig.Vector;

public class Item extends StationaryObject{
	
	//instance variables that correspond to columns in the database
	private int id;				//ID of this item
	private int oid;			//ID of this item's owner (0 = belongs to world, -1 = no owner)
	private String effect;		//effect, type, and material form the item's name
	private String type;		//  e.g. Leather Armor of Stench
	private String material;	//  the player class will handle the actual effects of items
	private boolean cursed;		//whether the item is cursed
	private boolean identified; //whether the player knows the item's properties
	
	
	public Item(Vector wc, boolean locked){
		super(wc, locked); //superconstructor
		//set random properties
	}
	
	public Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified){
		super(wc, locked);
		//create item with given properties
	}

}
