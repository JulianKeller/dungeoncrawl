package client;

import jig.Vector;
import org.newdawn.slick.Animation;

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
    boolean nearEdge = false;
    //    Vector origin;
    int ox;     // origin x
    int oy;     // origin y
    int newx;   // next origin x
    int newy;   // next origin y
    float dx = 0f;     // delta x
    float dy = 0f;     // delta y

    /**
     * Create a new client.Character (wx, wy)
     * @param wx world coordinates x
     * @param wy world coordinats y
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     * @param id id for client.MovingEntity
     */
    public Character(Main dc, final float wx, final float wy, String type, int id) {
        super(wx, wy, id);
        this.dc = dc;
        this.type = type;
        setStats();
        animate = new AnimateEntity( wx, wy, getSpeed(), this.type);
        direction = "walk_down";
        animate.selectAnimation(direction);
        animate.stop();
        ox = 0;
        oy = 0;
        screenOrigin = 0;
        screenEnd = screenOrigin + dc.tilesWide;
        setSpeed(25);
        moveSpeed = 8;
    }

    /**
     * Create a new client.Character (Vector)
     * @param wc world coordinates Vector
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     * @param id id for client.MovingEntity
     */
    public Character(Vector wc, String type, int id) {
        super(wc, id);
        this.type = type;
        setStats();
    }

    /**
     * Sets client.Character HP, AP, and Mana based on type given.
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
                System.out.println("ERROR: No matching client.Character type specified.\n");
                break;
        }
    }

    /**x
     * Retrieves the character for the character type.
     * @return type
     */
    public String getType() {
        return type;
    }


    /*
   Move the character based on the keystrokes.
   This is the method that should be called from the level class to move the character
   @param key String representing a keystroke
    */
    public void move(String key) {
        // move the screen under the character, fixed to a grid
        if (nearEdge) {
            moveMapHelper();
            return;
        }

        // moved the character fixed to the grid
        if (!canMove) {
            moveTranslationHelper();
            return;
        }

        String movement = null;
        float distance = 1f;
        Vector wc = getWorldCoordinates();
        float x = 0, y = 0;

        animate.stop();
        switch (key) {
            case "w":
                movement = "walk_up";
                x = wc.getX();
                y = wc.getY() - distance;
                break;
            case "s":
                movement = "walk_down";
                x = wc.getX();
                y = wc.getY() + distance;
                break;
            case "a":
                movement = "walk_left";
                x = wc.getX() - distance;
                y = wc.getY();
                break;
            case "d":
                movement = "walk_right";
                x = wc.getX() + distance;
                y = wc.getY();
                break;
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
            changeOrigin();
        }
    }


    /*
    This method updates the players position such that it is a smooth transition without jumps. It is
    called from the move method and should not be called by any other methods.
     */
    private void moveTranslationHelper() {
        Vector wc = getWorldCoordinates();
        float wx = wc.getX();
        float wy = wc.getY();
        float x = 0, y = 0;
        int change = moveSpeed;

        if (movesLeft > 0) {
            switch (direction) {
                case "walk_up":
                    x = wx;
                    y = wy - change;
                    break;
                case "walk_down":
                    x = wx;
                    y = wy + change;
                    break;
                case "walk_left":
                    x = wx - change;
                    y = wy;
                    break;
                case "walk_right":
                    x = wx + change;
                    y = wy;
                    break;
            }
            movesLeft -= change;
            updatePosition(x, y);
        }
        else {
            canMove = true;
        }
    }


    /*
    Moves the map under the character
     */
    private void moveMapHelper() {
        Vector wc = getWorldCoordinates();
        float wx = wc.getX();
        float wy = wc.getY();
        float x = 0, y = 0;
        float change = moveSpeed;

//        System.out.println("movesleft: " + movesLeft);
        if (movesLeft > 0) {
            switch (direction) {
                case "walk_up":
                    dx = 0f;
                    dy -= change;
                    break;
                case "walk_down":
                    dx = 0f;
                    dy += change;
                    break;
                case "walk_left":
                    dx -= change;
                    dy = 0f;
                    break;
                case "walk_right":
                    dx += change;
                    dy = 0f;
                    break;
            }
//            System.out.printf("dx, dy = %s, %s\n", dx, dy);
            movesLeft -= change;
            RenderMap.setMap(dc, this);
        }
        else {
            nearEdge = false;
            System.out.printf("Origin updated %s, %s --> %s, %s\n", ox, oy, newx, newy);
            ox = newx;
            oy = newy;
        }

    }

    /*
    Changes the screen origin if the player is in range
    Note: Not using the Vector class as we don't want to pass classes around AND
    it refused to update it's values when using the setX function.
     */
    /* TODO
        - screen origin only changes in the x or y direction, not both, this is not currently working
        - if character is within buffer of screen edge, enable animation, but player doesn't move
        - players position stays the same, but the players origin changes

     */
    private boolean changeOrigin() {
        // calculate players distance to up, down, left, right borders of screen
        int px = (int) animate.getX();
        int py = (int) animate.getY();
        px = px/dc.tilesize;
        py = py/dc.tilesize;
        int buffer = 5;
        nearEdge = false;
        int height = dc.tilesHigh - 2;
        int width = dc.tilesWide - 2;

//        System.out.printf("height: %s, width: %s\n", height, width);

        // move screen up
        if (py < buffer && direction.equals("walk_up")) {
            System.out.printf("up: %s < %s\n", py, buffer);
            if (oy - 1 >= 0) {
                nearEdge = true;
                newy--;
            }
        }
        // move screen down
        else if (Math.abs(py - height) < buffer && direction.equals("walk_down")) {
            System.out.printf("down: %s - %s = %s < %s\n", py, height, Math.abs(py - height), buffer);
//            System.out.printf("oy: %s, height: %s\n", oy, height);
            if (oy - 1 <= height + 2) {
                nearEdge = true;
                newy++;
            }

        }
        // move screen left
        else if (px - 1 < buffer && direction.equals("walk_left")) {
            System.out.printf("left: %s < %s\n", px, buffer);
            if (ox > 0) {
                nearEdge = true;
                newx--;
            }
        }
        // move screen right
        else if (Math.abs(px - width) < buffer && direction.equals("walk_right")) {
            System.out.printf("right: %s - %s = %s < %s\n", px, width, Math.abs(px - width), buffer);
            if (ox < width + 2) {
                nearEdge = true;
                newx++;
            }
        }

        if (nearEdge) {
            movesLeft = dc.tilesize;
            dx = 0f;
            dy = 0f;
            return true;
        }
        return false;
    }


    /*
    check if there is a collision at the next x, y with the wall
    returns true if there is a collision, false otherwise
     */
    // TODO this method needs to be adjusted for the screen coordinates
    private boolean collision() {
        int len = dc.map.length;
        int width = dc.map[0].length;
        int x = (int) animate.getX() + (dc.tilesize * screenOrigin);
        int y = (int) animate.getY() + (dc.tilesize * screenOrigin);
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
        x = x/dc.tilesize;
        y = y/dc.tilesize;
        return (dc.map[y][x] != 0);
    }



    /*
    Updates the animation that is currently in use
    @param action a new animations action to be selected
     */
    private void updateAnimation(String action) {
        if (action != null) {
            // TODO change getX and getY
            Vector wc = getWorldCoordinates();
//            System.out.printf("Update World Coordinates %s, %s\n", wc.getX(), wc.getY());
            animate = new AnimateEntity(wc.getX(), wc.getY(), getSpeed(), this.type);
            animate.selectAnimation(action);
            // TODO set world coordinates
        }
    }

    /*
     Translates the entity's screen and world coordinates position
     @param x The new x position
     @param y The new y position
     */
    private void updatePosition(float x, float y) {
        Vector wc = getWorldCoordinates();
//        System.out.printf("World Coordinates %s, %s\n", wc.getX(), wc.getY());
//        System.out.printf("Walk Coordinates %s, %s\n\n", x, y);
        animate.setPosition(x, y);
        setWorldCoordinates(x, y);
    }
}