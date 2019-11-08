import jig.Vector;

public class Character extends MovingEntity {
    private String type;
    AnimateEntity animations;

    /**
     * Create a new Character (wx, wy)
     * @param wx world coordinates x
     * @param wy world coordinats y
     * @param type 'K'night, 'M'age, 'A'rcher, 'T'ank
     * @param id id for MovingEntity
     */
    public Character(final float wx, final float wy, String type, int id){
        super(wx,wy,id);
        this.type = type;
        setStats();
        animations = new AnimateEntity(wx, wy,100, this.type);
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
}