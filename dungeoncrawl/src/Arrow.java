import jig.Entity;
import jig.ResourceManager;

// draw the dijkstra's path
public class Arrow extends Entity {
    private String current;
    private int count;
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
//    public static void loadPathArrows(Game dtc, AITank tank) {
//        if (tank.shortest.isEmpty()) {
//            return;
//        }
//        int tilesize = dtc.tileW;
//        int offset = tilesize / 2;
//        int x, y;
//        int px = -1, py = -1;
//        String dir = "left";
//
//        for (int[] v : tank.shortest) {
//            if (v == null) {
//                continue;
//            }
//            x = v[0];
//            y = v[1];
//            if (x > px) {
//                dir = "right";
//            }
//            else if (x < px) {
//                dir = "left";
//            }
//            if (y > py) {
//                dir = "down";
//            }
//            else if (y < py) {
//                dir = "up";
//            }
//            tank.arrows.add(new Arrow(x * tilesize + offset, y * tilesize + offset, dir));
//            px = x;
//            py = y;
//        }
//    }

    // remove arrows from the previous run
//    public static void removeArrows(AITank ai) {
//        for (Arrow a : ai.arrows) {
//            if (a != null) {
//                a.removeArrowImage();
//            }
//        }
//    }

    public void removeArrowImage() {
        removeImage(ResourceManager.getImage(current));
    }
}
