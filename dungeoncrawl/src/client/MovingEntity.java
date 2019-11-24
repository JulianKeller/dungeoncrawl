package client;

import jig.Entity;
import jig.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class MovingEntity extends Entity {
    private int hitPoints;
    private int armorPoints;
    private int mana;
    private Vector worldCoordinates;
    private int speed;
    private int pid;
    private ArrayList<Item> inventory;
    private ArrayList<Item> codex; //list of identified items
    private Item [] equipped;
    private Vector position;
    private Vector tileWorldCoordinates;
    private Main dc;
    
    private ArrayList<Effect> activeEffects; //list of things currently affecting the character
    private final int defaultEffectTimer = 5000;
    private class Effect{
    	String name;
    	int timer = defaultEffectTimer;
    	
    	public Effect(String name){
    		this.name = name;
    	}
    }
    /**
     * Create a new Entity (x,y)
     * @param dc
     * @param wy starting world y coordinate
     * @param wx starting world x coordinate
     */
    public MovingEntity(Main dc, final float wy, int pid, final float wx) {
        super(wx, wy);
        this.dc = dc;
        hitPoints = 0;
        armorPoints = 0;
        worldCoordinates = new Vector(wx, wy);

        tileWorldCoordinates = getTileWorldCoordinates();
        speed = 0;
        mana = 0;
        this.pid = pid;
        inventory = new ArrayList<Item>(10);
        equipped = new Item[4];
        Arrays.fill(equipped, null);
        position = new Vector(0,0);
        
        codex = new ArrayList<Item>();
        
        activeEffects = new ArrayList<Effect>();

        Vector wc = getWorldCoordinates();
//        System.out.printf("\nGiven ME World Coordinates %s, %s\n", wx, wy);
//        System.out.printf("Computed ME World Coordinates %s, %s\n\n", wc.getX(), wc.getY());
    }

    /**
     * Create a new Entity (Vector)
     * @param wc Vector for starting world coordinates of the moving entity
     * @param pid entity's id.
     */
    public MovingEntity(Vector wc, int pid) {
        super(wc.getX(), wc.getY());
        hitPoints = 0;
        armorPoints = 0;
        worldCoordinates = wc;
        speed = 0;
        mana = 0;
        this.pid = pid;
        inventory = new ArrayList<Item>(10);
        equipped = new Item[5];
        Arrays.fill(equipped, null);
        position = new Vector(0,0);
    }
    
    public void addEffect(String name){
    	//if the character already has the effect,
    	// reset the timer
    	for( Effect e : activeEffects ){
    		if( e.name.equals(name) ){
    			e.timer = defaultEffectTimer;
    		}
    	}
    	//if not, add the effect
    	activeEffects.add(new Effect(name));
    }
    
    public ArrayList<Effect> getActiveEffects(){
    	return activeEffects;
    }

    public void addItem(Item i){
        inventory.add(i);
        if( i.isIdentified() && i.getType().equals("Potion") ){
        	addToCodex(i);
        }
    }
    
    public void addToCodex(Item i){
    	//check if the item is already in the codex first
    	if( !i.getType().equals("Potion") ){
    		System.out.println("Cannot add non-potions to codex.");
    		return;
    	}
    	for( Item itm : codex ){
    		if( i.getMaterial().equals(itm.getMaterial()) ){
				return;
			}
    	}
    	codex.add(i);
    }

    /**
     * Removes an item from the client.MovingEntity's Inventory
     * @param i_id id of the item to be removed.
     */
    public Item discardItem(int i_id, boolean use){
    	for( int i = 0; i < inventory.size(); i++ ){
    		if( inventory.get(i).getID() == i_id ){
    			if( use ){
    				//if the player is using this item, identify it
    				//  and add to the codex
    				inventory.get(i).identify();
    				if( inventory.get(i).getType().equals("Potion") ){
    					addToCodex(inventory.get(i));
    				}
    			}
    			return inventory.remove(i);
    		}
    	}
    	return null;
        //inventory.removeIf(item -> item.getID() == i_id);
    }
    
    public String equipItem(int index){
    	//equip the item with id in the next available slot
    	//return a status message
    	for( int i = 0; i < equipped.length; i++ ){
    		if( equipped[i] == null ){
    			//equip item here, remove it from the main inventory
    			equipped[i] = inventory.get(index);
    			for( int x = 0; x < equipped.length; x++ ){
    				if( equipped[x] == null ){
    					System.out.print("null ");
    				}else{
    					System.out.print(equipped[x].getType() + " ");
    				}
    			}
    			inventory.remove(index);
    			return null;
    		}
    		System.out.println("Slot "+i+" full.");
    	}

    	return "Your hands are full.";   	
    }

    /**
     * Equips an item from the client.MovingEntity's inventory
     * @param i_id id of the item to be equipped.
     * @param slot slot to be placed in inventory (0 and 1 for right and left hand,
     *             gloves, armor, and potions will be automatically
     *             placed in the appropriate slots).
     */
    public void equipItem(int i_id, int slot){
        for(Iterator<Item> i = inventory.iterator(); i.hasNext();){
            Item item = i.next();
            if(item.getID() == i_id){
                switch(item.getType()){
                    case "Sword":
                    case "Shield":
                    case "Staff":
                    case "Arrow":
                        if(slot > 1 || slot < 0){
                            System.out.println("Invalid slot.");
                            break;
                        }
                        if(equipped[slot] != null){
                            Item temp = equipped[slot];
                            equipped[slot] = item;
                            i.remove();
                            inventory.add(temp);
                        }
                        break;
                    case "Armor":
                        if(equipped[2] != null){
                            Item temp = equipped[2];
                            equipped[2] = item;
                            i.remove();
                            inventory.add(temp);
                        }
                        break;
                    case "Glove":
                        if(equipped[3] != null){
                            Item temp = equipped[2];
                            equipped[3] = item;
                            i.remove();
                            inventory.add(temp);
                        }
                        break;
                    case "Potion":
                        if(equipped[4] != null){
                            Item temp = equipped[2];
                            equipped[4] = item;
                            i.remove();
                            inventory.add(temp);
                        }
                        break;
                    default:
                        System.out.println("Couldn't equip.");
                        break;
                }
            }
            for( Item itm : equipped ){
            	System.out.print(itm.getType() + " ");
            }
        }
    }

    /**
     * Unequips the item from client.MovingEntity's equipped list and places it in inventory.
     * @param slot the equipped slot number to remove.
     */
    public void unequipItem(int slot){
        if(equipped[slot] != null) {
            inventory.add(equipped[slot]);
            equipped[slot] = null;
        }
    }

    public void setHitPoints(int hp){
        hitPoints = hp;
    }
    public int getHitPoints(){
        return hitPoints;
    }

    public void setArmorPoints(int ap){
        armorPoints = ap;
    }
    /**
     * Retrieve current armor points
     * @return current armor points
     */
    public int getArmorPoints(){
        return armorPoints;
    }
    public void setMana(int m){
        mana = m;
    }
    public int getMana(){
        return mana;
    }

    public void setSpeed(int sp){
        if (sp <= 0) {
            return;
        }
        speed = sp;
    }
    /**
     * Retreive client.MovingEntity's speed
     * @return speed
     */
    public int getSpeed(){
        return speed;
    }
    
    public int getPid(){
        return pid;
    }
    
    public ArrayList<Item> getInventory(){
    	return inventory;
    }
    public ArrayList<Item> getCodex(){
    	return codex;
    }
    public Item[] getEquipped(){
    	return equipped;
    }

    public void setPosition(Vector p){
        position = p;
    }

    public Vector getPosition(){
        return position;
    }
    /**
     * Sets the moving entity's worldcoordinates
     * @param wc Vector to set entity's world coordinates.
     */
    public void setWorldCoordinates(Vector wc){
        worldCoordinates = wc;
    }
    public void setWorldCoordinates(float x, float y){
        setWorldCoordinates(new Vector(x, y));
    }


    /**
     * Returns the client.MovingEntity's current world coordinates.
     * @return Vector world coordinates
     */
    public Vector getWorldCoordinates(){
        return worldCoordinates;
    }


//    /**
//     * Set the entities tile coordinates for the world
//     * @param tileWC
//     */
//    public void setTileWorldCoordinates(Vector tileWC) {
//        tileWorldCoordinates = tileWC;
//    }

    /**
     * Get the entities world coordinates in tiles
     * @return
     */
    public Vector getTileWorldCoordinates() {
        float x = Math.round((worldCoordinates.getX() + dc.offset)/dc.tilesize);
        float y = Math.round((worldCoordinates.getY() + dc.tilesize + dc.doubleOffset)/dc.tilesize);
        return new Vector(x, y);
    }


    /**
     * Adds hit points by specified amount
     * @param amt amount to add hit points by
     */
    public void addHitPoints(int amt){
        hitPoints += amt;
    }

    /**
     * Subtracts hit points by specified amount
     * @param amt amount to subtract hit points by
     */
    public void subHitPoints(int amt){
        hitPoints-= amt;
    }
    /**
     * Subtracts armor points by specified amount
     * @param amt amount to subtract armor points by
     */
    public void subArmorPoints(int amt){
        armorPoints-= amt;
    }

    /**
     * Adds armor points by specified amount
     * @param amt amount to add armor points by
     */
    public void addArmorPoints(int amt){
        armorPoints += amt;
    }

    /**
     * Adds to Moving entity's speed.
     * @param amt amount to add to speed.
     */
    public void addToSpeed(int amt){
        speed += amt;
    }

    /**
     *
     * @param amt subtract amount to speed.
     */
    public void subSpeed(int amt){
        speed -= amt;
    }

//    public void update(int delta){
//        translate(position.scale(delta));
//    }

}
