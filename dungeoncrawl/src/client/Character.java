package client;

import jig.Vector;

public class Character extends MovingEntity {
    private Main dc;
    AnimateEntity animate;
    private String type;
    private String direction;
    private boolean canMove = true;
    private boolean nearEdge = false;
    private int movesLeft;
    int ox;             // origin x
    int oy;             // origin y
    private int newx;   // next origin x
    private int newy;   // next origin y
    float dx = 0f;      // delta x
    float dy = 0f;      // delta y

    /**
     * Create a new Character (wx, wy)
     *
     * @param wx   world coordinates x
     * @param wy   world coordinates y
     * @param type entity animation to get "knight_leather" for example see the AnimateEntity Class
     * @param id   id for MovingEntity
     */
    public Character(Main dc, final float wx, final float wy, String type, int id) {
        super(wx, wy, id);
        this.dc = dc;
        this.type = type;
        setStats();
        animate = new AnimateEntity(wx, wy, getAnimationSpeed(), this.type);
        direction = "walk_down";
        animate.selectAnimation(direction);
        animate.stop();
        ox = 0;
        oy = 0;
        setAnimationSpeed(50);       // speed of the animation
    }


    /**
     * Create a new Character (Vector)
     *
     * @param wc   world coordinates Vector
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     * @param id   id for MovingEntity
     */
    public Character(Vector wc, String type, int id) {
        super(wc, id);
        this.type = type;
        setStats();
    }
    


    /**
     * Sets Character HP, AP, and Mana based on type given.
     */
    private void setStats() {
        switch (type) {
            case "knight_leather": // Knight
            case "knight_iron":
            case "knight_gold":
                setHitPoints(100);
                setArmorPoints(100);
                setAnimationSpeed(50);
                break;
            case "mage_leather": // Mage
            case "mage_improved":
                setHitPoints(80);
                setArmorPoints(50);
                setAnimationSpeed(75);
                setMana(100);
                break;
            case "archer_leather": // Archer
                setHitPoints(100);
                setArmorPoints(50);
                setAnimationSpeed(75);
                break;
            case "tank_leather": // Tank
            case "tank_iron":
            case "tank_gold":
                setHitPoints(150);
                setArmorPoints(100);
                setAnimationSpeed(25);
                break;
            default:
                System.out.println("ERROR: No matching Character type specified.\n");
                break;
        }
    }

    /**
     * x
     * Retrieves the character for the character type.
     *
     * @return type
     */
    public String getType() {
        return type;
    }


    /**
     * Move the character based on the keystrokes.
     * This is the method that should be called from the level class to move the character
     *
     * @param key String representing a keystroke
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

        if (key == null || key.equals("")) {
            animate.stop();
            return;
        }

        String movement = null;
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
            case "4":       // speed up
            	doubleMoveSpeed();
                break;
            case "5":        // slow down
            	halfMoveSpeed();
                break;
        }
        if (movement != null) {
            canMove = false;
            movesLeft = dc.tilesize;
            if (!movement.equals(direction)) {
                updateAnimation(movement);
                direction = movement;
            } else {
                animate.start();
            }
            // check for collisions with the wall
            if (collision() && dc.collisions) {
                canMove = true;
                return;
            }
            changeOrigin();     // check if the screen origin needs to change
        }
    }


    /*
    This method updates the players position such that it is a smooth transition without jumps. It is
    called from the move method and should not be called by any other methods. Updates the characters coordinates.
     */
    private void moveTranslationHelper() {
        Vector sc = animate.getPosition();
        float sx = sc.getX();
        float sy = sc.getY();
        float x = 0, y = 0;
        if (movesLeft > 0) {
            switch (direction) {
                case "walk_up":
                    x = sx;
                    y = sy - super.getMovementSpeed();
                    break;
                case "walk_down":
                    x = sx;
                    y = sy + super.getMovementSpeed();
                    break;
                case "walk_left":
                    x = sx - super.getMovementSpeed();
                    y = sy;
                    break;
                case "walk_right":
                    x = sx + super.getMovementSpeed();
                    y = sy;
                    break;
            }
            movesLeft -= super.getMovementSpeed();
            updatePosition(x, y);
        } else {
            canMove = true;
        }
    }


