package client;

import jig.Entity;
import jig.ResourceManager;

/*
This class creates a client.Floor tile Entity with the correct image
 */
public class BaseMap extends Entity {

    public BaseMap(final float x, final float y, String type) {
        super(x, y);
        switch (type) {
            case "wall_top": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.WALL_TOP));
                break;
            }
            case "wall_border": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.WALL));
                break;
            }
            case "floor": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.FLOOR));
                break;
            }
            case "shadow_floor": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SHADOW_FLOOR));
                break;
            }
            case "shadow_right": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SHADOW_FLOOR_R));
                break;
            }
            case "shadow_corner": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SHADOW_FLOOR_CORNER));
                break;
            }
            case "shadow_double": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.SHADOW_FLOOR_DOUBLE_CORNER));
                break;
            }
        }

    }

}
