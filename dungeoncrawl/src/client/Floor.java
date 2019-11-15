package client;

import jig.Entity;
import jig.ResourceManager;

/*
This class creates a client.Floor tile Entity with the correct image
 */
public class Floor extends Entity {

    public Floor(final float x, final float y, String type) {
        super(x, y);
        switch (type) {
            case "normal": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.FLOOR));
                break;
            }
            case "shadow": {
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
            case "iso": {
                addImageWithBoundingBox(ResourceManager.getImage(Main.ISOFLOOR));
                break;
            }
        }
    }

}
