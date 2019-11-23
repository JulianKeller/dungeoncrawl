package client;

import jig.Entity;
import jig.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.newdawn.slick.SlickException;

public class MovingEntity extends Entity {
    private int hitPoints;
    private final int startingHitPoints;
    private int armorPoints;
    private int mana;
    private int strength; //determines what level of items the player can pick up
    
    //boolean effects for AI
    private boolean invisible = false;
    private boolean stinky = false;
    private boolean thorny = false;
    private boolean frightening = false;
    private boolean reflecting = false;
    
    private Vector worldCoordinates;
    private int speed;
    private int pid;
    private ArrayList<Item> inventory;
    private ArrayList<Item> codex; //list of identified items
    private Item [] equipped;
    private Vector position;
    
    //random number generator
    private Random rand = new Random();
    
   
    
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
     * @param wx starting world x coordinate
     * @param wy starting world y coordinate
     */
    public MovingEntity(final float wx, final float wy, int pid) {
        super(wx, wy);
        hitPoints = 0;
        startingHitPoints = hitPoints;
        armorPoints = 0;
        worldCoordinates = new Vector(wx, wy);
        speed = 0;
        mana = 0;
        strength = 1;
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
        startingHitPoints = hitPoints;
        armorPoints = 0;
        worldCoordinates = wc;
        speed = 0;
        mana = 0;
        strength = 1;
        this.pid = pid;
        inventory = new ArrayList<Item>(10);
        equipped = new Item[5];
        Arrays.fill(equipped, null);
        position = new Vector(0,0);
    }
    
    //get boolean effects for AI
    public boolean isInvisible(){
    	return invisible;
    }
    public boolean isStinky(){
    	return stinky;
    }
    public boolean isThorny(){
    	return thorny;
    }
    public boolean isFrightening(){
    	return frightening;
    }
    public boolean isReflecting(){
    	return reflecting;
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
    
    public void updateEffectTimers(int delta){
    	//reduce each active effect timer by delta
    	for( Effect e : activeEffects ){
    		e.timer -= delta;
    	}
    	//remove any expired effects
    	activeEffects.removeIf(b -> b.timer <= 0);
    }
    
    /*
     * Healing:
	Restore 25% of lost HP
	Staff: on hit
	Potion: on hit/drink
Strength:
	Increase character strength by 1 point.
Flame (Flaming):
	Decrease health by 10% every update loop.
Mana:
	Increase maximum mana by 15%
Invisibility:
	Enemies can no longer see character.
Poisoned:
	Decrease health by 5% every update loop.
Ice:
	Character is frozen in place for a time.
Lightning:
	Character has a 30% chance of being struck by lightning after a hit.
Stench:
	Enemies have a 30% chance to run away from the character.
Iron Skin:
	Defense attribute is doubled.
Thorns:
	Attacking enemies will take 50% of the damage they deal.
Swiftness:
	Character speed is doubled.
Fright:
	Enemies have a 50% chance to run away.
Might:
	Double sword damage.
Regeneration:
	50% chance to Restore 3% of lost HP each loop.
Reflection:
	Attacking enemies will take 50% of the damage they deal.
     */
    
    public void implementEffects() throws SlickException{
    	for( Effect e : activeEffects ){
    		if( e.name.equals("Healing") ){
    			//find the difference between this entity's current HP
    			//  and its starting HP, then add 25% of that to the
    			//  current value
    			int diff = startingHitPoints - hitPoints;
    			hitPoints += (diff*0.25);
    			
    		}else if( e.name.equals("Strength") ){
    			//increment the player's strength variable
    			strength++;
    			//this should only happen once
    			
    		}else if( e.name.equals("Flame") ){
    			//decrease health by 10
    			hitPoints -= 10;
    			
    		}else if( e.name.equals("Mana") ){
    			//add 15% to the current maximum mana
    			mana += (mana*0.15);
    			
    		}else if( e.name.equals("Invisibility") ){
    			//little too complicated for this function,
    			//  just set a boolean value
    			invisible = true;
    		}else if( e.name.equals("Poisoned") ){
    			//decrease health by 5
    			hitPoints -= 5;
    			
    		}else if( e.name.equals("Ice") ){
    			//set movement speed to zero
    			speed = 0;
    		}else if( e.name.equals("Lightning") ){
    			//roll 30% change to take 20 damage
    			rand.setSeed(System.nanoTime());
    			int r = rand.nextInt(100);
    			if( r < 30 ){
    				hitPoints -= 20;
    			}
    			
    		}else if( e.name.equals("Stench") ){
    			//another AI problem, set a boolean
    			stinky = true;
    			
    		}else if( e.name.equals("Iron Skin") ){
    			//double the armor points variable
    			armorPoints *= 2;
    			
    		}else if( e.name.equals("Thorns") ){
    			//AI problem
    			thorny = true;
    			
    		}else if( e.name.equals("Swiftness") ){
    			//double movement speed
    			speed *= 2;
    			
    		}else if( e.name.equals("Fright") ){
    			//AI problem, see stench
    			frightening = true;
    			
    		}else if( e.name.equals("Might") ){
    			//double player's attack damage
    			//TODO: add attack system
    			
    		}else if( e.name.equals("Regeneration") ){
    			//roll 50% chance to restore 3 hp
    			rand.setSeed(System.nanoTime());
    			int r = rand.nextInt(100);
    			if( r < 50 ){
    				hitPoints += 3;
    			}
    			
    		}else if( e.name.equals("Reflection") ){
    			//AI problem, see thorns
    			reflecting = true;
    			
    		}else{
    			throw new SlickException("Unknown character effect.");
    		}
    		
    		//remove one-time effects
    		activeEffects.removeIf(b -> b.name.equals("Strength") || 
    									b.name.equals("Iron Skin") || 
    									b.name.equals("Healing"));
    	}
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
    public int getStrength(){
    	return strength;
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
