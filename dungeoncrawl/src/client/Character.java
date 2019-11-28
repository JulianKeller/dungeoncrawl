package client;

import jig.Vector;

import java.util.ArrayList;
import java.util.Random;

public class Character extends MovingEntity {
    private Main dc;
    AnimateEntity animate;
    private String type;
    private String direction;
    private boolean canMove = true;
    private boolean nearEdge = false;
    public boolean ai;
    private int movesLeft;
    private int moveSpeed;
    int ox;             // origin x
    int oy;             // origin y
    float pixelX;       // players exact origin in pixels
    float pixelY;       // players exact origin in pixels
    private int newx;   // next origin x
    private int newy;   // next origin y
    float dx = 0f;      // delta x
    float dy = 0f;      // delta y
    ArrayList<int[]> shortest;
    ArrayList<Arrow> arrows;
    float[][] weights;
    int range;          // range to player in tiles to use dijkstra's

    /**
     * Create a new Character (wx, wy)
     *
     * @param wx   world coordinates x
     * @param wy   world coordinates y
     * @param type entity animation to get "knight_leather" for example see the AnimateEntity Class
     * @param id   id for MovingEntity
     * @param AI:  true if this is an AI character
     */

    public Character(Main dc, final float wx, final float wy, String type, int id, Level level, boolean AI) {
        super(wx, wy, id, level);

        this.dc = dc;
        this.type = type;
        setStats();
        animate = new AnimateEntity(wx, wy, getAnimationSpeed(), this.type);
        direction = "walk_down";
        animate.selectAnimation(direction);
        animate.stop();
        ox = 0;
        oy = 0;
        pixelX = 0f;
        pixelY = 0f;
        setAnimationSpeed(50);       // speed of the animation
        ai = AI;            //
        shortest = new ArrayList<>();
        arrows = new ArrayList<>();
        range = 10;
    }
    
    public Vector getOrigin(){
    	return new Vector(ox, oy);
    }


    /**
     * Create a new Character (Vector)
     *
     * @param wc   world coordinates Vector
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     * @param id   id for MovingEntity
     */
    public Character(Vector wc, String type, int id, Level level) {
        super(wc, id, level);
        this.type = type;
        setStats();

    }