    /*
    Moves the map under the character while the character animates in place. Updates the characters world coordinates.
    Should only be called from the move method.
     */
    private void moveMapHelper() {
        if (movesLeft > 0) {
            switch (direction) {
                case "walk_up":
                    dx = 0f;
                    dy -= super.getMovementSpeed();
                    break;
                case "walk_down":
                    dx = 0f;
                    dy += super.getMovementSpeed();
                    break;
                case "walk_left":
                    dx -= super.getMovementSpeed();
                    dy = 0f;
                    break;
                case "walk_right":
                    dx += super.getMovementSpeed();
                    dy = 0f;
                    break;
            }
            movesLeft -= super.getMovementSpeed();
            RenderMap.setMap(dc, this);
        } else {
            ox = newx;
            oy = newy;
            nearEdge = false;
        }
        updateWorldCoordinates();
    }


    /*
    Changes the screen origin if the player is within 5 tiles from the of the edge of the screen
     */
    private void changeOrigin() {
        Vector sc = animate.getPosition();
        int px = (int) sc.getX() / dc.tilesize;
        int py = (int) sc.getY() / dc.tilesize;
        int buffer = 5;
        int height = dc.tilesHigh - 2;
        int width = dc.tilesWide - 2;
        nearEdge = false;

        // move screen up
        if (py < buffer && direction.equals("walk_up")) {
            if (oy - 1 >= 0) {
                nearEdge = true;
                newy--;
            }
        }
        // move screen down
        else if (Math.abs(py - height) < buffer && direction.equals("walk_down")) {
            if (oy - 1 <= height + 2) {
                nearEdge = true;
                newy++;
            }
        }
        // move screen left
        else if (px - 1 < buffer && direction.equals("walk_left")) {
            if (ox > 0) {
                nearEdge = true;
                newx--;
            }
        }
        // move screen right
        else if (Math.abs(px - width) < buffer && direction.equals("walk_right")) {
            if (ox < width + 2) {
                nearEdge = true;
                newx++;
            }
        }
        if (nearEdge) {
            movesLeft = dc.tilesize;
            dx = 0f;
            dy = 0f;
        }
    }


    /*
    check if there is a collision at the next world x, y with the wall
    returns true if there is a collision, false otherwise
     */
    private boolean collision() {
        Vector wc = getWorldCoordinates();
        int x = (((int) wc.getX() + dc.offset) / dc.tilesize) - 1;
        int y = (((int) wc.getY() + dc.tilesize + dc.doubleOffset) / dc.tilesize) - 1;
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
        return (dc.map[y][x] != 0);
    }


    /**
     * Updates the animation that is currently in use
     *
     * @param action a new animations action to be selected
     */
    private void updateAnimation(String action) {
        if (action != null) {
            Vector sc = animate.getPosition();
            animate = new AnimateEntity(sc.getX(), sc.getY(), getAnimationSpeed(), this.type);
            animate.selectAnimation(action);
        }
    }


    /**
     * Translates the entity's screen position and sets the correct world coordinates
     *
     * @param x The new x screen position
     * @param y The new y screen position
     */
    private void updatePosition(float x, float y) {
        animate.setPosition(x, y);      // screen coordinates
        float wx = x + (ox * dc.tilesize);
        float wy = y + (oy * dc.tilesize);
        setWorldCoordinates(wx, wy);    // world coordinates
    }


    /**
     * Updates the characters world position when the screen is scrolling
     */
    private void updateWorldCoordinates() {
        Vector sc = animate.getPosition();
        float wx = (ox * dc.tilesize) + sc.getX();
        float wy = (oy * dc.tilesize) + sc.getY();
        setWorldCoordinates(wx, wy);    // world coordinates
    }
}