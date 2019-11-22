package server;/*
 * Multithreaded server.Server Example from GeeksforGeeks.org
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 *
 */

import client.RenderMap;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;


public class Server {

    private ArrayList<PlayerPosition> positions;              // this holds all player's positions
    private int [][] map;                                     // This holds the world map

    public Server() throws IOException{
        // server is listening on port 5000
        ServerSocket ss = new ServerSocket(5000);
        positions = new ArrayList<>(4);
        map = RenderMap.getRandomMap();
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
                ClientHandler t = new ClientHandler(s,dis,dos,s.getPort());

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