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
    private Item [] equipped;
    private Vector position;

    /**
     * Create a new Entity (x,y)
     * @param wx starting world x coordinate
     * @param wy starting world y coordinate
     */
    public MovingEntity(final float wx, final float wy, int pid) {
        super(wx, wy);
        hitPoints = 0;
        armorPoints = 0;
        worldCoordinates = new Vector(wx, wy);
        speed = 0;
        mana = 0;
        this.pid = pid;
        inventory = new ArrayList<Item>(10);
        equipped = new Item[4];
        Arrays.fill(equipped, null);
        position = new Vector(0,0);

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
    /**
     * Retrieve current hit points.
     * @return current hit points
     */

    public void addItem(Item i){
        inventory.add(i);
    }

    /**
     * Removes an item from the client.MovingEntity's Inventory
     * @param i_id id of the item to be removed.
     */
    public Item discardItem(int i_id){
    	for( int i = 0; i < inventory.size(); i++ ){
    		if( inventory.get(i).getID() == i_id ){
    			return inventory.remove(i);
    		}
    	}
    	return null;
        //inventory.removeIf(item -> item.getID() == i_id);
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
                    case "client.Arrow":
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
                    case "client.Potion":
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