    public void setType(String type) {
        this.type = type;
        float hp = getHitPoints();
        setStats();
        setHitPoints(hp);
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
                break;
            case "tank_iron":
                break;
            case "tank_gold":
                setHitPoints(150);
                setArmorPoints(100);
                setAnimationSpeed(25);
                break;
            case "skeleton_basic":
                setHitPoints(150);
                setArmorPoints(100);
                setAnimationSpeed(25);
                break;
            default:
                System.out.println("ERROR: No matching Character type specified." +
                        " Setting default values\n");
                setHitPoints(100);
                setArmorPoints(100);
                setAnimationSpeed(25);
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
            moveTranslationHelper(animate.getPosition());
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


    /**
     * Move the AI randomly until the character is within range
     * This is the method that should be called from the level class to move the AI
     */
    public void moveAI() {
        String[] moves = {"walk_up", "walk_down", "walk_left", "walk_right", "wait"};
        String next = null;
        String currentDirection = direction;
        // moved the character fixed to the grid
        if (!canMove) {
            moveTranslationHelper(getWorldCoordinates());
            return;
        }

        // run dijkstra's so enemies attack the player
        if (playerNearby(range)) {
            PathFinding find = new PathFinding(dc, getTileWorldCoordinates(), dc.hero.getTileWorldCoordinates());
            int startX = (int) getTileWorldCoordinates().getX();
            int startY = (int) getTileWorldCoordinates().getY();
            shortest = find.dijkstra(dc, startX, startY);
//            PathFinding.printShortestPath(shortest);      // print shortest path to console for debugging

            // load arrows for dijkstra's debugging
            if (dc.showPath) {
                Arrow.removeArrows(this);
                Arrow.loadPathArrows(dc, this);
                weights = find.getWeights();
            }
            next = getNextDirection(dc);
        } else {
            Arrow.removeArrows(this);
            weights = null;
        }
        // move based on the shortest path

        if (next != null) {
            direction = next;
        } else {
            // get a random direction to move in
            int rand = new Random().nextInt(moves.length);
            direction = moves[rand];
            if (direction.equals("wait")) {
                animate.stop();
                return;
            }
        }
        String movement = direction;
        canMove = false;
        movesLeft = dc.tilesize;
        if (!movement.equals(currentDirection)) {
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

    /**
     * @return Checks if the player is within range of the ai, true if so
     */
    public boolean playerNearby(int range) {
        Vector heroWC = dc.hero.getTileWorldCoordinates();
        Vector aiWC = getTileWorldCoordinates();
        if (Math.abs(heroWC.getX() - aiWC.getX()) <= range && (Math.abs(heroWC.getY() - aiWC.getY()) <= range)) {
            return true;
        }
        return false;
    }

    // get next direction based on Dijkstra shortest path
    public String getNextDirection(Main dc) {
        if (shortest.isEmpty() || shortest.size() <= 2) {
            return null;
        }
        int px = (int) getTileWorldCoordinates().getX();
        int py = (int) getTileWorldCoordinates().getY();
        String dir = null;

        int[] v = shortest.get(1);
        int x = v[0];
        int y = v[1];
        if (x == px && y == py) {
            v = shortest.get(2);
            x = v[0];
            y = v[1];
            dir = "wait";
        } else if (x > px) {
            dir = "walk_right";
        } else if (x < px) {
            dir = "walk_left";
        } else if (y > py) {
            dir = "walk_down";
        } else if (y < py) {
            dir = "walk_up";
        }
        return dir;
    }

    /**
     * This method updates the characters position such that it is a smooth transition without jumps. It is
     * called from the move method and should not be called by any other methods. Updates the characters coordinates.
     *
     * @param position animate.getPosition() for the player and getWorldCoordinates() for ai
     */
    private void moveTranslationHelper(Vector position) {
        float sx = position.getX();
        float sy = position.getY();
        float x = 0;
        float y = 0;
        
        float modifier = 1;
        if( getInventoryWeight() >= getMaxInventoryWeight()*0.7){
        	modifier = 0.5f;
        }
        
        if (movesLeft > 0) {
            switch (direction) {
                case "walk_up":
                    x = sx;
                    y = sy - super.getMovementSpeed() * modifier;
                    break;
                case "walk_down":
                    x = sx;
                    y = sy + super.getMovementSpeed() * modifier;
                    break;
                case "walk_left":
                    x = sx - super.getMovementSpeed() * modifier;
                    y = sy;
                    break;
                case "walk_right":
                    x = sx + super.getMovementSpeed() * modifier;
                    y = sy;
                    break;
            }
            movesLeft -= super.getMovementSpeed();
            if (ai) {
                setWorldCoordinates(x, y);
            } else {
                updatePosition(x, y);
            }
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
            pixelX = (ox * dc.tilesize) + dx;        // columns
            pixelY = (oy * dc.tilesize) + dy;        // columns
        } else {
            ox = newx;
            oy = newy;
            pixelX = (ox * dc.tilesize);// + (float) dc.tilesize / 2);
            pixelY = (oy * dc.tilesize);// + (float) dc.tilesize / 2);
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
        if (nearEdge && !ai) {
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
        boolean collided = false;
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
//        if (y < 0 || y > dc.tilesWide || x < 0 || x > dc.tilesHigh) {
//            return true;
//        }
        return dc.map[y][x] != 0;
    }


    private String action;

    /**
     * Updates the animation that is currently in use
     *
     * @param action a new animations action to be selected
     */
    public void updateAnimation(String action) {
        if (action != null) {
            this.action = action;
            Vector sc = animate.getPosition();
            animate = new AnimateEntity(sc.getX(), sc.getY(), getAnimationSpeed(), this.type);
            animate.selectAnimation(action);
        }
    }

    public String getAction() {
        return action;
    }


    /**
     * Translates the entity's screen position and sets the correct world coordinates
     *
     * @param x The new x screen position
     * @param y The new y screen position
     */
    private void updatePosition(float x, float y) {
        // set the position but convert the world coordinates to screen coordinates first
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
        
        //set the world coordinates to the origin times the tile size plus the character's screen coords
    }


}