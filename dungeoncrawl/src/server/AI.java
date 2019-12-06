package server;

import client.Main;
import jig.Vector;

import java.util.ArrayList;
import java.util.Random;

/*
This class spawns AI players and items locations, these can be sent to the client and then rendered
 */
public class AI {

// TODO copy over the getTileCoordinates method


    /**
     * update the position of the AI player
     * // TODO run dijkstra's
     */
    public static void updatePosition(float wx, float wy) {
        int tilesize = 32;
        int offset = tilesize/2;
        int doubleOffset = offset/2;
        int range = 4;
        String[] moves = {"walk_up", "walk_down", "walk_left", "walk_right", "wait"};
        int rand;

        // run dijkstra
        PathFinding find = new PathFinding(Server.map);
        Vector heroTile = getTileWorldCoordinates(wx, wy);
        find.dijkstra((int) heroTile.getX(), (int) heroTile.getY());

        for (Msg ai : Server.enemies) {
            // if ai close enough pathfind with dijkstra
            if (playerNearby(range, wx, wy, ai.wx, ai.wy)) {
                Vector aiTile = getTileWorldCoordinates(ai.wx, ai.wy);
                ArrayList<int[]> shortest = find.findShortestPath((int) heroTile.getX(), (int) heroTile.getY(), (int) aiTile.getX(), (int) aiTile.getY());
                ai.nextDirection = getNextDirection(shortest, wx, wy);
//                System.out.println("Dijkstra next: " + ai.nextDirection);
            }
            else {
                // else choose random direction
                rand = new Random().nextInt(moves.length);
                ai.nextDirection = moves[rand];
//                System.out.println("Random next: " + ai.nextDirection);
            }
        }
    }

    // get next direction based on Dijkstra shortest path

    /**
     *
     * @param shortest
     * @param wx hero wx
     * @param wy hero wy
     * @return
     */
    public static String getNextDirection(ArrayList<int[]> shortest, float wx, float wy) {
        if (shortest.isEmpty() || shortest.size() <= 2) {
            return null;
        }
        Vector tileCoord = getTileWorldCoordinates(wx, wy);
        int px = (int) tileCoord.getX();
        int py = (int) tileCoord.getY();
        String dir = null;

        int[] v = shortest.get(1);
        int x = v[0];
        int y = v[1];
        if (x == px && y == py) {
            dir = "wait";
        } else if (x > px) {
            dir = "walk_left";
        } else if (x < px) {
            dir = "walk_right";
        } else if (y > py) {
            dir = "walk_up";
        } else if (y < py) {
            dir = "walk_down";
        }
        return dir;
    }


    /**
     * @return Checks if the player is within range of the ai, true if so
     */
    private static boolean playerNearby(int range, float wx, float wy, float awx, float awy) {
        Vector heroWC = getTileWorldCoordinates(wx, wy);
        Vector aiWC = getTileWorldCoordinates(awx, awy);
        if (Math.abs(heroWC.getX() - aiWC.getX()) <= range && (Math.abs(heroWC.getY() - aiWC.getY()) <= range)) {
            return true;
        }
        return false;
    }


//    // TODO pass in the starting coordinates of the player
//    public static void dijkstra(int startX, int startY) {
//        PathFinding find = new PathFinding(Server.map);
//        find.dijkstra(startX, startY);
//        int[][] shortest = find.findShortestPath();
//        next = getNextDirection(dc);
//    }

    /**
     * Get the entities world coordinates in tiles
     * @return
     */
    public static Vector getTileWorldCoordinates(float wx, float wy) {
        int tilesize = 32;
        int offset = tilesize/2;
        int doubleOffset = offset/2;
        float x = Math.round((wx + offset)/tilesize) - 1;
        float y = Math.round((wy + tilesize + doubleOffset)/tilesize) - 1;
        return new Vector(x, y);
    }

    /**
     * Randomly creates coordinates, type, and ID for the AI characters to spawn
     * The result is saved to an arraylist of strings which is then returned
     */
    public static ArrayList<Msg> spawnEnemies(int[][] map, int count) {
        int tilesize = 32;
        int offset = tilesize/2;
        int doubleOffset = offset/2;
        ArrayList<Msg> enemies = new ArrayList<>(count);
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
            int id = (int)System.nanoTime();

            if (id < 0) {
                id  = -id;
            }
            Msg message = new Msg(id, "skeleton_basic",wx,wy,150);   // "x y id"
//            System.out.println("Created "+message.id);
            enemies.add(message);
//            dc.characters.add(new Character(dc, wx, wy, "skeleton_basic", (int) System.nanoTime(), this, true));

            //create a random item at the given position
            count--;
        }
        return enemies;
    }

    public static ArrayList<Msg> spawnDebugEnemies(int[][] map) {
        int tilesize = 32;
        int offset = tilesize/2;
        int doubleOffset = offset/2;
        ArrayList<Msg> enemies = new ArrayList<>(1);
        float wx = (tilesize * 18) - offset;
        float wy = (tilesize * 18) - tilesize - doubleOffset;
        Msg message = new Msg(333, "skeleton_basic",wx,wy,150);   // "x y id"
        enemies.add(message);
        return enemies;
    }
}
