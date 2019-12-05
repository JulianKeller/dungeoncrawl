package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Server extends Thread{
    // Static Objects for each thread.
    public static BlockingQueue<Msg> serverQueue = new LinkedBlockingQueue<>();
    public static ArrayList<BlockingQueue> clientQueues = new ArrayList<>();
    public static int [][] map;;
    public static ArrayList<String> enemies;

    public Server(){

    }
    @Override
    public void run() {
        while(true){
            sendToClients();

        }
    }

    public void sendToClients(){
            try {
                Msg playerInfo = serverQueue.take();
                for(BlockingQueue c : clientQueues)
                    c.put(playerInfo);

                } catch(InterruptedException e){
                    e.printStackTrace();
                }
    }

    public static void main(String [] args){
        try {
            // Create a new Socket for the server
            ServerSocket ss = new ServerSocket(5000);
            // Generate the map
            map = LoadMap.getRandomMap();

            // TODO generate AI characters
            enemies = AI.spawnEnemies(map, 20);

            // TODO generate items here


            // Start a Server thread that will handle distributing to the client and servers.
            Server server = new Server();
            server.start();
            // This listens for new connections.
            while (true) {
                Socket s = ss.accept();
                System.out.println("A new client has connected " + s);
                ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(s.getInputStream());
                // This is the client handler thread.
                System.out.println("Creating new thread for this client...");
                ClientHandler t = new ClientHandler(s, is, os, new LinkedBlockingQueue<>());
                t.start();

            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }



}
