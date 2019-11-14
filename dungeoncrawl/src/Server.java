/*
 * Multithreaded Server Example from GeeksforGeeks.org
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 *
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class Server {
    // setting up server class to be starting up in Main.java
    public static ArrayList<LevelServer> clients;
    public static int [][] map;
    public static final int tilesize = 32;
    public static final int offset = tilesize/2;
    public static final int doubleOffset = offset/2;
    public static final int xOffset = tilesize - doubleOffset;
    public static final int yOffset = tilesize + doubleOffset/2;

    public Server() throws IOException{
        // server is listening on port 5000
        ServerSocket ss = new ServerSocket(5000);
        clients = new ArrayList<LevelServer>(4);
        map = RenderMap.getRandomMap();
        Integer [][] iMap = convertMap();
        // infinite loop for getting client request
        while(true){
            Socket s = null;
            try {
                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("A new client is connected: " + s);

                // obtaining input and out streams
                ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream dis = new ObjectInputStream(s.getInputStream());


                System.out.println("Assigning new thread for this client");

                // Create a new thread object
                LevelServer t = new LevelServer(s,dis,dos,s.getPort(), iMap);
                clients.add(t);

                // Invoking the start() method
                t.start();
            } catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }

    /**
     * Converts the map to an Integer object for transport
     * @return newMap
     */
    private Integer [][] convertMap(){
        Integer [][] newMap = new Integer[map.length][map[0].length];
        for(int i = 0; i < map.length; i++){
            for(int j=0; j < map[i].length; j++){
                newMap[i][j] = map[i][j];
            }
        }
        return newMap;
    }
    public static void main(String [] args) throws IOException {
        new Server();
    }
}