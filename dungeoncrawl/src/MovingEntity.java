import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Input;

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
        hitPoints = 0;
        armorPoints = 0;
        worldCoordinates = new Vector(wx,wy);
        speed = 0;
        mana = 0;
        this.pid = pid;
        inventory = new ArrayList<Item>(10);
        equipped = new Item[4];
        Arrays.fill(equipped, null);
        position = new Vector(0,0);
    }

    /**
     * Create a new Entity (Vector)
     * @param wc Vector for starting world coordinates of the moving entity
     * @param pid entity's id.
     */
    public MovingEntity(Vector wc, int pid){
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
     * Removes an item from the MovingEntity's Inventory
     * @param i_id id of the item to be removed.
     */
    public void discardItem(int i_id){
        inventory.removeIf(item -> item.getID() == i_id);
    }

    /**
     * Equips an item from the MovingEntity's inventory
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
        }
    }

    /**
     * Unequips the item from MovingEntity's equipped list and places it in inventory.
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
        speed = sp;
    }
    /**
     * Retreive MovingEntity's speed
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
    /**
     * Returns the MovingEntity's current world coordinates.
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

    public void update(int delta){
        translate(position.scale(delta));
    }


//    public static String getSpritesheet(String sprite) {
//        String spritesheet = null;
//        switch (sprite) {
//            case "knight_leather": {
//                spritesheet = Main.KNIGHT_LEATHER;
//                break;
//            }
//            case "knight_iron": {
//                spritesheet = Main.KNIGHT_IRON;
//                break;
//            }
//            case "knight_gold": {
//                spritesheet = Main.KNIGHT_GOLD;
//                break;
//            }
//            case "mage_leather": {
//                spritesheet = Main.MAGE_LEATHER;
//                break;
//            }
//            case "mage_improved": {
//                spritesheet = Main.MAGE_IMPROVED;
//                break;
//            }
//            case "archer_leather": {
//                spritesheet = Main.ARCHER_LEATHER;
//                break;
//            }
//            case "tank_leather": {
//                spritesheet = Main.TANK_LEATHER;
//                break;
//            }
//            case "tank_iron": {
//                spritesheet = Main.TANK_IRON;
//                break;
//            }
//            case "tank_gold": {
//                spritesheet = Main.TANK_GOLD;
//                break;
//            }
//            case "skeleton_basic": {
//                spritesheet = Main.SKELETON_BASIC;
//                break;
//            }
//            case "skeleton_leather": {
//                spritesheet = Main.SKELETON_LEATHER;
//                break;
//            }
//            case "skeleton_chain": {
//                spritesheet = Main.SKELETON_CHAIN;
//                break;
//            }
//            case "ice_elf": {
//                spritesheet = Main.ICE_ELF;
//                break;
//            }
//        }
//        return spritesheet;
//    }
//
//    /*
//     Selects and starts the appropriate animation sequence for the specified sprites action
//    */
//    public static Animation selectAnimation(String action, String spritesheet, int speed) {
//        speed = 100;
//        int row = 0;        // sprite sheet y
//        int startx = 0;
//        int endx = 0;
//        int spritesize = 64;
//        System.out.println("action: " + action);
//        switch (action) {
//            case "spell_up": {
//                row = 0;
//                startx = 0;
//                endx = 6;
//                break;
//            }
//            case "spell_left": {
//                row = 1;
//                startx = 0;
//                endx = 6;
//                break;
//            }
//            case "spell_down": {
//                row = 2;
//                startx = 0;
//                endx = 6;
//                break;
//            }
//            case "spell_right": {
//                row = 3;
//                startx = 0;
//                endx = 6;
//                break;
//            }
//            case "jab_up": {
//                row = 4;
//                startx = 0;
//                endx = 7;
//                break;
//            }
//            case "jab_left": {
//                row = 5;
//                startx = 0;
//                endx = 7;
//                break;
//            }
//            case "jab_down": {
//                row = 6;
//                startx = 0;
//                endx = 7;
//                break;
//            }
//            case "jab_right": {
//                row = 7;
//                startx = 0;
//                endx = 7;
//                break;
//            }
//            case "walk_up": {
//                row = 8;
//                startx = 0;
//                endx = 8;
//                break;
//            }
//            case "walk_left": {
//                row = 9;
//                startx = 0;
//                endx = 8;
//                break;
//            }
//            case "walk_down": {
//                row = 10;
//                startx = 0;
//                endx = 8;
//                break;
//            }
//            case "walk_right": {
//                row = 11;
//                startx = 0;
//                endx = 8;
//                break;
//            }
//            case "slash_up": {
//                row = 12;
//                startx = 0;
//                endx = 5;
//                break;
//            }
//            case "slash_left": {
//                row = 13;
//                startx = 0;
//                endx = 5;
//                break;
//            }
//            case "slash_down": {
//                row = 14;
//                startx = 0;
//                endx = 5;
//                break;
//            }
//            case "slash_right": {
//                row = 15;
//                startx = 0;
//                endx = 5;
//                break;
//            }
//            case "shoot_up": {
//                row = 16;
//                startx = 0;
//                endx = 12;
//                break;
//            }
//            case "shoot_left": {
//                row = 17;
//                startx = 0;
//                endx = 12;
//                break;
//            }
//            case "shoot_down": {
//                row = 18;
//                startx = 0;
//                endx = 12;
//                break;
//            }
//            case "shoot_right": {
//                row = 19;
//                startx = 0;
//                endx = 12;
//                break;
//            }
//            case "die": {
//                row = 20;
//                startx = 0;
//                endx = 5;
//                break;
//            }
//        }
//        Animation animation = new Animation(ResourceManager.getSpriteSheet(spritesheet, spritesize, spritesize), startx, row, endx, row, true, speed, true);
//        animation.setLooping(true);
////        addAnimation(animation);
//        return animation;
//    }
    
}
