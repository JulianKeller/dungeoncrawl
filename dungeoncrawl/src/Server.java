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
    private final int [][] map;


    public Server() throws IOException{
        // server is listening on port 5000
        ServerSocket ss = new ServerSocket(5000);
        clients = new ArrayList<LevelServer>(4);
        map = RenderMap.getRandomMap();
        Integer [][] iMap = new Integer[map.length][map[0].length];
        for(int i = 0; i < map.length; i++){
            for(int j=0; j < map[i].length; j++){
                iMap[i][j] = map[i][j];
            }
        }
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
                LevelServer t = new LevelServer(s,dis,dos,clients.size(), iMap);
                clients.add(t);

                // Invoking the start() method
                t.start();
            } catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
    public static void main(String [] args) throws IOException {
        new Server();
    }
}