package client;

import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

// draw the dijkstra's path
public class Arrow extends Entity {
    private String current;
    private int count;
    private Vector worldCoordinates;

    private Arrow(final float x, final float y, String direction) {
        super(x, y);
        String image = Main.ARROW_U;
        count = 0;
        switch (direction) {
            case "up":
                image = Main.ARROW_U;
                break;
            case "down":
                image = Main.ARROW_D;
                break;
            case "left":
                image = Main.ARROW_L;
                break;
            case "right":
                image = Main.ARROW_R;
        }
        current = image;
        addImageWithBoundingBox(ResourceManager.getImage(image));
    }

//    // load the images of the arrows to be drawn on the display
    public static void loadPathArrows(Main dc, Character ai) {
        if (ai.shortest.isEmpty()) {
            return;
        }
        int tilesize = dc.tilesize;
        int offset = tilesize / 2;
        int x, y;
        int px = -1, py = -1;
        String dir = "left";

        for (int[] v : ai.shortest) {
            if (v == null) {
                continue;
            }
            x = v[0];
            y = v[1];
            if (x > px) {
                dir = "right";
            }
            else if (x < px) {
                dir = "left";
            }
            if (y > py) {
                dir = "down";
            }
            else if (y < py) {
                dir = "up";
            }

            Arrow a = new Arrow(x * tilesize + dc.offset, y * tilesize + dc.offset, dir);
            a.worldCoordinates = new Vector(x * tilesize + dc.offset, y * tilesize + dc.offset);
            
//            Arrow a = new Arrow(x * tilesize - dc.offset, y * tilesize - dc.offset, dir);
//            a.worldCoordinates = new Vector(x * tilesize - dc.offset, y * tilesize - dc.offset);
            ai.arrows.add(a);
            px = x;
            py = y;
        }
    }

    public Vector getWorldCoordinates() {
        return worldCoordinates;
    }

    public void setWorldCoordinates(Vector wc) {
        worldCoordinates = wc;
    }

    //     remove arrows from the previous run
    public static void removeArrows(Character ai) {
        for (Arrow a : ai.arrows) {
            if (a != null) {
                a.removeArrowImage();
            }
        }
    }

    public void removeArrowImage() {
        removeImage(ResourceManager.getImage(current));
    }
}
