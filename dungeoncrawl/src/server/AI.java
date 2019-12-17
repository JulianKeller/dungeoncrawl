package server;

import jig.Vector;

import java.util.ArrayList;
import java.util.Random;

/*
This class spawns AI players and items locations, these can be sent to the client and then rendered
 */
public class AI {
    private boolean debug = false;

    /**
     * run dijkstra's algorithm and gets the next direction the AI should move
     * @return the list of weights for the map from dijkstra
     * // TODO test this
     */
    public static void getDijkstraWeights(Msg hero) {
        int range = 8;
        String[] moves = {"walk_up", "walk_down", "walk_left", "walk_right", "wait"};
        int rand;
        int value = 100;
        int chance = 0;
        ArrayList<int[]> shortest = null;

        // run dijkstra
        PathFinding find = new PathFinding(Server.map);
        Vector heroTile = getTileWorldCoordinates(hero.wx, hero.wy);
        find.dijkstra((int) heroTile.getX(), (int) heroTile.getY());
        hero.dijkstraWeights = find.getWeights();
        hero.path = find.path;
    }


    public static void updatePosition(Msg ai, Msg hero) {
        int range = 8;
        String[] moves = {"walk_up", "walk_down", "walk_left", "walk_right", "wait"};
        int rand;
        int value = 100;
        int chance = 0;
        ArrayList<int[]> shortest = null;

        // if ai close enough pathfind with dijkstra and player is not invisible
        if (hero.path != null && playerNearby(range, hero.wx, hero.wy, ai.wx, ai.wy) && !hero.invisible) {
            // apply effects from stinky and frightening hero
            if (hero.stinky || hero.frightening) {
                Random random = new Random();
                value = random.nextInt(100);
                chance = 50;    // isFrigtening chance 50%
                if (hero.stinky) {
                    chance = 30;    // stinky chance 30%
                }
            }

            // run dijkstra's algorithm
            Vector aiTile = getTileWorldCoordinates(ai.wx, ai.wy);
            Vector heroTile = getTileWorldCoordinates(hero.wx, hero.wy);
            shortest = PathFinding.findShortestPath(hero.path, (int) heroTile.getX(), (int) heroTile.getY(), (int) aiTile.getX(), (int) aiTile.getY());
            ai.nextDirection = getNextDirection(shortest, ai.wx, ai.wy);

            // reverse ai direction if effect takes place
            if (value <= chance && ai.nextDirection != null) {
                switch (ai.nextDirection) {
                    case "walk_up":
                        ai.nextDirection = "walk_down";
                        break;
                    case "walk_down":
                        ai.nextDirection = "walk_up";
                        break;
                    case "walk_left":
                        ai.nextDirection = "walk_right";
                        break;
                    case "walk_right":
                        ai.nextDirection = "walk_left";
                        break;
                }
            }
        }

        // otherwise if no dijkstra, choose a random direction to move
        if (shortest == null || shortest.size() <= 2) {
            rand = new Random().nextInt(moves.length);
            ai.nextDirection = moves[rand];
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

        int[] v = shortest.get(shortest.size() - 1);
        int x = v[0];
        int y = v[1];
        if (x == px && y == py) {
            dir = "wait";
        } else if (x > px) {
            dir = "walk_right";
        } else if (x < px) {
            dir = "walk_left";
        } else if (y > py) {
            dir = "walk_down";
        } else if (y < py) {
            dir = "walk_up";
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
        Msg message;
        int id = 0;
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
            if (count == 1) {
                message = new Msg(id, "skeleton_boss",wx,wy,250, true, 1);   // "x y id"
            }
            else {
                message = new Msg(id, "skeleton_basic", wx, wy, 150, true, 1);   // "x y id"
            }
//            if (debug) System.out.printf("placing skeleton at: %s, %s\n", wx, wy);
            message.tilex = row;
            message.tiley = col;
            enemies.add(message);
            count--;
            id++;
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
        Msg message = new Msg(333, "skeleton_basic",wx,wy,150, true, 1);   // "x y id"
        enemies.add(message);
        return enemies;
    }

    /*
Id should be the size of Server.enemies + 1
 */
    public static void spawnBoss(int[][] map) {
        int tilesize = 32;
        int offset = tilesize/2;
        int doubleOffset = offset/2;
        int maxcol =  40;
        int maxrow = 23;
        Random rand = new Random();
        int col = rand.nextInt(maxcol);
        int row = rand.nextInt(maxrow);
        while(row < 2 || col < 2 || map[col][row] == 1){
            col = rand.nextInt(maxcol) - 1;
            row = rand.nextInt(maxrow) - 1;
        }

        row = 5;
        col = 5;
        float wx = (tilesize * row) - offset;
        float wy = (tilesize * col) - tilesize - doubleOffset;
        Server.enemies.add(new Msg(Server.enemies.size(), "skeleton_boss", wx, wy, 250, true, 1));
        System.out.printf("Spawning Boss at: %s, %s\n", row, col);
    }
}
