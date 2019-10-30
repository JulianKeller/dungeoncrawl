import jig.Entity;

public class MovingEntity extends Entity {
    private int hitPoints;
    private int armorPoints;

    /**
     * Create a new Entity
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param hp entity's starting hit points
     * @param ap entity's starting armor points
     */
    public MovingEntity(final float x, final float y, int hp, int ap) {
        super(x,y);
        hitPoints = hp;
        armorPoints = ap;

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
