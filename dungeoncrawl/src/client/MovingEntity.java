package client;

import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

public class MovingEntity extends Entity {
    private float hitPoints;
    private float startingHitPoints = -1;
    private int armorPoints;
    private int initialArmorPoints = -1;
    private float mana;
    private float maxMana;
    private int strength; //determines what level of items the player can pick up
    private int inventoryWeight = 0; //weight of items factored into movement speed
    private int attackDamage;   // determines the amount of damage AI can deal
    private int attackSpeed;    // determines how fast the ai deal the attackDamage
    //boolean effects for AI
    private boolean invisible = false;
    private boolean stinky = false;
    private boolean thorny = false;
    private boolean frightening = false;
    private boolean reflecting = false;
    private boolean mighty = false;
    
    private Vector worldCoordinates;
    private int animationSpeed;
    private int initialMovementSpeed;
    private int initialAnimationSpeed = -1;
    private int movementSpeed;
    private int pid;
    private ArrayList<Item> inventory;
    private ArrayList<Item> codex; //list of identified items
    private Item [] equipped;
    private ArrayList<String> cursedItemTypes; //list of cursed item types the player is using/wearing
    private Vector position;
    private Vector tileWorldCoordinates;
    private Vector nextTileWorldCoordinates;
    private Main dc;
    


    
    //random number generator
    private Random rand = new Random();
    
    //provides ability to add messages to the level
    private Level currentLevel;
   
    
    private ArrayList<Effect> activeEffects; //list of things currently affecting the character
    private final int defaultEffectTimer = 5000;
    class Effect{
    	String name;
    	int timer = defaultEffectTimer;
    	boolean cursed;
    	
