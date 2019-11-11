import jig.Entity;
import jig.ResourceManager;

public class Potion extends Entity {
    String type;

    public Potion(final float x, final float y, String type) {
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
        }
    }


}
