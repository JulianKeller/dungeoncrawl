package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import client.RenderMap;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.Iterator;

public class Server extends Thread{
    // Static Objects for each thread.
    public static BlockingQueue<String> serverQueue = new ArrayBlockingQueue<>(50);
    public static int [][] map;

    // Keep track of all the clients connected
    private ArrayList<ClientHandler> clients;
    /**
     * Static method getMap, that gets a random map from file.
     */
    private static void getMap() {
        try {
            map = RenderMap.getRandomMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server() {
        clients = new ArrayList<>(4);
    }

    @Override
    public void run() {
        System.out.println("Run Started.");
        while(true){
         sendToClients();
        }
    }

    public void sendToClients(){
            try {
                String playerInfo = serverQueue.take();
//                    if (playerInfo.split(" ")[1].equals("Exit")) {
//                        clients.removeIf(clientHandler ->
//                                clientHandler.getClientId() == Integer.parseInt(playerInfo.split(" ")[0]));
//                    }
                    System.out.println(playerInfo);
                    for (Iterator<ClientHandler> i = clients.iterator(); i.hasNext(); ) {
                        i.next().threadQueue.put(playerInfo);
                        Thread.sleep(10);
                    }
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
    }

    public static void main(String [] args){
        try {
            ServerSocket ss = new ServerSocket(5000);
            getMap();
            Server server = new Server();
            server.start();
            while (true) { ;
                Socket s = ss.accept();
                System.out.println("A new client has connected " + s);
                ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(s.getInputStream());
                ClientHandler t = new ClientHandler(s, is, os);
                t.start();
                server.clients.add(t);

            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }



}
