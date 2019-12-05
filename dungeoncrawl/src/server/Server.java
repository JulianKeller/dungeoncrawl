package server;

import client.Character;
import client.Item;
import client.Main;
import jig.Vector;
import org.newdawn.slick.SlickException;
import java.util.Random;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Server extends Thread{
    // Static Objects for each thread.
    public static BlockingQueue<Msg> serverQueue = new LinkedBlockingQueue<>();
    public static ArrayList<BlockingQueue> clientQueues = new ArrayList<>();
    public static int [][] map;
    public static int [][] rotatedMap;
    public static ArrayList<Msg> enemies;
    public static ArrayList<Item> worldItems = new ArrayList<>();

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
            rotatedMap = rotateMap(map);
            // TODO generate AI characters
            enemies = Spawn.spawnEnemies(map, 20);

            // TODO generate items here
            try {
                plantItem(20, 5,rotatedMap, map.length-2, map[0].length-2);
            }catch(SlickException e){
                e.printStackTrace();
            }


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

    public static int [][] rotateMap(int [][] map){
        int [][] rotated = new int[map[0].length][map.length];
        for( int i = 0; i < map.length; i++ ){
            for( int j = 0; j < map[i].length; j++ ){
                //g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
                //i is y, j is x
                rotated[j][i] = map[i][j];
            }
        }
        return rotated;
    }
    public static void plantItem(int numItems, int uniquePotions, int[][] map, int maxcol, int maxrow) throws SlickException {
        Random rand = new Random();
        rand.setSeed(System.nanoTime());
        String [] identifiedPotionEffects = new String[5];
        for( int i = 0; i < uniquePotions; i++ ){
            String effect = Main.PotionEffects[ rand.nextInt(Main.PotionEffects.length)];
            for( int j = 0; j < identifiedPotionEffects.length; j++ ){
                if( identifiedPotionEffects[j] != null && identifiedPotionEffects[j].equals(effect) ){
                    i--;
                    break;
                }
                if( j == identifiedPotionEffects.length-1 ){
                    identifiedPotionEffects[i] = effect;
                }
            }

        }
        int currentItemID = 0;
        //int[][] potionAt = new int[game.map.length][game.map.length];


        while( numItems > 0 ){

            int col = rand.nextInt(maxcol);
            int row = rand.nextInt(maxrow);
            //Vector wc = new Vector( rand.nextInt(maxx), rand.nextInt(maxy) );
            while( map[row][col] == 1 ){
                col = rand.nextInt(maxcol);
                row = rand.nextInt(maxrow);
            }
            Vector wc = new Vector( row, col );

            //create a random item at the given position
            Item i = new Item(wc, false, currentItemID, 0);

            if( i.getType().equals("Potion") || i.getType().equals("Arrow") ){
                i.setRequiredLevel(0);
            }
//            else{
//                //set the item's required level to the average level of the team
//                int levelSum = 0;
//                int numCharacters = 0;
//                for( Character ch : game.characters ){
//                    if( !ch.ai ){
//                        numCharacters++;
//                        levelSum += ch.getStrength();
//                    }
//                }
//                if( numCharacters == 0 ){
//                    throw new SlickException("Load the list of characters before planting items.");
//                }
//                int levelAverage = levelSum / numCharacters;
//
//                int r = rand.nextInt(3);
//                i .setRequiredLevel(levelAverage + r);
//            }


            //check if the item is a potion and set its effect
            //0: blue, 1: orange, 2: pink, 3: red, 4: yellow
            if( i.getType().equals("Potion") ){
                if( i.getMaterial().equals("Blue") ){
                    //use the stored effect
                    i.setEffect(identifiedPotionEffects[0]);
                }else if( i.getMaterial().equals("Orange")){
                    //use the stored effect
                    i.setEffect(identifiedPotionEffects[1]);
                }else if( i.getMaterial().equals("Pink")){
                    //use the stored effect
                    i.setEffect(identifiedPotionEffects[2]);
                }else if( i.getMaterial().equals("Red")){
                    //use the stored effect
                    i.setEffect(identifiedPotionEffects[3]);
                }else if( i.getMaterial().equals("Yellow") ){
                    //use the stored effect
                    i.setEffect(identifiedPotionEffects[4]);
                }else{
                    //this would suggest that the list of potion colors in this class
                    //  is incomplete
                    throw new SlickException("Error: invalid potion color "+i.getMaterial());
                }
            }
            worldItems.add(i);

            currentItemID++;

            numItems--;
        }
    }
}
