package client;

import jig.Entity;
import jig.ResourceManager;

/*
This class creates a client.Floor tile Entity with the correct image
 */
public class Wall extends Entity {

    public Wall(final float x, final float y, String type) {
        super(x, y);
        switch (type) {
            case "top": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.WALL_TOP));
                break;
            }
            case "border": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.WALL));
                break;
            }
            case "shadow": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SHADOW_FLOOR));
                break;
            }
            case "iso": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.ISOWALL));
                break;
            }
        }

    }

}
