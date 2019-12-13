package server;

import java.util.*;

// an implementation of Dijkstra's algorithm
public class PathFinding {
    public float[][] distance;
    public int[][][] path;
    private boolean[][] visited;
    private ArrayList<Vertex> q;
    private int y;
    private int x;

    public PathFinding(int[][] map) {
        this.y = map.length;
        this.x = map[0].length;
    }

    // runs dijkstra's from the starting point
    public void dijkstra(int startX, int startY) {
        int[][] map = Server.map;
        Vertex min;
        initializeSingleSource(startX, startY);
        int[] current;
        int[] up;
        int[] down;
        int[] left;
        int[] right;
        initializeQueue(startX, startY);
        while (q.size() > 0) {
            min = q.get(0);
            current = new int[]{min.x, min.y};
            q.remove(0);
            up = new int[]{min.x, min.y + 1};
            down = new int[]{min.x, min.y - 1};
            left = new int[]{min.x - 1, min.y};
            right = new int[]{min.x + 1, min.y};
            relax(map, current, up);
            relax(map, current, down);
            relax(map, current, left);
            relax(map, current, right);
        }
    }

    /**
     * Finds the shortest path after dijkstra's has been run between the starting and target points
     *
     * @param startX  x starting tile coordinates
     * @param startY  y starting tile coordinates
     * @param targetX x ending tile coordinates
     * @param targetY y ending tile coordinates
     * @return the shortest path between start and end
     */
    public static ArrayList<int[]> findShortestPath(int[][][] path, int startX, int startY, int targetX, int targetY) {
        Stack<int[]> stack = new Stack<>();
        ArrayList<int[]> shortest = new ArrayList<>();
        int prevx, prevy;
        // start at target, push onto stack, then pop off

        int[] prev = path[targetX][targetY];
        stack.push(prev);

        prevx = targetX;
        prevy = targetY;
        int count = 0;
        while (count < 200) {
            //stop loop early if we found the player
            if (prevx == startX && prevy == startY) {
                break;
            }

            prev = path[prevx][prevy];
            if (prev == null) {
                break;
            }
            stack.push(prev);
            prevx = prev[0];
            prevy = prev[1];
            count++;
        }
        while (!stack.isEmpty()) {
            shortest.add(stack.pop());
        }
//        printShortestPath(shortest);
        return shortest;
    }

    // initialize the priority queue, returns a sorted queue based on distance
    private void initializeQueue(int startX, int startY) {
        q = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                q.add(new Vertex(i, j, startX, startY));
            }
        }
        q.sort(vertexcmp);     // sort the list from small to large
    }

    // Set the priority queue to unvisited, path to null, distances to MAX distance
    private void initializeSingleSource(int startX, int startY) {
        distance = new float[x][y];
        path = new int[x][y][2];
        visited = new boolean[x][y];
        for (int j = 0; j < y; j++) {
            for (int i = 0; i < x; i++) {
                distance[i][j] = Integer.MAX_VALUE;
                path[i][j] = null;
                visited[i][j] = false;
            }
        }
        distance[startX][startY] = 0;
    }

    // current = u, adjacent = v
    private void relax(int[][] map, int[] current, int[] adjacent) {
        int cx = current[0];
        int cy = current[1];
        int ax = adjacent[0];
        int ay = adjacent[1];

        if (!inRange(current) || !inRange(adjacent))
            return;

        int weight = 1;
        if (map[ay][ax] == 1) {
            weight = Integer.MAX_VALUE;
        }

        if (distance[ax][ay] > (distance[cx][cy] + weight)) {
            distance[ax][ay] = distance[cx][cy] + weight;
            path[ax][ay] = current;
        }
    }

    // make sure everything is in range of the map
    private Boolean inRange(int[] vertex) {
        // set to >= 1 because edges of map have infinite walls
        return (vertex[0] < x && vertex[0] >= 0 && vertex[1] < y && vertex[1] >= 0);
    }

    // make sure everything is in range of the map
    private Boolean inRange(int vertexX, int vertexY) {
        // set to >= 1 because edges of map have infinite walls
        return (vertexX < x && vertexX >= 0 && vertexY < y && vertexY >= 0);
    }

    // print the shortest path based on dijkstra
    public static void printShortestPath(ArrayList<int[]> shortest) {
        int x, y;
        for (int[] v : shortest) {
            if (v == null) {
                continue;
            }
            x = v[0];
            y = v[1];
            System.out.printf("[%s, %s] -> ", x, y);
        }
        System.out.println();
    }

    public float[][] getWeights() {
        return distance;
    }

    // comparator for vertices
    public static Comparator<Vertex> vertexcmp = new Comparator<Vertex>() {
        @Override
        public int compare(Vertex vertex, Vertex t1) {
            return vertex.distance - t1.distance;
        }
    };

    // class for creating vertices in use with Dijkstra
    public static class Vertex {
        int distance;
        int x;
        int y;

        private Vertex(int x, int y, int startX, int startY) {
            this.x = x;
            this.y = y;
            this.distance = Math.abs(startX - x) + Math.abs(startY - y);
        }
    }


}

