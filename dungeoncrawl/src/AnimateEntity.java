import jig.Entity;
import jig.ResourceManager;

import org.newdawn.slick.Animation;


// TODO this class will need to be updated to grab the correct spritesheet for each character
//  most likely each character will contain an instance of this class
/**
 * A class representing a transient explosion. The game should monitor
 * explosions to determine when they are no longer active and remove/hide
 * them at that point.
 */
class AnimateEntity extends Entity {
    private Animation action;
    private String type;
    private int speed;

    public AnimateEntity(final float x, final float y, String type, int speed) {
        super(x, y);
        this.type = type;
        this.speed = speed;
        int row = 0;        // sprite sheet y
        int startx = 0;
        int endx = 0;
        int spritesize = 64;
        switch (type) {
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
        action = new Animation(ResourceManager.getSpriteSheet(Main.KNIGHT_LEATHER, spritesize, spritesize), startx, row, endx, row, true, speed, true);
        addAnimation(action);
        action.setLooping(true);
    }

    // stop the animation
    public void stop() {
        action.stop();
    }

    // resume the animation
    public void start() {
        action.start();
    }

    // check if the animation is active
    public boolean isActive() {
        return !action.isStopped();
    }
}
