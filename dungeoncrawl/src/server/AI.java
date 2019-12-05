package server;

import java.util.ArrayList;
import java.util.Random;

/*
This class spawns AI players and items locations, these can be sent to the client and then rendered
 */
public class AI {
    


    /**
     * Randomly creates coordinates, type, and ID for the AI characters to spawn
     * The result is saved to an arraylist of strings which is then returned
     */
    public static ArrayList<String> spawnEnemies(int[][] map, int count) {
        int tilesize = 32;
        int offset = tilesize/2;
        int doubleOffset = offset/2;
        ArrayList<String> enemies = new ArrayList<>(count);
        int maxcol =  map.length - 2;
        int maxrow = map[0].length - 2;
        Random rand = new Random();
        while( count > 0 ){
            int col = rand.nextInt(maxcol);
            int row = rand.nextInt(maxrow);
            while(row < 2 || col < 2 || map[col][row] == 1){
                col = rand.nextInt(maxcol) - 1;
                row = rand.nextInt(maxrow) - 1;
            }
            float wx = (tilesize * row) - offset;
            float wy = (tilesize * col) - tilesize - doubleOffset;

            // TODO will need to fix the structure of this string for parsing
            String message = (int)System.nanoTime()+" skeleton_basic "+wx + " " + wy;   // "x y id"
//            System.out.println("Created "+message.split(" ")[0]);
            enemies.add(message);
//            dc.characters.add(new Character(dc, wx, wy, "skeleton_basic", (int) System.nanoTime(), this, true));

            //create a random item at the given position
            count--;
        }
        return enemies;
    }
}
