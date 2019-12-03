
package client;

import java.util.Random;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import jig.ResourceManager;
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
	private Image image;
	private boolean added = false;
	
	private Random rand;
	
	public int count; //the number of this item in the inventory
	private int requiredLevel;
	private int weight;
	
	public boolean isEquipped = false; //true if the item has been equipped
	//private String requiredClasses = "";
	private String[] requiredClasses = {"", "", "", ""};
	
	public Item(Vector wc, boolean locked, int id, int oid) throws SlickException{
		super(wc, locked); //superconstructor
		//set random properties
		rand = new Random();
		rand.setSeed(System.nanoTime());
		
		this.id = id;
		this.oid = oid;
		
		count = 1;
		
		//first get the item type
		
		//currently developed item types, for debugging purposes only
		//String[] currentTypes = {"Potion", "Sword", "Armor", "Arrow"};
		
		//rarity: armor, sword/other weapons, potion, arrow
		//armor: 20%, sword: 30%, potion: 40%, arrow: 50%
		int r = rand.nextInt(100);
		if( r < 20 ){
			type = "Armor";
			requiredClasses[0] = "knight";
			requiredClasses[1] = "tank";
		}else if( r < 30 ){
			type = "Sword";
			requiredClasses[0] = "knight";
		}else if( r < 40 ){
			type = "Potion";
			requiredClasses[0] = "knight";
			requiredClasses[1] = "tank";
			requiredClasses[2] = "mage";
			requiredClasses[3] = "archer";
		}else{
			type = "Arrow";
			requiredClasses[0] = "archer";
		}
		
		
		
		
		//choose materials from the appropriate list
		
		r = rand.nextInt(100);
		
		if( type.equals("Sword") ){
			if( r < 20 ){
				material = "Gold";
			}else if( r < 40 ){
				material = "Iron";
			}else{
				material = "Wooden";
			}
		}else if( type.equals("Armor") ){
			if( r < 30 ){
				material = "Gold";
			}else{
				material = "Iron";
			}
		}else if( type.equals("Staff") ){
			this.material = Main.StaffMaterials[ rand.nextInt(Main.StaffMaterials.length) ];
		}else if( type.equals("Glove") ){
			if( r < 20 ){
				material = "Gold";
			}else if( r < 50 ){
				material = "Iron";
			}else{
				material = "Leather";
			}
		}else{
			this.material = "";
		}
		
		//choose effects from the appropriate list
		//chance of effect: 50%
		r = rand.nextInt(100);
		if ( r < 50 ){
			if( type.equals("Sword") ){
				this.effect = Main.SwordEffects[ rand.nextInt(Main.SwordEffects.length) ];
			}else if( type.equals("Armor") ){
				this.effect = Main.ArmorEffects[ rand.nextInt(Main.ArmorEffects.length) ];
			}else if( type.equals("Glove") ){
				this.effect = Main.GloveEffects[ rand.nextInt(Main.GloveEffects.length) ];
			}else if( type.equals("Arrow") ){
				this.effect = Main.ArrowEffects[ rand.nextInt(Main.ArrowEffects.length) ];
			}else{
				this.effect = "";
			}
		//except potions and staffs, which will always have an effect
		}else if( type.equals("Potion") ){
				this.effect = Main.PotionEffects[ rand.nextInt(Main.PotionEffects.length) ];
		}else if( type.equals("Staff") ){
			this.effect = Main.StaffEffects[ rand.nextInt(Main.StaffEffects.length) ];
		}else{
			effect = "";
		}
		
		//items have a 50% chance to be cursed (for now?)
		if( rand.nextInt(100) <= 500 ){ //TODO
			cursed = true;
		}else{
			cursed = false;
		}
		
		//all items start unidentified
		identified = false;
		

		//get an image based on item type
		if( type.equals("Potion") ){
			r = rand.nextInt(5);
			switch( r ){
			case 0:
				this.image = ResourceManager.getImage(Main.POTION_BLUE);
				material = "Blue";
				break;
			case 1:
				this.image = ResourceManager.getImage(Main.POTION_ORANGE);
				material = "Orange";
				break;
			case 2:
				this.image = ResourceManager.getImage(Main.POTION_PINK);
				material = "Pink";
				break;
			case 3:
				this.image = ResourceManager.getImage(Main.POTION_RED);
				material = "Red";
				break;
			case 4:
				this.image = ResourceManager.getImage(Main.POTION_YELLOW);
				material = "Yellow";
				break;
			}
		}else if( type.equals("Sword") ){
			//wood, iron, gold
			if( material.equals("Wooden") ){
				image = ResourceManager.getImage(Main.SWORD_WOOD);
			}else if( material.equals("Iron") ){
				image = ResourceManager.getImage(Main.SWORD_IRON);
			}else if( material.equals("Gold") ){
				image = ResourceManager.getImage(Main.SWORD_GOLD);
			}else{
				throw new SlickException("Error: invalid sword material'"+material+"'");
			}
		}else if( type.equals("Armor") ){
			//iron, gold
			if( material.equals("Iron") ){
				image = ResourceManager.getImage(Main.ARMOR_IRON);
			}else if( material.equals("Gold") ){
				image = ResourceManager.getImage(Main.ARMOR_GOLD);
			}else{
				throw new SlickException("Error: invalid armor material '"+material+"'");
			}
		}else if( type.equals("Arrow") ){
			if( effect.equals("Flame") ){
				image = ResourceManager.getImage(Main.ARROW_FLAME);
			}else if( effect.equals("Poison") ){
				image = ResourceManager.getImage(Main.ARROW_POISON);
			}else if( effect.equals("Ice") ){
				image = ResourceManager.getImage(Main.ARROW_ICE);
			}else if( effect.equals("") ){
				image = ResourceManager.getImage(Main.ARROW_NORMAL);
			}else{
				throw new SlickException("Invalid arrow effect.");
			}
		}
		
		updateWeight();
	}
	
	public Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified, Image image, int count){
		super(wc, locked);
		//create item with given properties
		this.id = id;
		this.oid = oid;
		this.effect = effect;
		this.type = type;
		this.material = material;
		this.cursed = cursed;
		this.identified = identified;
		this.image = image;
		this.count = count;
		
		updateWeight();
	}
	
	
	public boolean equals(Item b){
		if( getMaterial().equals(b.getMaterial()) && getType().equals(b.getType()) && getEffect().equals(b.getEffect()) ){
			return true;
		}
		return false;
	}
	
	public String toString(){
		String tmp = "";
		
		if( isIdentified() ){
			if( getMaterial() != "" ){
				tmp = tmp + getMaterial() + " ";
			}
			tmp = tmp + getType();
			if( getEffect() != "" ){
				tmp = tmp + " of " + getEffect();
			}
		}else{
			if( getMaterial() != "" ){
				tmp = tmp + getMaterial() + " ";
			}
			tmp = tmp + getType();
		}
		return tmp;
	}
	
	//getter functions
	public int getID(){
		return id;
	}
	public int getOID(){
		return oid;
	}
	public String getEffect(){
		return effect;
	}
	public String getType(){
		return type;
	}
	public String getMaterial(){
		return material;
	}
	public boolean isCursed(){
		return cursed;
	}
	public boolean isIdentified(){
		return identified;
	}
	public Image getImage(){
		return image;
	}
	public boolean isLocked(){
		return super.isLocked();
	}
	public String[] getRequiredClasses(){
		return requiredClasses;
	}
	public int getRequiredLevel(){
		return requiredLevel;
	}
	public int getWeight(){
		return weight;
	}
	
	//setter functions
	public void removeCurse(){
		cursed = false;
	}
	public void setOID( int oid ){
		this.oid = oid;
	}
	public void identify(){
		this.identified = true;
	}
	public void setEffect( String effect ){
		this.effect = effect;
	}
	public void lock(){
		super.lock();
	}
	public void unlock(){
		super.unlock();
	}
	public void setRequiredLevel(int level){
		requiredLevel = level;
	}
	public void updateWeight(){
		//set weight based on type and material
		if( type.equals("Armor") ){
			if( material.equals("Leather") ){
				weight = 40;
			}else if( material.equals("Iron") ){
				weight = 50;
			}else if( material.equals("Gold") ){
				weight = 60;
			}
		}else if( type.equals("Sword") ){
			if( material.equals("Wooden") ){
				weight = 10; 
			}else if( material.equals("Iron") ){
				weight = 30;
			}else if( material.equals("Gold") ){
				weight = 40;
			}
		}else if( type.equals("Arrow") ){
			weight = 2*count;
		}else if( type.equals("Potion") ){
			weight = 15*count;
		}
	}
	public void setImage(Image img){
		removeImage(this.image);
		addImage(img);
	}
	
	//render function
	public void render(Graphics g){
		if( image != null && !added ){
			addImage(image);
			added = true;
		}
		super.render(g);
	}
}
