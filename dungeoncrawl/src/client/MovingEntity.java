package client;

import jig.Entity;
import jig.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class MovingEntity extends Entity {
    private float hitPoints;
    private float startingHitPoints = -1;
    private int armorPoints;
    private int initialArmorPoints = -1;
    private float mana;
    private int strength; //determines what level of items the player can pick up
    
    //boolean effects for AI
    private boolean invisible = false;
    private boolean stinky = false;
    private boolean thorny = false;
    private boolean frightening = false;
    private boolean reflecting = false;
    
    private Vector worldCoordinates;
    private int animationSpeed;
    private int initialMovementSpeed;
    private int initialAnimationSpeed = -1;
    private int movementSpeed;
    private int pid;
    private ArrayList<Item> inventory;
    private ArrayList<Item> codex; //list of identified items
    private Item [] equipped;
    private Vector position;
    private Vector tileWorldCoordinates;
    private Main dc;

    
    //random number generator
    private Random rand = new Random();
    
    //provides ability to add messages to the level
    private Level currentLevel;
   
    
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
     * @param wy starting world y coordinate
     * @param wx starting world x coordinate
     */

    public MovingEntity(final float wx, final float wy, int pid, Level level) {
        super(wx, wy);
        
        currentLevel = level;

        hitPoints = 0;
        //startingHitPoints = hitPoints;
        armorPoints = 1;
        worldCoordinates = new Vector(wx, wy);
        animationSpeed = 1;
        initialMovementSpeed = movementSpeed = 1;

        tileWorldCoordinates = getTileWorldCoordinates();

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
    public MovingEntity(Vector wc, int pid, Level level) {
        super(wc.getX(), wc.getY());
        
        
        currentLevel = level;
        
        
        hitPoints = 0;
        //startingHitPoints = hitPoints;
        armorPoints = 0;
        worldCoordinates = wc;
        animationSpeed = 1;
        initialMovementSpeed = movementSpeed = 1;
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
    
    /*
     * Note on effects:
     * -Effects to be removed automatically after a period of time
     *  should be removed with the updateEffectTimers method
     * -Effects to be removed immediately after being applied
     *  should be added to the removeIf statement at the bottom of
     *  the implementEffects method
     * -Effects to be removed in any other situation can be manually
     *  removed with the removeEffect method
     */
    
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
    
    /**
     * Use this function to remove effects caused by
     * an equipped item when said item is unequipped
     */
    public void removeEffect(String name){
    	activeEffects.removeIf(e -> e.name.equals(name));
    	//special exit behavior
    	if( name.equals("Iron Skin") ){
    		armorPoints = initialArmorPoints;
    	}else if( name.equals("Stench") ){
			stinky = false;
		}else if( name.equals("Thorns")){
			thorny = false;
		}else if( name.equals("Fright") ){
			frightening = false;
		}else if( name.equals("Invisibility") ){
			invisible = false;
		}else if( name.equals("Reflection") ){
			reflecting = false;
		}
    }
    
    /**
     * Use this function every update loop for
     * the automatic removal of timed effects
     */
    public void updateEffectTimers(int delta){
    	//reduce each active effect timer by delta
    	//System.out.println("Updating effect timers")
    	ArrayList<String> effectsToAdd = new ArrayList<String>();
    	for( Effect e : activeEffects ){
    		//System.out.println("reducing timer of " + e.name + " by " + delta);
    		e.timer -= delta;
    		if( e.timer <= 0 ){
    			//set special exit properties
    			//  for certain effects
    			if( e.name.equals("Swiftness") || e.name.equals("Ice") ){	
    				//reset to initial movement speed
    				movementSpeed = initialMovementSpeed;
    			}else if( e.name.equals("Iron Skin") ){
    				//iron skin will be active as long as the player is wearing
    				//  armor carrying the effect, so it should be reactivated
    				//  every time its timer runs out (effect should be
    				//    manually removed when the armor is taken off)
    				effectsToAdd.add("Iron Skin");
    			}else if( e.name.equals("Stench") ){
    				effectsToAdd.add("Stench");
    			}else if( e.name.equals("Regeneration")){
    				effectsToAdd.add("Regeneration");
    			}else if( e.name.equals("Thorns")){
    				effectsToAdd.add("Thorns");
    			}else if( e.name.equals("Fright") ){
    				effectsToAdd.add("Fright");
    			}else if( e.name.equals("Might") ){
    				effectsToAdd.add("Might");
    			}else if( e.name.equals("Reflection") ){
    				effectsToAdd.add("Reflection");
    			}
    		}
    	}
    	//remove any expired effects
    	activeEffects.removeIf(b -> b.timer <= 0);
    	
    	//add any new effects
    	for( String s : effectsToAdd ){
    		addEffect(s);
    	}
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
    		//System.out.println(e.name);
    		if( e.name.equals("Healing") ){
    			//find the difference between this entity's current HP
    			//  and its starting HP, then add 25% of that to the
    			//  current value
    			if( startingHitPoints > hitPoints ){
	    			float diff = startingHitPoints - hitPoints;
	    			hitPoints += (diff*0.25);
    			}
    			
    		}else if( e.name.equals("Strength") ){
    			//increment the player's strength variable
    			strength++;
    			//this should only happen once
    			
    		}else if( e.name.equals("Flame") ){
    			//decrease health by 10 every second
    			//	want to lose 10 hp per second
    			//	assume 60 frames per second
    			//	10/60 = amount of hp lost per frame
    			hitPoints -= ( (float) 10/60);
    			
    		}else if( e.name.equals("Mana") ){
    			//add 15% to the current maximum mana
    			mana += (mana*0.15);
    			
    		}else if( e.name.equals("Invisibility") ){
    			//little too complicated for this function,
    			//  just set a boolean value
    			invisible = true;
    		}else if( e.name.equals("Poisoned") ){
    			//decrease health by 5 every second
    			hitPoints -= ( (float) 5/60);
    			
    		}else if( e.name.equals("Ice") ){
    			//set movement speed to zero
    			movementSpeed = 0;
    			//animationSpeed = 0;
    			
    		}else if( e.name.equals("Lightning") ){
    			//roll 30% change to take 20 damage
    			rand.setSeed(System.nanoTime());
    			int r = rand.nextInt(100);
    			if( r < 60 ){
    				hitPoints -= 20;
    				currentLevel.addMessage("Struck by lightning!");
    				System.out.println("Struck by lightning!");
    			}
    			
    		}else if( e.name.equals("Stench") ){
    			//another AI problem, set a boolean
    			stinky = true;
    			
    		}else if( e.name.equals("Iron Skin") ){
    			//double the armor points variable
    			if( armorPoints == initialArmorPoints ){
    				armorPoints *= 2;
    			}
    			
    			
    		}else if( e.name.equals("Thorns") ){
    			//AI problem
    			thorny = true;
    			
    		}else if( e.name.equals("Swiftness") ){
    			//double movement speed
    			//  only if the current speed is equal to the
    			//  initial speed
    			if( movementSpeed == initialMovementSpeed ){
    				doubleMoveSpeed();
    			}
    			
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
    		

    	}
    	
		/*
		 * Some effects should only be applied once.
		 * For example, swiftness will double the speed every loop
		 *   if it remains in the list
		 */ 
		activeEffects.removeIf(b -> b.name.equals("Strength") || 
									b.name.equals("Healing") ||
									b.name.equals("Lightning") ||
									b.name.equals("Mana"));
					
    }
    
    public boolean takeDamage(float amount, String effect ){
    	hitPoints -= amount;
    	if( !effect.equals("") ){
    		addEffect(effect);
    	}
    	
    	//check if this entity is dead
    	if( hitPoints <= 0 ){
    		return true;
    	}else{
    		return false;
    	}
    }

    public void addItem(Item i){
    	boolean add = true;
    	for( Item itm : inventory ){
    		if( itm.equals(i) ){
    			itm.count += i.count;
    			add = false;
    			break;
    		}
    	}
    	if( add ){
    		inventory.add(i);
    	}
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
    			//return inventory.remove(i);
    			if( inventory.get(i).count == 1 ){
    				return inventory.remove(i);
    			}else{
    				inventory.get(i).count--;
    				return inventory.get(i);
    			}
    		}
    	}
    	return null;
        //inventory.removeIf(item -> item.getID() == i_id);
    }
    
    public Item discardItem(Item i, boolean use){
    	if( i.count == 1 ){
    		inventory.remove(i);
    	}else{
    		i.count--;
    	}
    	
    	if( use ){
    		i.identify();
    	}
    	
    	if( i.getType().equals("Potion") ){
    		addToCodex(i);
    	}
    	
    	return i;
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

    public void setHitPoints(float hp){
        hitPoints = hp;
        if( startingHitPoints == -1 ){
        	startingHitPoints = hitPoints;
        }
    }
    public float getHitPoints(){
        return hitPoints;
    }

    public void setArmorPoints(int ap){
        armorPoints = ap;
        if( initialArmorPoints == -1 ){
        	initialArmorPoints = ap;
        }
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
    public float getMana(){
        return mana;
    }
    public int getStrength(){
    	return strength;
    }

    public void setAnimationSpeed(int sp){
        if (sp <= 0) {
            return;
        }
        animationSpeed = sp;
        if( initialAnimationSpeed == -1 ){
        	initialAnimationSpeed = sp;
        }
    }
    
    public void setMovementSpeed(int sp){
        if (sp <= 0) {
            return;
        }
        movementSpeed = sp;
    }
    
    public void doubleMoveSpeed(){
        if (movementSpeed >= 32) {
            return;
        }
        setAnimationSpeed(getAnimationSpeed() / 2);
        movementSpeed *= 2;
    }
    
    public void halfMoveSpeed(){
        if (movementSpeed <= 1) {
            return;
        }
        setAnimationSpeed(getAnimationSpeed() * 2);
        movementSpeed /= 2;
    }
    
    /**
     * Retreive client.MovingEntity's speed
     * @return speed
     */
    public int getAnimationSpeed(){
        return animationSpeed;
    }
    public int getMovementSpeed(){
    	return movementSpeed;
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
        float x = Math.round((worldCoordinates.getX() + currentLevel.offset)/currentLevel.tilesize) - 1;
        float y = Math.round((worldCoordinates.getY() + currentLevel.tilesize + currentLevel.doubleOffset)/currentLevel.tilesize) - 1;
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
        animationSpeed += amt;
    }

    /**
     *
     * @param amt subtract amount to speed.
     */
    public void subSpeed(int amt){
        animationSpeed -= amt;
    }

//    public void update(int delta){
//        translate(position.scale(delta));
//    }

}
