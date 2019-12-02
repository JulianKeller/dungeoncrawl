package client;

import jig.Entity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.Random;

/*
This class exists for the purpose of rendering the floors and walls of the map.
 */
public class RenderMap extends Entity {

    /**
    Creates tiles in the correct x, y coordinates to be rendered
    @param dc the Main game class
    @param c an instance of the character class
    */
    public static void setMap(Main dc, Character c) {
        float x = 0, y = 0;
        int i = 0, j = 0;
        int ox = c.ox;
        int oy = c.oy;
        float dx = c.dx;
        float dy = c.dy;
        int startx = ox;
        int starty = oy;
        int endx = dc.tilesWide + ox;
        int endy = dc.tilesHigh + oy;

        // increase the range rendered by 1 if possible, prevents black edges on scrolling
        if (startx > 0) {
            startx--;
        }
        if (starty > 0) {
            starty--;
        }
        if (endx < dc.mapWidth) {
            endx++;
        }
        if (endy < dc.mapHeight) {
            endy++;
        }

        // generate the correct wall, floor, shadow tiles in the x, y coordinates
        dc.maptiles = new ArrayList<>((dc.mapHeight+1)*(dc.mapWidth+1));
        try {
            for (i = starty; i < endy && i < dc.mapHeight; i++) {
                for (j = startx; j < endx && j < dc.mapWidth; j++) {
                    x = ((j - ox) * dc.tilesize + (float) dc.tilesize / 2) - dx;        // columns
                    y = ((i - oy) * dc.tilesize + (float) dc.tilesize / 2) - dy;        // columns
                    // WALLs
                    if (dc.map[i][j] == 1) {
                        if (i + 1 >= dc.map.length) {
                            dc.maptiles.add(new BaseMap(x, y, "wall_top"));
                        } else if (dc.map[i + 1][j] == 0) {
                            dc.maptiles.add(new BaseMap(x, y, "wall_border"));
                        } else if (dc.map[i + 1][j] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "wall_top"));
                        }
                    }
                    // FLOORs
                    else if (dc.map[i][j] == 0) {
                        if (dc.map[i + 1][j] == 1 && dc.map[i][j - 1] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "shadow_double"));
                        } else if (i + 1 < dc.map.length && dc.map[i + 1][j] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "shadow_floor"));
                        } else if (j - 1 >= 0 && dc.map[i][j - 1] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "shadow_right"));
                        } else if (j - 1 > 0 && i + 1 < dc.map[i].length && dc.map[i + 1][j - 1] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "shadow_corner"));
                        } else {
                            dc.maptiles.add(new BaseMap(x, y, "floor"));
                        }
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.printf("[i, j] = [%s, %s]\t<x, y> = <%s, %s>\n", i, j, x, y);

        }
    }

    public static void setMap(Main dc) {
        float x = 0, y = 0;
        int i = 0, j = 0;
        int ox = c.ox;
        int oy = c.oy;
        float dx = c.dx;
        float dy = c.dy;
        int startx = ox;
        int starty = oy;
        int endx = dc.tilesWide + ox;
        int endy = dc.tilesHigh + oy;

        // increase the range rendered by 1 if possible, prevents black edges on scrolling
        if (startx > 0) {
            startx--;
        }
        if (starty > 0) {
            starty--;
        }
        if (endx < dc.mapWidth) {
            endx++;
        }
        if (endy < dc.mapHeight) {
            endy++;
        }

        // generate the correct wall, floor, shadow tiles in the x, y coordinates
        dc.maptiles = new ArrayList<>((dc.mapHeight+1)*(dc.mapWidth+1));
        try {
            for (i = starty; i < endy && i < dc.mapHeight; i++) {
                for (j = startx; j < endx && j < dc.mapWidth; j++) {
                    x = ((j - ox) * dc.tilesize + (float) dc.tilesize / 2) - dx;        // columns
                    y = ((i - oy) * dc.tilesize + (float) dc.tilesize / 2) - dy;        // columns
                    // WALLs
                    if (dc.map[i][j] == 1) {
                        if (i + 1 >= dc.map.length) {
                            dc.maptiles.add(new BaseMap(x, y, "wall_top"));
                        } else if (dc.map[i + 1][j] == 0) {
                            dc.maptiles.add(new BaseMap(x, y, "wall_border"));
                        } else if (dc.map[i + 1][j] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "wall_top"));
                        }
                    }
                    // FLOORs
                    else if (dc.map[i][j] == 0) {
                        if (dc.map[i + 1][j] == 1 && dc.map[i][j - 1] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "shadow_double"));
                        } else if (i + 1 < dc.map.length && dc.map[i + 1][j] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "shadow_floor"));
                        } else if (j - 1 >= 0 && dc.map[i][j - 1] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "shadow_right"));
                        } else if (j - 1 > 0 && i + 1 < dc.map[i].length && dc.map[i + 1][j - 1] == 1) {
                            dc.maptiles.add(new BaseMap(x, y, "shadow_corner"));
                        } else {
                            dc.maptiles.add(new BaseMap(x, y, "floor"));
                        }
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.printf("[i, j] = [%s, %s]\t<x, y> = <%s, %s>\n", i, j, x, y);

        }
    }

}
