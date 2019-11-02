import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.Animation;

public class Character extends MovingEntity {
    private Animation animation;
    private char type;
    private String sprite;
    private String action;
    private String spritesheet;
    private int ani_speed;
    /**
     * Create a new Character (wx, wy)
     * @param wx world coordinates x
     * @param wy world coordinats y
     * @param sprite String to sprite sheet file
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     */
    public Character(final float wx, final float wy, String sprite, char type){
        super(wx,wy);
        spritesheet = sprite;
        this.type = type;
        addImageWithBoundingBox(ResourceManager.getImage(sprite));
        setStatsandSprites();
    }

    /**
     * Create a new Character (Vector)
     * @param wc world coordinates x
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     */
    public Character(Vector wc, char type) {
        super(wc);
        spritesheet = "";
        this.type = type;
        setStatsandSprites();
    }

    /**
     * Sets Character HP, AP, and Mana based on type given.
     */
    private void setStatsandSprites(){
        switch (type){
            case 'K': // Knight
                setHitPoints(100);
                setArmorPoints(100);
                setSpeed(50);
                spritesheet = Main.KNIGHT_GOLD;
                break;
            case 'M': // Mage
                setHitPoints(80);
                setArmorPoints(50);
                setSpeed(75);
                setMana(100);
                spritesheet = Main.MAGE_IMPROVED;
                break;
            case 'A': // Archer
                setHitPoints(100);
                setArmorPoints(50);
                setSpeed(75);
                spritesheet = Main.ARCHER_LEATHER;
                break;
            case 'T': // Tank
                setHitPoints(150);
                setArmorPoints(100);
                setSpeed(25);
                spritesheet = Main.TANK_GOLD;
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
    public char getType() {
        return type;
    }
}