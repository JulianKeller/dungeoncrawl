import jig.Entity;
import jig.Vector;

public class MovingEntity extends Entity {
    private int hitPoints;
    private int armorPoints;
    private Vector worldCoordinates;
    private float speed;

    /**
     * Create a new Entity
     * @param wx starting world x coordinate
     * @param wy starting world y coordinate
     * @param hp entity's starting hit points
     * @param ap entity's starting armor points
     * @param spd entity's starting speed.
     */
    public MovingEntity(final float wx, final float wy, int hp, int ap, float spd) {
        hitPoints = hp;
        armorPoints = ap;
        worldCoordinates = new Vector(wx,wy);
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

}
