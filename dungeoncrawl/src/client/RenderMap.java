package client;

import jig.Entity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Random;

/*
This class exists for the purpose of rendering the floors and walls of the map.
 */
public class RenderMap extends Entity {

    // grabs a random map and returns it as a 2d array
    public static int[][] getRandomMap() throws IOException {
        File f;
        if( System.getProperty("os.name").toLowerCase().contains("windows")){
        	f = new File("src/maps");
        }else{
        	f = new File("dungeoncrawl/src/maps");
        }
        Random r = new Random();
        int rand = r.nextInt(100);
        String filepath = f.getAbsolutePath() + "/map" + rand + ".txt";
        System.out.println("Loading Map: " + "map" + rand + ".txt");
        return loadMapFromFile(Paths.get(filepath));
    }

    /*
    Returns a small map 32 x 21 for debugging
     */
    public static int[][] getDebugMap(Main dc) {
        return new int[][] {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1},
                {1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        };
    }

    /*
    Based on Streams from: https://stackoverflow.com/questions/22185683/read-txt-file-into-2d-array
    Uses Java's Stream API to convert a text file to a 2d int array
     */
    static public int[][] loadMapFromFile(Path path) throws IOException {
        return Files.lines(path)                        // Read all lines from the filepath
                .map(line -> line.split("\\s"))   // for each line, get an array chars split by spaces
                .map((sa) -> Stream.of(sa)              // convert char array to a sequential ordered stream
                .mapToInt(Integer::parseInt)            // map the char array to an int stream
                .toArray())                             // convert the int stream to an array
                .toArray(int[][]::new);                 // add the array to a 2d array
    }


    // Draw the 2D map to the screen
    public static void setMap(Main dc, Character c) {
        // todo adjust based on bounds of game
        int ox = c.ox;
        int oy = c.oy;
        float dx = c.dx;
        float dy = c.dy;
        int startx = ox;
        int starty = oy;
        int endx = dc.width + ox;
        int endy = dc.height + oy;
        System.out.printf("sx sy: %s, %s\n", startx, starty);
        System.out.printf("ex ey: %s, %s\n", endx, endy);


        // increase the range by 1 if possible
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
        System.out.printf("sx sy: %s, %s\n", startx, starty);
        System.out.printf("ex ey: %s, %s\n\n", endx, endy);

        float x, y;
        dc.mapTiles = new Entity[dc.map.length][dc.map[0].length];      // initialize the mapTiles
        // todo offset the i and j values based on origin offest
        for (int i = starty; i < endy; i++) {
            for (int j = startx; j < endx; j++) {
//                x = (((float) j - (float) ox) * (float) dc.tilesize + (float) dc.tilesize/2);        // columns
//                y = (((float) i - (float) oy) * (float) dc.tilesize + (float) dc.tilesize/2);        // columns
                x = (((float) j - (float) ox) * (float) dc.tilesize + (float) dc.tilesize/2) - dx;        // columns
                y = (((float) i - (float) oy) * (float) dc.tilesize + (float) dc.tilesize/2) - dy;        // columns
                // WALLs
                if (dc.map[i][j] == 1) {
                    if (i+1 >= dc.map.length) {
                        dc.mapTiles[i][j] = new Wall(x, y, "top");
                    }
                    else if (dc.map[i+1][j] == 0) {
                        dc.mapTiles[i][j] = new Wall(x, y, "border");
                    }
                    else if (dc.map[i+1][j] == 1) {
                        dc.mapTiles[i][j] = new Wall(x, y, "top");
                    }
                }
                // FLOORs
                else if (dc.map[i][j] == 0) {
                    if (dc.map[i+1][j] == 1 && dc.map[i][j-1] == 1) {
                        dc.mapTiles[i][j] = new Floor(x, y, "shadow_double");
                    }
                    else if (i+1 < dc.map.length && dc.map[i+1][j] == 1) {
                        dc.mapTiles[i][j] = new Floor(x, y, "shadow");
                    }
                    else if (j-1 >= 0 && dc.map[i][j-1] == 1) {
                        dc.mapTiles[i][j] = new Floor(x, y, "shadow_right");
                    }
                    else if (j-1 > 0 && i+1 < dc.map[i].length && dc.map[i+1][j-1] == 1) {
                        dc.mapTiles[i][j] = new Floor(x, y, "shadow_corner");
                    }
                    else {
                        dc.mapTiles[i][j] = new Floor(x, y, "normal");
                    }
                }
            }
        }
    }

}
