package client;

import jig.Entity;
import jig.ResourceManager;

import org.newdawn.slick.Animation;



// TODO most likely each character will contain an instance of this class
// TODO

/**
 * A class representing a characters movement.
 */
public class AnimateEntity extends Entity {
    public Animation animation;
    private String action;
    private String sprite;
    private String spritesheet;
    private int speed;


    /*
    Creates an Animated Entity
    @param x entities starting x position
    @param y entities starting y position
    @param speed The speed the animation plays at, 100 is pretty balanced
    @param sprite The sprite that is being animated
     */
    public AnimateEntity(float x, float y, int speed, String sprite) {
        super(x, y);
        this.speed = speed;
        this.sprite = sprite;
        this.spritesheet = getSpritesheet();
    }

    /*
    Get the correct spritesheet for the specified sprite
    */
    public String getSpritesheet() {
        String spritesheet = null;
        switch (sprite) {
            case "knight_leather": {
                spritesheet = Main.KNIGHT_LEATHER;
                break;
            }
            case "knight_iron": {
                spritesheet = Main.KNIGHT_IRON;
                break;
            }
            case "knight_gold": {
                spritesheet = Main.KNIGHT_GOLD;
                break;
            }
            case "mage_leather": {
                spritesheet = Main.MAGE_LEATHER;
                break;
            }
            case "mage_blue": {
                spritesheet = Main.MAGE_BLUE;
                break;
            }
            case "mage_purple": {
                spritesheet = Main.MAGE_PURPLE;
                break;
            }
            case "archer_leather": {
                spritesheet = Main.ARCHER_LEATHER;
                break;
            }
            case "archer_iron": {
                spritesheet = Main.ARCHER_IRON;
                break;
            }
            case "archer_green": {
                spritesheet = Main.ARCHER_GREEN;
                break;
            }
            case "tank_leather": {
                spritesheet = Main.TANK_LEATHER;
                break;
            }
            case "tank_iron": {
                spritesheet = Main.TANK_IRON;
                break;
            }
            case "tank_gold": {
                spritesheet = Main.TANK_GOLD;
                break;
            }
            case "skeleton_basic": {
                spritesheet = Main.SKELETON_BASIC;
                break;
            }
            case "skeleton_leather": {
                spritesheet = Main.SKELETON_LEATHER;
                break;
            }
            case "skeleton_chain": {
                spritesheet = Main.SKELETON_CHAIN;
                break;
            }
            case "skeleton_boss": {
                spritesheet = Main.SKELETON_BOSS;
                break;
            }
            case "ice_elf": {
                spritesheet = Main.ICE_ELF;
                break;
            }
        }
        return spritesheet;
    }

    /*
     Selects and starts the appropriate animation sequence for the specified sprites action
    */
    public Animation selectAnimation(String action) {
        int row = 0;        // sprite sheet y
        int startx = 0;
        int endx = 0;
        int spritesize = 64;
        switch (action) {
            case "spell_up": {
                row = 0;
                startx = 0;
                endx = 6;
                break;
            }
            case "spell_left": {
                row = 1;
                startx = 0;
                endx = 6;
                break;
            }
            case "spell_down": {
                row = 2;
                startx = 0;
                endx = 6;
                break;
            }
            case "spell_right": {
                row = 3;
                startx = 0;
                endx = 6;
                break;
            }
            case "jab_up": {
                row = 4;
                startx = 0;
                endx = 7;
                break;
            }
            case "jab_left": {
                row = 5;
                startx = 0;
                endx = 7;
                break;
            }
            case "jab_down": {
                row = 6;
                startx = 0;
                endx = 7;
                break;
            }
            case "jab_right": {
                row = 7;
                startx = 0;
                endx = 7;
                break;
            }
            case "walk_up": {
                row = 8;
                startx = 0;
                endx = 8;
                break;
            }
            case "walk_left": {
                row = 9;
                startx = 0;
                endx = 8;
                break;
            }
            case "walk_down": {
                row = 10;
                startx = 0;
                endx = 8;
                break;
            }
            case "walk_right": {
                row = 11;
                startx = 0;
                endx = 8;
                break;
            }
            case "slash_up": {
                row = 12;
                startx = 0;
                endx = 5;
                break;
            }
            case "slash_left": {
                row = 13;
                startx = 0;
                endx = 5;
                break;
            }
            case "slash_down": {
                row = 14;
                startx = 0;
                endx = 5;
                break;
            }
            case "slash_right": {
                row = 15;
                startx = 0;
                endx = 5;
                break;
            }
            case "shoot_up": {
                row = 16;
                startx = 0;
                endx = 12;
                break;
            }
            case "shoot_left": {
                row = 17;
                startx = 0;
                endx = 12;
                break;
            }
            case "shoot_down": {
                row = 18;
                startx = 0;
                endx = 12;
                break;
            }
            case "shoot_right": {
                row = 19;
                startx = 0;
                endx = 12;
                break;
            }
            case "die": {
                row = 20;
                startx = 0;
                endx = 5;
                break;
            }
        }
        if (animation != null) {
            removeAnimation(animation);
            animation = null;
        }
        animation = new Animation(ResourceManager.getSpriteSheet(spritesheet, spritesize, spritesize), startx, row, endx, row, true, speed, true);
        addAnimation(animation);
        return animation;
    }

    // stop the animation
    public void stop() {
        animation.stop();
    }

    // resume the animation
    public void start() {
        animation.start();
    }

    // check if the animation is active
    public boolean isActive() {
        return !animation.isStopped();
    }

    @Override
    public float getX() {
        return super.getX();
    }

    @Override
    public float getY() {
        return super.getY() + 24;
    }

}