    	public Effect(String name, boolean cursed){
    		this.name = name;
    		this.cursed = cursed;
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
        nextTileWorldCoordinates = tileWorldCoordinates;

        mana = 0;
        maxMana = 0;
        strength = 1;
        this.pid = pid;
        inventory = new ArrayList<Item>(10);
        cursedItemTypes = new ArrayList<String>();
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
        maxMana = 0;
        strength = 1;
        this.pid = pid;
        inventory = new ArrayList<Item>(10);
        cursedItemTypes = new ArrayList<String>();
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
    public boolean isMighty(){
    	return mighty;
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
    
    public void addEffect(String name, boolean cursed){
    	//if the character already has the effect,
    	// reset the timer
    	for( Effect e : activeEffects ){
    		if( e.name.equals(name) ){
    			e.timer = defaultEffectTimer;
    			e.cursed = cursed;
    		}
    	}
    	//if not, add the effect
    	activeEffects.add(new Effect(name, cursed));
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
    	ArrayList<String> cursedEffectsToAdd = new ArrayList<String>();
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
    		addEffect(s, false);
    	}
    	for( String s : cursedEffectsToAdd ){
    		addEffect(s, true);
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
    

    
    public ArrayList<String> implementEffects() throws SlickException{
    	ArrayList<String> returnMessages = new ArrayList<String>();
    	float curseModifier = 0.5f;
    	for( Effect e : activeEffects ){
    		//System.out.println(e.name);
    		if( e.name.equals("Healing") ){
    			//find the difference between this entity's current HP
    			//  and its starting HP, then add 25% of that to the
    			//  current value
    			if( startingHitPoints > hitPoints ){
	    			float diff = startingHitPoints - hitPoints;
	    			if( !e.cursed ){
	    				hitPoints += (diff*0.25);
	    			}else{
	    				hitPoints += (diff*0.25)*curseModifier;
	    			}
    			}
    			
    		}else if( e.name.equals("Strength") ){
    			//increment the player's strength variable
    			strength++;
    			//this should only happen once
    			
    			//remove all curses from the player's equipped items
    			boolean curseRemoved = false;
    			for( Item i : inventory ){
    				if( i.isEquipped && i.isCursed() ){
    					i.removeCurse();
    					curseRemoved = true;
    				}
    			}
    			if( curseRemoved ){
    				returnMessages.add("Your strength has overcome curses in your equipped items.");
    			}
    			
    		}else if( e.name.equals("Flame") ){
    			//decrease health by 10 every second
    			//	want to lose 10 hp per second
    			//	assume 60 frames per second
    			//	10/60 = amount of hp lost per frame
    			if( !e.cursed ){
    				hitPoints -= ( (float) 10/60 );
    			}else{
    				hitPoints -= ( (float) (10*curseModifier)/60 );
    			}
    			
    		}else if( e.name.equals("Mana") ){
    			//add 15% to the current maximum mana
    			if( !e.cursed ){
    				maxMana += (maxMana*0.15);
    			}else{
    				maxMana += (maxMana*0.15*curseModifier);
    			}
    			
    		}else if( e.name.equals("Invisibility") ){
    			//little too complicated for this function,
    			//  just set a boolean value
    			if( !e.cursed ){
    				invisible = true;
    			}
    		}else if( e.name.equals("Poisoned") ){
    			//decrease health by 5 every second
    			if( !e.cursed ){
    				hitPoints -= ( (float) 5/60);
    			}else{
    				hitPoints -= ( (float) (5*curseModifier)/60);
    			}
    			
    		}else if( e.name.equals("Ice") ){
    			//set movement speed to zero
    			if( !e.cursed ){
    				movementSpeed = 0;
    			}else{
    				movementSpeed /= 2;
    			}
    			//animationSpeed = 0;
    			
    		}else if( e.name.equals("Lightning") ){
    			//roll 30% change to take 20 damage
    			rand.setSeed(System.nanoTime());
    			int r = rand.nextInt(100);
    			if( r < 60 ){
    				if( !e.cursed ){
    					hitPoints -= 20;
    				}else{
    					hitPoints -= 20*curseModifier;
    				}
    				currentLevel.addMessage("Struck by lightning!");
    				System.out.println("Struck by lightning!");
    			}
    			
    		}else if( e.name.equals("Stench") ){
    			//another AI problem, set a boolean
    			if( !e.cursed ){
    				stinky = true;
    			}
    		}else if( e.name.equals("Iron Skin") ){
    			//double the armor points variable
    			if( armorPoints == initialArmorPoints ){
    				if( !e.cursed ){
    					armorPoints *= 2;
    				}else{
    					armorPoints *= 2*curseModifier;
    				}
    			}
    			
    			
    		}else if( e.name.equals("Thorns") ){
    			//AI problem
    			if( !e.cursed ){
    				thorny = true;
    			}
    			
    		}else if( e.name.equals("Swiftness") ){
    			//double movement speed
    			//  only if the current speed is equal to the
    			//  initial speed
    			if( movementSpeed == initialMovementSpeed ){
    				doubleMoveSpeed();
    			}
    			
    		}else if( e.name.equals("Fright") ){
    			//AI problem, see stench
    			if( !e.cursed ){
    				frightening = true;
    			}
    		}else if( e.name.equals("Might") ){
    			//double player's attack damage
    			if( !e.cursed ){
    				mighty = true;
    			}
    			
    		}else if( e.name.equals("Regeneration") ){
    			//roll 50% chance to restore 3 hp
    			rand.setSeed(System.nanoTime());
    			int r = rand.nextInt(100);
    			if( r < 50 ){
    				if( !e.cursed ){
    					hitPoints += 3;
    				}else{
    					hitPoints += 3*curseModifier;
    				}
    			}
    			
    		}else if( e.name.equals("Reflection") ){
    			//AI problem, see thorns
    			if( !e.cursed ){
    				reflecting = true;
    			}
    		}else{
    			throw new SlickException("Unknown character effect '"+e.name+"'.");
    		}
    		

    	}
    	

		
		return returnMessages;
					
    }
    
    public void removeSingleEffects(){
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
    
    public Animation getFloatingPlusSigns(String color, int frameCount, int duration) throws SlickException{
    	int spriteWidth = 32;
    	int spriteHeight = 64;
    	SpriteSheet ss = null;
    	if( color.toLowerCase().equals("red") ){
    		ss = ResourceManager.getSpriteSheet(Main.RED_FLOATING_PLUS, spriteWidth, spriteHeight);
    	}else if( color.toLowerCase().equals("green") ){
    		ss = ResourceManager.getSpriteSheet(Main.GREEN_FLOATING_PLUS, spriteWidth, spriteHeight);
    	}else if( color.toLowerCase().equals("blue") ){
    		ss = ResourceManager.getSpriteSheet(Main.BLUE_FLOATING_PLUS, spriteWidth, spriteHeight);
    	}else if( color.toLowerCase().equals("gray") ){
    		ss = ResourceManager.getSpriteSheet(Main.GRAY_FLOATING_PLUS, spriteWidth, spriteHeight);
    	}else if( color.toLowerCase().equals("yellow") ){
    		ss = ResourceManager.getSpriteSheet(Main.YELLOW_FLOATING_PLUS, spriteWidth, spriteHeight);
    	}else{
    		throw new SlickException("Invalid floating plus sign color '" + color + "'.");
    	}
    	
    	Animation ani = new Animation(ss, 0, 0, frameCount-1, 0, true, duration, true);
    	return ani;
    }
    

    
    public boolean takeDamage(float amount, String effect, boolean cursed ){
    	hitPoints -= amount;
    	if( !effect.equals("") ){
    		addEffect(effect, cursed);
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
    			inventoryWeight += i.getWeight();
    			add = false;
    			break;
    		}
    	}
    	if( add ){
    		inventory.add(i);
    		inventoryWeight += i.getWeight();
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
    			Item ret;
    			//return inventory.remove(i);
    			if( inventory.get(i).count == 1 ){
    				inventory.get(i).count--;
    				ret = inventory.remove(i);
    			}else{
    				inventory.get(i).count--;
    				ret = inventory.get(i);
    			}
    	    	//update weight
    	    	//take off the whole stack
    	    	inventoryWeight -= ret.getWeight();
    	    	//get the weight of the new stack
    	    	ret.updateWeight();
    	    	//add the new weight
    	    	inventoryWeight += ret.getWeight();
    	    	
    	    	return ret;
    		}
    	}
    	return null;
        //inventory.removeIf(item -> item.getID() == i_id);
    }
    
    public Item discardItem(Item i, boolean use){
    	boolean removed = false;
    	if( i.count == 1 ){
    		i.count--;
    		inventory.remove(i);
    		removed = true;
    	}else{
    		i.count--;
    	}
    	
    	if( use ){
    		i.identify();
    	}
    	
    	if( i.getType().equals("Potion") ){
    		addToCodex(i);
    	}
    	
    	//update weight
    	//take off the whole stack
    	inventoryWeight -= i.getWeight();
    	//get the weight of the new stack
    	i.updateWeight();
    	if( !removed ){
    		//add the new weight
    		inventoryWeight += i.getWeight();
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

    public float getStartingHitPoints() {
        return startingHitPoints;
    }

    public float getHitPoints(){
        return hitPoints;
    }

    public void setAttackDamage(int dmg) {
        attackDamage = dmg;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackSpeed(int speed) {
        attackSpeed = speed;
    }

    public int getAttackSpeed() {
        return attackSpeed;
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
    public void setMana(float m){
        mana = m;
    }
    public void setMaxMana(float m){
    	maxMana = m;
    }
    public float getMana(){
        return mana;
    }
    public float getMaxMana(){
    	return maxMana;
    }
    public int getStrength(){
    	return strength;
    }
    public int getInventoryWeight(){
    	return inventoryWeight;
    }
    public int getMaxInventoryWeight(){
    	return (int) startingHitPoints * strength;
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
    public ArrayList<String> getCursedItemTypes(){
    	return cursedItemTypes;
    }

    public void setPosition(Vector p){
        position = p;
    }

    public Vector getPosition(){
        return position;
    }
    
    public void addCursedItemType(String type){
    	cursedItemTypes.add(type);
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

    public void setNextTileWorldCoordinates(String direction) {
        int x = (int) getTileWorldCoordinates().getX();
        int y = (int) getTileWorldCoordinates().getY();
        switch (direction) {
            case "walk_up":
                y -= 1;
                break;
            case "walk_down":
                y += 1;
                break;
            case "walk_left":
                x -= 1;
                break;
            case "walk_right":
                x += 1;
                break;
        }
        nextTileWorldCoordinates = new Vector(x, y);
    }

    /*
    The players next coordinates
     */
    public Vector getNextTileWorldCoordinates() {
        return nextTileWorldCoordinates;
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
