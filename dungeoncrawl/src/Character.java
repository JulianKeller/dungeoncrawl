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
    int screenOrigin;
    int screenEnd;
    int moveSpeed;
    Vector origin;

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
//        System.out.printf("Start Position %s, %s", wx, wy);
        origin = new Vector(0, 0);
        screenOrigin = 0;
        screenEnd = screenOrigin + dc.width;
        setSpeed(25);
        moveSpeed = 2;
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
        int change = moveSpeed;

        // TODO check players distance from edges of the screen
        changeOrigin();

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
            walk(x, y);
        }
        else {
            canMove = true;
        }
    }

    /*
    Changes the screen origin if the player is in range
     */
    // TODO screen origin only changes in the x or y direction, not both, this is not currently working
    private void changeOrigin() {
        // calculate players distance to up, down, left, right borders of screen
        int x = (int) animate.getX();
        int y = (int) animate.getY();
        x = x/dc.tilesize;
        y = y/dc.tilesize;
//        System.out.printf("%s, %s\n", x, y);
        int buffer = 5;
        float ox = origin.getX()/dc.tilesize;
        float oy = origin.getY()/dc.tilesize;


//        System.out.println(x - ox < buffer);
//        System.out.println(oy - y < buffer);
        // ox > 0 &&
        if (x - ox < buffer) {
//            System.out.println("original Origin " + origin);
////            origin.setX(ox + buffer);
//            System.out.println("Origin Updated: " + origin);
            origin = new Vector(ox + buffer, oy);
        }
        else if (y - oy < buffer) {
//            System.out.println("original Origin " + origin);
////            origin.setY(oy + buffer);
//            System.out.println("Origin Updated: " + origin);
            origin = new Vector(ox, oy + buffer);
        }


//        else if (ox ) {
//            System.out.println("Changing Origin 2");
//            screenOrigin += buffer;
//            screenEnd += buffer;
//        }



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
            // check for collisions with the wall
            if (collision() && dc.collisions) {
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
    // TODO this method needs to be adjusted for the screen coordinates
    public boolean collision() {
        int len = dc.map.length;
        int width = dc.map[0].length;
        int x = (int) animate.getX() + (dc.tilesize * screenOrigin);
        int y = (int) animate.getY() + (dc.tilesize * screenOrigin);
        System.out.printf("Position x, y:  %s, %s --> %s, %s\n", x, y, x/dc.tilesize, y/dc.tilesize);
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