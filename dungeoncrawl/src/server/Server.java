package server;

import client.Main;
import jig.Entity;
import jig.Vector;
import client.Item;
import org.newdawn.slick.SlickException;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Server extends Thread {
    // Static Objects for each thread.
    public static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    public static int [][] map;
    public static int [][] rotatedMap;
    public static List<Msg> enemies = Collections.synchronizedList(new ArrayList<>());
    public static List<ItemMsg> worldItems = Collections.synchronizedList(new ArrayList<>());
    public static List<Msg> characters = Collections.synchronizedList(new ArrayList<>());
    private boolean debug = false;
    private static int currentItemId = 0;

    public Server() {
    }

    @Override
    public void run() {

        while (true) {
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

                // calculate dijkstra's weights for each active player
                synchronized (characters) {
                    for (Msg c : characters) {
                        AI.getDijkstraWeights(c);       // updates clients weights and paths
                        if (debug) System.out.println("Ran Dijkstra on player: " + c.id);
                    }
                }

                // for each enemy find the closest player to them,
                // use closestPlayer.dijkstraWeights to determine path to player
                synchronized (enemies) {
                    float closest = 10000;
                    for (Msg ai : enemies) {
                        float distance = 0;
                        synchronized (characters) {  // TODO change to list of players
                            for (Msg hero : characters) {
                                distance = (Math.abs(ai.wx - hero.wx) + Math.abs(ai.wy - hero.wy));
                                if (distance < closest) {
                                    AI.updatePosition(ai, hero);    // then ai.get next direction
                                }
                            }
                        }
                        if (debug) System.out.printf("AI %s next direction: %s\n", ai.id, ai.nextDirection);
                    }
                }

                // update all clients with latest details of other players and enemies
                putCharactersInClientQueues();
                putEnemiesInClientQueues();
            }
    }


    /**
     * Put the list of characters into the queue for the thread to send to it's client
     */
    public void putCharactersInClientQueues() {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (debug) System.out.println("putCharactersinClientQueues() -> queue " + c.getId());
                    // put characters
                    synchronized (characters) {
                        for (Msg chars : characters) {
                            if (chars.id == c.getClientId()) {
                                continue;
                            }
                            try {
                                c.characterQueue.put(chars);
                                if (debug) System.out.printf("Putting %s into queue\n", chars);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (debug) System.out.println();
                    }
            }
        }
    }


    /**
     * Put the list of characters into the queue for the thread to send to it's client
     */
    public void putEnemiesInClientQueues() {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (debug) System.out.println("putEnemiesInClientQueues() -> queue " + c.getId());
                // put characters
                synchronized (enemies) {
                    for (Msg ai : enemies) {
                        try {
                            c.enemyQueue.put(ai);
                            if (debug) System.out.printf("Putting %s into queue\n", ai);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (debug) System.out.println();
                }
                synchronized (c.pauseObject) {
                    c.pauseObject.notifyAll();
                }
            }
        }
    }


    public static int[][] rotateMap(int[][] map) {
        int[][] rotated = new int[map[0].length][map.length];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                //g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
                //i is y, j is x
                rotated[j][i] = map[i][j];
            }
        }
        return rotated;
    }

    public static void plantItem(int numItems, int uniquePotions, int[][] map, int maxcol, int maxrow) throws SlickException {
        Entity.setCoarseGrainedCollisionBoundary(Entity.AABB);
        Random rand = new Random();
        rand.setSeed(System.nanoTime());
        String[] identifiedPotionEffects = new String[5];
        for (int i = 0; i < uniquePotions; i++) {
            String effect = Main.PotionEffects[rand.nextInt(Main.PotionEffects.length)];
            for (int j = 0; j < identifiedPotionEffects.length; j++) {
                if (identifiedPotionEffects[j] != null && identifiedPotionEffects[j].equals(effect)) {
                    i--;
                    break;
                }
                if (j == identifiedPotionEffects.length - 1) {
                    identifiedPotionEffects[i] = effect;
                }
            }

        }


        while( numItems > 0 ){

            int col = rand.nextInt(maxcol);
            int row = rand.nextInt(maxrow);
            //Vector wc = new Vector( rand.nextInt(maxx), rand.nextInt(maxy) );
            while( map[row][col] == 1 ){
                col = rand.nextInt(maxcol);
                row = rand.nextInt(maxrow);
            }
//            Vector wc = new Vector( row, col );
//            System.out.println("wc: " + wc);

            //create a random item at the given position
            ItemMsg i = generateItem();
            i.wx = row;
            i.wy = col;
//            System.out.println("i Material: "+i.material);

            if( i.type.equals("Potion") || i.type.equals("Arrow") ){
                i.requiredLevel=0;
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
            if (i.type.equals("Potion")) {
                if (i.material.equals("Blue")) {
                    //use the stored effect
                    i.effect=identifiedPotionEffects[0];
                }else if( i.material.equals("Orange")){
                    //use the stored effect
                    i.effect=identifiedPotionEffects[1];
                }else if( i.material.equals("Pink")){
                    //use the stored effect
                    i.effect=identifiedPotionEffects[2];
                }else if( i.material.equals("Red")){
                    //use the stored effect
                    i.effect=identifiedPotionEffects[3];
                }else if( i.material.equals("Yellow") ){
                    //use the stored effect
                    i.effect=identifiedPotionEffects[4];
                }else{
                    //this would suggest that the list of potion colors in this class
                    //  is incomplete
                    throw new SlickException("Error: invalid potion color "+i.material);
                }
            }
            worldItems.add(i);

            numItems--;
        }
    }

    public static ItemMsg generateItem() throws SlickException{
    	Item i = new Item( new Vector(0, 0), false, currentItemId, 0, false);
    	
        ItemMsg im = new ItemMsg(i.getID(), i.getOID(), i.getWorldCoordinates().getX(), i.getWorldCoordinates().getY(),
        						i.getType(), i.getEffect(), i.getMaterial(), i.getRequiredClasses(), i.isCursed(),
        						i.isIdentified(), i.getWeight(), i.count, i.getRequiredLevel());
        
        currentItemId++;
        
        return im;
    }

    public static void updateWeight(ItemMsg i, int count){
        //set weight based on type and material
        if( i.type.equals("Armor") ){
            if( i.material.equals("Leather") ){
                i.weight = 40;
            }else if( i.material.equals("Iron") ){
                i.weight = 50;
            }else if( i.material.equals("Gold") ){
                i.weight = 60;
            }
        }else if( i.type.equals("Sword") ){
            if( i.material.equals("Wooden") ){
                i.weight = 10;
            }else if( i.material.equals("Iron") ){
                i.weight = 30;
            }else if( i.material.equals("Gold") ){
                i.weight = 40;
            }
        }else if( i.type.equals("Arrow") ){
            i.weight = 2*count;
        }else if( i.type.equals("Potion") ){
            i.weight = 15*count;
        }else if( i.type.equals("Gloves") ){
            i.weight = 20;
        }else if( i.type.equals("Staff") ){
            if( i.material.equals("Ruby") ){
                i.weight = 15;
            }else if( i.material.equals("Emerald") ){
                i.weight = 10;
            }else if( i.material.equals("Amethyst") ){
                i.weight = 5;
            }
        }
    }

    public static void main(String[] args) {
        int currentPlayerId = 0;
        int maxPlayers = 4;
        try {
            // Create a new Socket for the server
            ServerSocket ss = new ServerSocket(5000);
            // Generate the map
            map = LoadMap.getRandomMap();
            rotatedMap = rotateMap(map);
            // TODO generate AI characters
            enemies = AI.spawnEnemies(map, 20);
//            enemies = AI.spawnDebugEnemies(map);


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
                if (currentPlayerId >= maxPlayers) {     // limit the game to 4 players
                    continue;
                }
                Socket s = ss.accept();
                System.out.println("A new client has connected " + s);
                ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(s.getInputStream());
                // This is the client handler thread.
                System.out.println("Creating new thread for this client...");
                ClientHandler t = new ClientHandler(s, is, os, currentPlayerId);
                clients.add(t);
                t.start();
                currentPlayerId++;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
