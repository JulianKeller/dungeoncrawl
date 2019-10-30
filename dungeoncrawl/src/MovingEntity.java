import jig.Entity;
import jig.Vector;

public class MovingEntity extends Entity {
    private int hitPoints;
    private int armorPoints;
    private Vector worldCoordinates;
    private int speed;

    /**
     * Create a new Entity (x,y)
     * @param wx starting world x coordinate
     * @param wy starting world y coordinate
     * @param hp entity's starting hit points
     * @param ap entity's starting armor points
     * @param spd entity's starting speed.
     */
    public MovingEntity(final float wx, final float wy, int hp, int ap, int spd) {
        hitPoints = hp;
        armorPoints = ap;
        worldCoordinates = new Vector(wx,wy);
        speed = spd;
    }

    /**
     * Create a new Entity (Vector)
     * @param wc Vector for starting world coordinates of the moving entity
     * @param hp entity's starting hit points
     * @param ap entity's starting armor points
     * @param spd entity's starting speed.
     */
    public MovingEntity(Vector wc, int hp, int ap, int spd){
        hitPoints = hp;
        armorPoints = ap;
        worldCoordinates = wc;
        speed = spd;
    }
    /**
     * Retrieve current hit points.
     * @return current hit points
     */
    public int getHitPoints(){
        return hitPoints;
    }

    /**
     * Retrieve current armor points
     * @return current armor points
     */
    public int getArmorPoints(){
        return armorPoints;
    }

    /**
     * Subtracts hit points by specified amount
     * @param amt amount to subtract hit points by
     */
    public void subHitPoints(int amt){
        hitPoints-= amt;
    }


    public Vector getWorldCoordinates(){
        return worldCoordinates;
    }

    /**
     * Sets the moving entity's worldcoordinates
     * @param wc Vector to set entity's world coordinates.
     */
    public void setWorldCoordinates(Vector wc){
        worldCoordinates = wc;
    }
    /**
     * Adds hit points by specified amount
     * @param amt amount to add hit points by
     */
    public void addHitPoints(int amt){
        hitPoints += amt;
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
}
