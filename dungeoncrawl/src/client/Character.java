package client;

import java.util.ArrayList;
import java.util.Random;

import org.newdawn.slick.Animation;
import org.newdawn.slick.SlickException;

import jig.Vector;

public class Character extends MovingEntity {
    private Main dc;
    AnimateEntity animate;
    private String type;
    private String direction;
    private String currentAction;
    private boolean canMove = true;
    private boolean nearEdge = false;
    public boolean ai;
    private int movesLeft;
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
    private int attackTimer = 0;
    PathFinding find;


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

//        System.out.println("Character added at: " + getWorldCoordinates());
        this.dc = dc;
        this.type = type;
        setStats();
        animate = new AnimateEntity(wx, wy, getAnimationSpeed(), this.type);
        direction = "walk_down";
        currentAction = direction;
        animate.selectAnimation(direction);
        animate.stop();
        ox = 0;
        oy = 0;
        pixelX = 0f;
        pixelY = 0f;
//        setAnimationSpeed(50);       // speed of the animation
        ai = AI;            //
        shortest = new ArrayList<>();
        arrows = new ArrayList<>();
        range = 10;
    }

    public Vector getOrigin() {
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
    
    //entity for rendering animations
    public VFXEntity vfx = null;
    
    public void addVisualEffects() throws SlickException{
    	//go through list of active effect and add visual effects
    	if( vfx == null ){
    		//make a new VFXEnt at the animation position
    		System.out.println("Creating new VFXEntity at " + animate.getPosition().toString());
    		vfx = new VFXEntity(animate.getX(), animate.getY());
    	}
    	for( Effect e : super.getActiveEffects() ){
    		Animation ani = null;
    		if( e.name.equals("Healing") || e.name.equals("Regeneration") ){
    			ani = getFloatingPlusSigns("red", 8, 125);
    		}else if( e.name.equals("Strength") ){
    			ani = getFloatingPlusSigns("green", 8, 125);
    		}else if( e.name.equals("Mana") ){
    			ani = getFloatingPlusSigns("blue", 8, 125);
    		}else if( e.name.equals("Iron Skin") ){
    			ani = getFloatingPlusSigns("gray", 8, 125);
    		}else if( e.name.equals("Might") ){
    			ani = getFloatingPlusSigns("yellow", 8, 125);
    		}else if( e.name.equals("Swiftness") ){
    			ani = getFloatingPlusSigns("white", 8, 125);
    		}else if( e.name.equals("Flame") ){
    			ani = getParticles("orange", 8, 125);
    		}else if( e.name.equals("Poison") ){
    			ani = getParticles("green", 8, 125);
    		}else if( e.name.equals("Fright") ){
    			ani = getGhostFaces(8, 125);
    		}else if( e.name.equals("Stench") ){
    			ani = getFlies(8, 125);
    		}else if( e.name.equals("Thorns") ){
    			ani = getThorns(8, 125);
    		}else if( e.name.equals("Reflection") ){
    			ani = getReflect(8, 125);
    		}else{
    			throw new SlickException("Invalid effect name '" + e.name + "'.");
    		}
    		
    		//add the animation to this character's vfxentity
    		vfx.addVisualEffect(ani, "healing");
    		

    	}
    	
		//remove any single effects
		removeSingleEffects();
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
                setMaxMana(100);
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
                setAttackDamage(1);
                setAttackSpeed(300);
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
            movesLeft = dc.tilesize;
            setNextTileWorldCoordinates(movement);
            if (!movement.equals(direction)) {
                updateAnimation(movement);
                direction = movement;
            } else {
                animate.start();
            }
            canMove = false;
            // if collisions are on
            if (dc.collisions) {
                // if no wall collisions and no character collisions, then player can move again
                if (wallCollision() || characterCollision()) {
                    canMove = true;
                    return;
                }
            }
            changeOrigin();     // check if the screen origin needs to change
        }
    }


    /**
     * Move the AI randomly until the character is within range
     * This is the method that should be called from the level class to move the AI
     */
    public void moveAI(int delta) {
        String[] moves = {"walk_up", "walk_down", "walk_left", "walk_right", "wait"};
        String next = null;
        String currentDirection = direction;

        // moved the character fixed to the grid
        if (!canMove) {
            // update the animation to walking
            if (!direction.equals(currentAction)) {
                currentAction = direction;
                updateAnimation(direction);
                animate.start();
            }
            moveTranslationHelper(getWorldCoordinates());
            return;
        }

        // turn towards player and attack if within 1 tile
        if (canAttackPlayer()) {
            String action = "jab_" + direction.substring("walk_".length());
            // update the animation to jabbing
            if (!currentAction.equals(action)) {
                updateAnimation(action);
                animate.start();
            }
            if (attackTimer <= 0 && !dc.invincible) {
                dc.hero.takeDamage(getAttackDamage(), "", false);
                attackTimer = getAttackSpeed();

                // thorns effect, AI takes 50% damage delt
                if (dc.hero.isThorny() || dc.hero.isReflecting()) {
                    takeDamage((float) getAttackDamage()/2, "", false);
                }
            }
            else {
                attackTimer -= delta;
            }
            return;
        }

        // run dijkstra's so enemies attack the player if the player is in range and not invisible
        if (playerNearby(range) && !dc.hero.isInvisible()) {
            Vector heroWC = dc.hero.getTileWorldCoordinates();

            // if the player has the stench effect there is a 30% chance the AI will pathfind to the wrong coordinates
            if (dc.hero.isStinky() || dc.hero.isFrightening()) {
                Random rand = new Random();
                int value = rand.nextInt(100);
                int chance = 50;    // isFrigtening chance 50%
                if (dc.hero.isStinky()) {
                    chance = 30;    // stinky chance 30%
                }
                if (value <= chance) {
                    switch (dc.hero.direction) {
                        case "walk_up":
                            heroWC = new Vector(dc.hero.getTileWorldCoordinates().getX(), dc.hero.getTileWorldCoordinates().getY() + 10);
                            break;
                        case "walk_down":
                            heroWC = new Vector(dc.hero.getTileWorldCoordinates().getX(), dc.hero.getTileWorldCoordinates().getY() - 10);
                            break;
                        case "walk_left":
                            heroWC = new Vector(dc.hero.getTileWorldCoordinates().getX() + 10, dc.hero.getTileWorldCoordinates().getY());
                            break;
                        case "walk_right":
                            heroWC = new Vector(dc.hero.getTileWorldCoordinates().getX() - 10, dc.hero.getTileWorldCoordinates().getY());
                            break;
                    }
                }
            }

            find = new PathFinding(dc, getTileWorldCoordinates(), heroWC);
            int startX = (int) getTileWorldCoordinates().getX();
            int startY = (int) getTileWorldCoordinates().getY();
            shortest = find.dijkstra(dc, startX, startY);
            next = getNextDirection(dc);
        }

        // load arrows for dijkstra's debugging
        if (dc.showPath) {
            Arrow.removeArrows(this);
            Arrow.loadPathArrows(dc, this);
            if (find != null) {
                weights = find.getWeights();
            }
        }
        else {
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
        setNextTileWorldCoordinates(movement);
        movesLeft = dc.tilesize;
        if (!movement.equals(currentDirection)) {
            updateAnimation(movement);
            direction = movement;
        } else {
            animate.start();
        }
        canMove = false;
        if (dc.collisions) {
            // if no wall collisions and no character collisions, then player can move again
            if (wallCollision() || characterCollision()) {
                canMove = true;
                return;
            }
        }
        changeOrigin();     // check if the screen origin needs to change
    }


    /**
     * @return Checks if the player is within range of the ai, true if so
     */
    private boolean playerNearby(int range) {
        Vector heroWC = dc.hero.getTileWorldCoordinates();
        Vector aiWC = getTileWorldCoordinates();
        if (Math.abs(heroWC.getX() - aiWC.getX()) <= range && (Math.abs(heroWC.getY() - aiWC.getY()) <= range)) {
            return true;
        }
        return false;
    }

    /*
    If a player within 1 tile, set direction and return true
     */
    private boolean canAttackPlayer() {
        // player position
        int px = (int) getTileWorldCoordinates().getX();
        int py = (int) getTileWorldCoordinates().getY();
        boolean canAttack = false;

        // character position
        int cx;
        int cy;
        for (Character ch : dc.characters) {
            if (ch.ai || ch.equals(this)) {     // ignore ai players
                continue;
            }

            cx = (int) ch.getTileWorldCoordinates().getX();
            cy = (int) ch.getTileWorldCoordinates().getY();

            // player is above
            if (cy == py - 1 && cx == px) {
                direction = "walk_up";
                canAttack = true;
            }
            else if (cy == py + 1 && cx == px) {
//                canMove = false;
                direction = "walk_down";
                canAttack = true;
            }
            else if (cy == py && cx == px - 1) {
//                canMove = false;
                direction = "walk_left";
                canAttack = true;
            }
            else if (cy == py && cx == px + 1) {
//                canMove = false;
                direction = "walk_right";
                canAttack = true;
            }
        }
        return canAttack;
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
    private boolean wallCollision() {
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
        try {
            return dc.map[y][x] == 1;
        }
        catch (Exception e) {
            return false;
        }
    }

    /*
    Checks for character vs character collisions
    returns true if there is a collision
     */
    private boolean characterCollision() {
        int x = (int) getTileWorldCoordinates().getX();
        int y = (int) getTileWorldCoordinates().getY();
        int chX;
        int chY;
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
        for (Character ch : dc.characters) {
            if (ch.equals(this)) {
                continue;
            }
            chX = (int) ch.getTileWorldCoordinates().getX();
            chY = (int) ch.getTileWorldCoordinates().getY();
            // if character pos == playeres next position
            if (chX == x && chY == y) {
                return true;
            }
            if ((getNextTileWorldCoordinates().getX() == ch.getNextTileWorldCoordinates().getX() &&
                    getNextTileWorldCoordinates().getY() == ch.getNextTileWorldCoordinates().getY())) {
                return true;
            }
            // else if coordinates in x and y are less than 32 pixels apart, expect collision
            float diffX = Math.abs(getWorldCoordinates().getX() - ch.getWorldCoordinates().getX());
            float diffY = Math.abs(getWorldCoordinates().getY() - ch.getWorldCoordinates().getY());
            if (diffX < 32 && diffY < 32) {
                return true;
            }
        }
        return false;
    }


    private String action;

    /**
     * Updates the animation that is currently in use
     *
     * @param action a new animations action to be selected
     */
    public void updateAnimation(String action) {
//        System.out.println("Setting animation to: " + action);
        if (action != null) {
            currentAction = action;
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
    }


}