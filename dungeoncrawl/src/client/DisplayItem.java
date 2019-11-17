package client;

import jig.Entity;
import jig.ResourceManager;

/*
This class exists solely for the purpose of displaying an item in the game to see how it looks.
 */
public class DisplayItem extends Entity {
    String type;

    public DisplayItem(final float x, final float y, String type) {
        super(x, y);
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
        }
    }
}
