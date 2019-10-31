import jig.Entity;
import jig.ResourceManager;

import org.newdawn.slick.Animation;


// TODO this class will need to be updated to grab the correct spritesheet for each character
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
        switch (type) {
            case "spell_up": {
                action = new Animation(ResourceManager.getSpriteSheet(Main.KNIGHT_LEATHER, 64, 64), 0, 0, 6, 0, true, speed, true);
                break;
            }
            case "spell_left": {
                action = new Animation(ResourceManager.getSpriteSheet(Main.KNIGHT_LEATHER, 64, 64), 0, 1, 6, 1, true, speed, true);
                break;
            }
            case "spell_down": {
                action = new Animation(ResourceManager.getSpriteSheet(Main.KNIGHT_LEATHER, 64, 64), 0, 2, 6, 2, true, speed, true);
                break;
            }
            case "spell_right": {
                action = new Animation(ResourceManager.getSpriteSheet(Main.KNIGHT_LEATHER, 64, 64), 0, 3, 6, 3, true, speed, true);
                break;
            }
        }
        addAnimation(action);
        action.setLooping(true);
    }



    public boolean isActive() {
        return !action.isStopped();
    }
}
