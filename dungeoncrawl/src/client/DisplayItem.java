package client;

import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

/*
This class exists solely for the purpose of displaying an item in the game to see how it looks.
 */
public class DisplayItem extends Entity {
    String type;
    Vector worldCoordinates;

    public DisplayItem(final float x, final float y, String type) {
        super(x, y);
        setWorldCoordinates(x, y);
        this.type = type;
        switch (type) {
            case "health": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.POTION_RED));
                break;
            }
            case "manna": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.POTION_BLUE));
                break;
            }
            case "strength": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.POTION_YELLOW));
                break;
            }
            case "invisibility": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.POTION_PINK));
                break;
            }
            case "fire": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.POTION_ORANGE));
                break;
            }
            // armor
            case "armor_gold": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.ARMOR_GOLD));
                break;
            }
            case "armor_iron": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.ARMOR_IRON));
                break;
            }
            case "sword_wood": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SWORD_WOOD));
                break;
            }
            case "sword_iron": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SWORD_IRON));
                break;
            }
            case "sword_gold": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SWORD_GOLD));
                break;
            }
            // arrows
            case "arrow_normal": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.ARROW_NORMAL));
                break;
            }
            case "arrow_ice": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.ARROW_ICE));
                break;
            }
            case "arrow_poison": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.ARROW_POISON));
                break;
            }
            case "arrow_flame": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.ARROW_FLAME));
                break;
            }
            // staffs
            case "staff_emerald": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.STAFF_EMERALD));
                break;
            }
            case "staff_ameythst": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.STAFF_AMETHYST));
                break;
            }
            case "staff_ruby": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.STAFF_RUBY));
                break;
            }
            case "spell_emerald": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SPELL_GREEN));
                break;
            }
            case "spell_ameythst": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SPELL_PURPLE));
                break;
            }
            case "spell_ruby": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SPELL_RED));
                break;
            }
            //gloves
            case "gloves_red": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.GLOVES_REGENERATION));
                break;
            }
            case "gloves_white": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.GLOVES_REFLECTION));
                break;
            }
            case "gloves_yellow": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.GLOVES_SWIFTNESS));
                break;
            }
        }
    }

    public Vector getWorldCoordinates(){
        return worldCoordinates;
    }
    public void setWorldCoordinates(Vector wc){
        worldCoordinates = wc;
    }
    public void setWorldCoordinates(float x, float y){
        setWorldCoordinates(new Vector(x, y));
    }
}
