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
    int movesLeft;
    Main dc;

    /**
     * Create a new Character (wx, wy)
     * @param wx world coordinates x
     * @param wy world coordinats y
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     * @param id id for MovingEntity
     */
    public Character(Main dc, final float wx, final float wy, String type, int id) {
        super(wx, wy, id);
        this.dc = dc;
        this.type = type;
        setStats();
//        Vector wc = getWorldCoordinates();
        // FIXME for some reason the x, y coordinates are getting divided by 2 before they get here
        animate = new AnimateEntity( wx, wy, getSpeed(), this.type);
        direction = "walk_down";
        animate.selectAnimation(direction);
        animate.stop();
        System.out.printf("Start Position %s, %s", wx, wy);
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


    public void update() {
        Vector wc = getWorldCoordinates();
        float wx = wc.getX();
        float wy = wc.getY();
        float x = 0, y = 0;
        int change = 1;
        if (movesLeft > 0) {
            if (direction.equals("walk_up")) {
                x = wx;
                y = wy - change;
            }
            else if (direction.equals("walk_down")) {
                x = wx;
                y = wy + change;
            }
            else if (direction.equals("walk_left")) {
                x = wx - change;
                y = wy;
            }
            else if (direction.equals("walk_right")) {
                x = wx + change;
                y = wy;
            }
            movesLeft -= change;
//            animate.setX(x);
//            animate.setY(y);
            walk(x, y);
//            setWorldCoordinates(new Vector(x, y));
        }
        else {
            canMove = true;
//            System.out.printf("Collision? x, y:  %s, %s\n", animate.getX() - 16, animate.getY() - 16);
//            System.out.println();
        }
    }

    /*
    Move the character based on the keystrokes given
     */
    // TODO configure such that the entity only moves 32 pixels each time
    public void move(String key) {
        // keep the character fixed to the grid
        if (!canMove) {
            update();
            return;
        }

        String movement = null;
        float distance = 1f;
        Vector wc = getWorldCoordinates();
        float x = 0, y = 0;

        if (key == null && animate != null) {
            animate.stop();
        }
        else if (key.equals("w")) {
            movement = "walk_up";
            x = wc.getX();
            y = wc.getY() - distance;
        }
        else if (key.equals("s")) {
            movement = "walk_down";
            x = wc.getX();
            y = wc.getY() + distance;
        }
        else if (key.equals("a")) {
            movement = "walk_left";
            x = wc.getX() - distance;
            y = wc.getY();
        }
        else if (key.equals("d")) {
            movement = "walk_right";
            x = wc.getX() + distance;
            y = wc.getY();
        }
        if (movement != null) {
            canMove = false;
            movesLeft = dc.tilesize;      // -1 because we walk once before the update method
            if (!movement.equals(direction)) {
                updateAnimation(movement);
                direction = movement;
            }
            else {
                animate.start();
            }
            if (collision()) {
                canMove = true;
                return;
            }
            update();
        }
    }


    /*
    check if there is a collision at the next x, y with the wall
    returns true if there is a collision, false otherwise
     */
    public boolean collision() {
        int len = dc.map.length;
        int width = dc.map[0].length;
        int x = (int) animate.getX();
        int y = (int) animate.getY();
//        System.out.printf("Position x, y:  %s, %s --> %s, %s\n", x, y, x/dc.tilesize, y/dc.tilesize);
        if (direction.equals("walk_up")) {
            y -= dc.tilesize;
        }
        else if (direction.equals("walk_down")) {
            y += dc.tilesize;
        }
        else if (direction.equals("walk_left")) {
            x -= dc.tilesize;
        }
        else if (direction.equals("walk_right")) {
            x += dc.tilesize;
        }
//        System.out.printf("this x, this y:  %s, %s\n", this.getX(), this.getY());
//        System.out.printf("Collision? x, y:  %s, %s", x, y);
        x = x/dc.tilesize;
        y = y/dc.tilesize;
//        System.out.printf(" -->  %s, %s\n\n", x, y);
        return (dc.map[y][x] != 0);
    }



    /*
    Updates the animation that is currently in use
     */
    public void updateAnimation(String action) {
        if (action != null) {
            // TODO change getX and getY
            Vector wc = getWorldCoordinates();
//            System.out.printf("Update World Coordinates %s, %s\n", wc.getX(), wc.getY());
            animate = new AnimateEntity(wc.getX(), wc.getY(), getSpeed(), this.type);
            animate.selectAnimation(action);
            // TODO set world coordinates
        }
    }

    // Translates the entity's position
    public void walk(float x, float y) {
        Vector wc = getWorldCoordinates();
//        System.out.printf("World Coordinates %s, %s\n", wc.getX(), wc.getY());
//        System.out.printf("Walk Coordinates %s, %s\n\n", x, y);
        animate.setPosition(x, y);
        setWorldCoordinates(x, y);

    }
}