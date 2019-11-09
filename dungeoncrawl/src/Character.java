import jig.Vector;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Input;

public class Character extends MovingEntity {
    private String type;
    AnimateEntity animate;
    Animation animation;
    String spritesheet;
    boolean canMove = true;
    Vector wcNext;
    final int tilesize = 32;
    String direction;

    /**
     * Create a new Character (wx, wy)
     * @param wx world coordinates x
     * @param wy world coordinats y
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     * @param id id for MovingEntity
     */
    public Character(final float wx, final float wy, String type, int id) {
        super(wx, wy, id);
        this.type = type;
        setStats();
        setSpeed(100);
        animate = new AnimateEntity(wx, wy, getSpeed(), this.type);
        animate.selectAnimation("walk_down");
        animate.stop();
//        wcNext = getWorldCoordinates();
    }

    /**
     * Create a new Character (Vector)
     * @param wc world coordinates Vector
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     * @param id id for MovingEntity
     */
    public Character(Vector wc, String type, int id) {
        super(wc, id);
        this.type = type;
        setStats();
    }

    /**
     * Sets Character HP, AP, and Mana based on type given.
     */
    private void setStats(){
        switch (type){
            case "knight_leather": // Knight
            case "knight_iron":
            case "knight_gold":
                setHitPoints(100);
                setArmorPoints(100);
                setSpeed(50);
                break;
            case "mage_leather": // Mage
            case "mage_improved":
                setHitPoints(80);
                setArmorPoints(50);
                setSpeed(75);
                setMana(100);
                break;
            case "archer_leather": // Archer
                setHitPoints(100);
                setArmorPoints(50);
                setSpeed(75);
                break;
            case "tank_leather": // Tank
            case "tank_iron":
            case "tank_gold":
                setHitPoints(150);
                setArmorPoints(100);
                setSpeed(25);
                break;
            default:
                System.out.println("ERROR: No matching Character type specified.\n");
                break;
        }
    }

    /**
     * Retrieves the character for the character type.
     * @return type
     */
    public String getType() {
        return type;
    }


//    public void update() {
//        Vector wc = getWorldCoordinates();
//        float wx = wc.getX();
//        float wy = wc.getY();
//        float nx = wcNext.getX();
//        float ny = wcNext.getY();
//        while (wc != wcNext) {
//            if (direction.equals("walk_up")) {
//                walk(wx, nx + 2);
//            }
//            else if (direction.equals("walk_down")) {
//                walk(wx, nx - 2);
//            }
//            else if (direction.equals("walk_left")) {
//                walk(wx - 2, nx);
//            }
//            else if (direction.equals("walk_right")) {
//                walk(wx + 2, nx);
//            }
//        }
//    }

    /*
    Move the character based on the keystrokes given
     */
    public void move(Input input) {
        String movement = null;
        Vector wc = getWorldCoordinates();
        float x = 0, y = 0;
        if (input.isKeyPressed(Input.KEY_W)) {
            movement = "walk_up";
            x = wc.getX();
            y = wc.getY() - tilesize;
        }
        else if (input.isKeyPressed(Input.KEY_S)) {
            movement = "walk_down";
            x = wc.getX();
            y = wc.getY() + tilesize;
        }
        else if (input.isKeyPressed(Input.KEY_A)) {
            movement = "walk_left";
            x = wc.getX() - tilesize;
            y = wc.getY();
        }
        else if (input.isKeyPressed(Input.KEY_D)) {
            movement = "walk_right";
            x = wc.getX() + tilesize;
            y = wc.getY();
        }
        if (movement != null) {
            updateAnimation(movement);
            walk(x, y);
//            wcNext = new Vector(x, y);
        }
    }

    public void updateAnimation(String action) {
        if (action != null) {
            // TODO change getX and getY
            Vector wc = getWorldCoordinates();
            System.out.printf("World Coordinates %s, %s\n", wc.getX(), wc.getY());
            animate = new AnimateEntity(wc.getX(), wc.getY(), getSpeed(), this.type);
            animate.selectAnimation(action);
        }
    }

    public void walk(float x, float y) {
        animate.translate(x, y);
        setWorldCoordinates(new Vector(x, y));

    }
}