package server;

import client.Main;
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
    public static ArrayList<ItemMsg> worldItems = new ArrayList<>();

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
        //Entity.setCoarseGrainedCollisionBoundary(Entity.AABB);
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
//            Vector wc = new Vector( row, col );
//            System.out.println("wc: " + wc);

            //create a random item at the given position
            ItemMsg i = generateItem();
            i.wx = row;
            i.wy = col;
            System.out.println("i Material: "+i.material);

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
            if( i.type.equals("Potion") ){
                if( i.material.equals("Blue") ){
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

            currentItemID++;

            numItems--;
        }
    }

    public static ItemMsg generateItem(){
        //set random properties
        Random rand = new Random();
        rand.setSeed(System.nanoTime());

        int count = 1;

        //first get the item type

        //currently developed item types, for debugging purposes only
        //String[] currentTypes = {"Potion", "Sword", "Armor", "Arrow"};

        //rarity: armor, sword/other weapons, potion, arrow
        //armor: 20%, sword: 30%, potion: 40%, arrow: 50%
        ItemMsg item = new ItemMsg();

        //arrow: 40%; potion: 30%; weapon: 20%;  armor: 10%
        int r = rand.nextInt(100);
        if( r < 10 ){		//10%
            item.type = "Armor";
            item.requiredClasses[0] = "knight";
            item.requiredClasses[1] = "tank";
        }else if( r < 30 ){ //20%
            r = rand.nextInt(100);
            if( r < 20 ){
                item.type = "Staff";
                item.requiredClasses[0] = "mage";
            }else if( r < 50 ){
                item.type = "Sword";
                item.requiredClasses[0] = "knight";
            }else{
                item.type = "Gloves";
                item.requiredClasses[0] = "tank";
            }
        }else if( r < 50 ){	//30%
            item.type = "Potion";
            item.requiredClasses[0] = "knight";
            item.requiredClasses[1] = "tank";
            item.requiredClasses[2] = "mage";
            item.requiredClasses[3] = "archer";
        }else{				//40%
            item.type = "Arrow";
            item.requiredClasses[0] = "archer";
        }




        //choose materials from the appropriate list
        System.out.println("Item type: "+item.type);
        r = rand.nextInt(100);

        if( item.type.equals("Sword") ){
            if( r < 20 ){ 			//20%
                item.material = "Gold";
            }else if( r < 50 ){ 	//30%
                item.material = "Iron";
            }else{ 					//50%
                item.material = "Wooden";
            }
        }else if( item.type.equals("Armor") ){
            if( r < 30 ){ 			//30%
                item.material = "Gold";
            }else{ 					//70%
                item.material = "Iron";
            }
        }else if( item.type.equals("Staff") ){
            if( r < 20 ){ 			//20%
                item.material = "Amethyst";
            }else if( r < 50 ){ 	//30%
                item.material = "Emerald";
            }else{ 					//50%
                item.material = "Ruby";
            }
        }else if( item.type.equals("Gloves") ){
            if( r < 20 ){			//20%
                item.material = "Yellow";
            }else if( r < 50 ){ 	//30%
                item.material = "White";
            }else{					//50%
                item.material = "Red";
            }
        }else{
            item.material = "";
        }

        //choose effects from the appropriate list
        //chance of effect: 50%
        r = rand.nextInt(100);
        if ( r < 50 ){
            if( item.type.equals("Sword") ){
                item.effect = Main.SwordEffects[ rand.nextInt(Main.SwordEffects.length) ];
            }else if( item.type.equals("Armor") ){
                item.effect = Main.ArmorEffects[ rand.nextInt(Main.ArmorEffects.length) ];
            }else if( item.type.equals("Gloves") ){
                item.effect = Main.GloveEffects[ rand.nextInt(Main.GloveEffects.length) ];
            }else if( item.type.equals("Arrow") ){
                item.effect = Main.ArrowEffects[ rand.nextInt(Main.ArrowEffects.length) ];
            }else{
                item.effect = "";
            }
            //except potions and staffs, which will always have an effect
        }else if( item.type.equals("Potion") ){
            item.effect = Main.PotionEffects[ rand.nextInt(Main.PotionEffects.length) ];
        }else if( item.type.equals("Staff") ){
            item.effect = Main.StaffEffects[ rand.nextInt(Main.StaffEffects.length) ];
        }else if( item.type.equals("Gloves") ){
            item.effect = Main.GloveEffects[ rand.nextInt(Main.GloveEffects.length) ];
        }else{
            item.effect = "";
        }

        if( item.type.equals("Potion") ) {
            r = rand.nextInt(5);
            switch (r) {
                case 0:
                    //this.image = ResourceManager.getImage(Main.POTION_BLUE);
                    item.material = "Blue";
                    break;
                case 1:
                    //this.image = ResourceManager.getImage(Main.POTION_ORANGE);
                    item.material = "Orange";
                    break;
                case 2:
                    //this.image = ResourceManager.getImage(Main.POTION_PINK);
                    item.material = "Pink";
                    break;
                case 3:
                    //this.image = ResourceManager.getImage(Main.POTION_RED);
                    item.material = "Red";
                    break;
                case 4:
                    //this.image = ResourceManager.getImage(Main.POTION_YELLOW);
                    item.material = "Yellow";
                    break;
            }
        }
        //items have a chance to be cursed (for now?)
        if( rand.nextInt(100) <= 30 ){
            item.cursed = true;
        }else{
            item.cursed = false;
        }

        //all items start unidentified
        item.identified = false;
        updateWeight(item,count);
        return item;
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
    public static void main(String [] args){

        try {
            // Create a new Socket for the server
            ServerSocket ss = new ServerSocket(5000);
            // Generate the map
            map = LoadMap.getRandomMap();
            rotatedMap = rotateMap(map);
            // TODO generate AI characters
//            enemies = AI.spawnEnemies(map, 20);
            enemies = AI.spawnDebugEnemies(map);


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
}
