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
        animate = new AnimateEntity(wx, wy, getSpeed(), this.type);
//        System.out.println("Starting getPosition(): " + animate.getPosition());

        direction = "walk_down";
        animate.selectAnimation(direction);
        animate.stop();
        ox = 0;
        oy = 0;
        screenOrigin = 0;
        screenEnd = screenOrigin + dc.tilesWide;
        setSpeed(25);
        moveSpeed = 2;
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
        animate.stop();
        switch (key) {
            case "w":
                movement = "walk_up";
                break;
            case "s":
                movement = "walk_down";
                break;
            case "a":
                movement = "walk_left";
                break;
            case "d":
                movement = "walk_right";
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
//        System.out.println("Move Translation Helper");
//        Vector wc = getWorldCoordinates();
        Vector sc = animate.getPosition();
        float sx = sc.getX();
        float sy = sc.getY();
        float x = 0, y = 0;
//        System.out.printf("%s: moveTranslationHelper Animate Position: %s, %s", movesLeft, animate.getX(), animate.getY());
//        System.out.println("Direction: " + direction);
        if (movesLeft > 0) {
            switch (direction) {
                case "walk_up":
                    x = sx;
                    y = sy - moveSpeed;
                    break;
                case "walk_down":
                    x = sx;
                    y = sy + moveSpeed;
                    break;
                case "walk_left":
                    x = sx - moveSpeed;
                    y = sy;
                    break;
                case "walk_right":
                    x = sx + moveSpeed;
                    y = sy;
                    break;
            }
            movesLeft -= moveSpeed;
//            System.out.printf(" next %s, %s\n", x, y);
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
//        System.out.println("Move Map Helper");
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
            movesLeft -= change;
            RenderMap.setMap(dc, this);
        }
        else {
            ox = newx;
            oy = newy;
            updateWorldPosition();
            nearEdge = false;
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
        Vector sc = animate.getPosition();
        int px = (int) sc.getX();
        int py = (int) sc.getY();
        px = px/dc.tilesize;
        py = py/dc.tilesize;
        int buffer = 5;
        nearEdge = false;
        int height = dc.tilesHigh - 2;
        int width = dc.tilesWide - 2;

//        System.out.printf("height: %s, width: %s\n", height, width);

        // move screen up
        if (py < buffer && direction.equals("walk_up")) {
//            System.out.printf("up: %s < %s\n", py, buffer);
            if (oy - 1 >= 0) {
                nearEdge = true;
                newy--;
            }
        }
        // move screen down
        else if (Math.abs(py - height) < buffer && direction.equals("walk_down")) {
//            System.out.printf("down: %s - %s = %s < %s\n", py, height, Math.abs(py - height), buffer);
//            System.out.printf("oy: %s, height: %s\n", oy, height);
            if (oy - 1 <= height + 2) {
                nearEdge = true;
                newy++;
            }

        }
        // move screen left
        else if (px - 1 < buffer && direction.equals("walk_left")) {
//            System.out.printf("left: %s < %s\n", px, buffer);
            if (ox > 0) {
                nearEdge = true;
                newx--;
            }
        }
        // move screen right
        else if (Math.abs(px - width) < buffer && direction.equals("walk_right")) {
//            System.out.printf("right: %s - %s = %s < %s\n", px, width, Math.abs(px - width), buffer);
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
    check if there is a collision at the next world x, y with the wall
    returns true if there is a collision, false otherwise
     */
    // TODO this method needs to be adjusted for the screen coordinates
    private boolean collision() {
        Vector wc = getWorldCoordinates();
        int x = (((int) wc.getX() + dc.offset)/dc.tilesize) - 1;
        int y = (((int) wc.getY()+ dc.tilesize + dc.doubleOffset)/dc.tilesize) - 1;
//        System.out.printf("World: %s, ox, oy: %s, %s\n", wc, ox, oy);
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
//        System.out.printf("dc.map[%s][%s] = ", y, x, dc.map[y][x]);
//        System.out.printf(" %s\n", dc.map[y][x]);
        return (dc.map[y][x] != 0);
    }



    /**
    Updates the animation that is currently in use
    @param action a new animations action to be selected
     */
    private void updateAnimation(String action) {
//        System.out.println("Update Animation: " + action );
        if (action != null) {
//            Vector wc = getWorldCoordinates();
            // TODO adjust for world coordinates
//            int x = (((int) wc.getX() + dc.offset)/dc.tilesize) - 1;
//            int y = (((int) wc.getY()+ dc.tilesize + dc.doubleOffset)/dc.tilesize) - 1;
//            float wx = wc.getX();
//            float wy = wc.getY();
//            System.out.printf("World Coordinates: %s, %s\n", wx, wy);
//            animate = null;
//            System.out.printf("Update Animation position: %s, %s\n", animate.getX(), animate.getY());
            Vector sc = animate.getPosition();
            animate = new AnimateEntity(sc.getX(), sc.getY(), getSpeed(), this.type);
            animate.selectAnimation(action);
        }
    }

    /**
     Translates the entity's screen position and sets the correct world coordinates
     @param x The new x screen position
     @param y The new y screen position
     */
    private void updatePosition(float x, float y) {
        System.out.printf("\nSetting Screen Position to: %s, %s\n", x, y);
//        System.out.println("bf: animate.getPosition() " + animate.getPosition());
        Vector sc = animate.getPosition();
        System.out.printf("bf: animate.getPosition() %s\n", animate.getPosition());
        animate.setPosition(x, y);      // screen coordinates
//        animate.setX(x);
//        animate.setY(y);
        System.out.println("af: animate.getPosition() " + animate.getPosition());
//        System.out.printf("af: animate.getX(), animate.getY(): %s, %s\n\n", animate.getX(), animate.getY());


        Vector wc = getWorldCoordinates();
        float wx = x + (ox * dc.tilesize);
        float wy = y + (oy * dc.tilesize);
        setWorldCoordinates(wx, wy);    // world coordinates
//        System.out.printf("updatePosition() wc: %s, %s\n\n", wx, wy);
    }

    /**
     Updates the characters world position when the screen is scrolling
     */
    private void updateWorldPosition() {
        Vector wc = getWorldCoordinates();
        Vector sc = animate.getPosition();
        float wx = (ox * dc.tilesize) + sc.getX();
        float wy = (oy * dc.tilesize) + sc.getY();
        setWorldCoordinates(wx, wy);    // world coordinates
        System.out.printf("updateWorldPosition(): %s, %s\n", wx, wy);
    }
}