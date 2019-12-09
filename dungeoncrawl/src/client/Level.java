package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import server.Msg;



import jig.Vector;


import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import com.sun.java.swing.plaf.motif.MotifBorders.InternalFrameBorder;

import client.MovingEntity.Effect;
import jig.ResourceManager;



public class Level extends BasicGameState {
    private Boolean paused;
    private Random rand;
    private int serverId;
    private String type;

    private int[][] rotatedMap;

    Socket socket;
    ObjectInputStream dis;
    ObjectOutputStream dos;
    String serverMessage;

    int tilesize = 32;
    int offset = tilesize/2;
    int doubleOffset = offset/2;

    private final int messageTimer = 2000;
    
    private int attackCooldown = 0;


    private ArrayList<Item> itemsToRender;
    
    private ArrayList<Character> targets; //union of enemies and characters

    //whether to display player inventory/codex on the screen
    private boolean displayInventory = false;
    private boolean displayCodex = false;
    private int itemx = 0;
    private int itemy = 0; //which item is currently selected in the inventory
    private int selectedEquippedItem = 0; //item selected in the hotbar

    private class Message{
        protected int timer = messageTimer;
        protected String text;
        protected Message(String text){
            this.text = text;
        }
    }

    //array of messages to print to the screen
    private Message[] messagebox;
    private int messages = 4; //number of messages printed at one time

    private class ItemLockTimer{
        //timer to unlock an item so it can be picked up again
        int itemID;
        int timer = 3000; //unlock when this hits zero

        public ItemLockTimer(int id){
            itemID = id;
        }
    }
    private ArrayList<ItemLockTimer> itemLockTimers;

    public void setType(String t){
        type = t;
    }

    public String setSkin(){
        switch(type){
            case "Knight":
                return "knight_leather";
            case "Mage":
                return "mage_leather";
            case "Archer":
                return "archer_leather";
            case "Tank":
                return "tank_leather";
            default:
                break;
        }
        return "";
    }

    private class ThrownItem{
        Item itm;
        Vector direction;
        Vector finalLocation;
        Vector step;

        public ThrownItem(Item itm, Vector direction, Vector finalLocation, Vector step){
            this.itm = itm;
            this.direction = direction;
            this.finalLocation = finalLocation;
            this.step = step;
        }
    }

    private ArrayList<ThrownItem> thrownItems;

    @Override
    public int getID() {
        return Main.LEVEL1;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        serverMessage = "";
        Main dc = (Main) game;
        paused = false;

        messagebox = new Message[messages]; //display four messages at a time

        this.socket = dc.socket;
        this.dis = dc.dis;
        this.dos = dc.dos;


        // Grab the map from the server.Server

        try {
           dc.map = (int[][])dis.readObject();
            System.out.println("reading dc.map type: " + dc.map.getClass().getSimpleName());
//           System.out.println("I got the map!");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

//        System.out.printf("Map Size: %s, %s\n", dc.map[0].length, dc.map.length);

        //rotated map verified correct
        rotatedMap = new int[dc.map[0].length][dc.map.length];
        for( int i = 0; i < dc.map.length; i++ ){
            for( int j = 0; j < dc.map[i].length; j++ ){
                //g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
                //i is y, j is x
                rotatedMap[j][i] = dc.map[i][j];
            }
        }



        // initialize itemsToRender
        itemsToRender = new ArrayList<>();
//        itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));

        //find a tile with no walls in its horizontal adjacencies
        rand = new Random();
        rand.setSeed(System.nanoTime());

        int row = rand.nextInt(dc.ScreenHeight/dc.tilesize);
        int col = rand.nextInt(dc.ScreenWidth/dc.tilesize);

		while(dc.map[row][col] == 1 || wallAdjacent(row, col, dc.map) ){
			//spawn on a floor tile with 4 adjacent floor tiles
	        row = rand.nextInt(dc.ScreenHeight/dc.tilesize);
			col = rand.nextInt(dc.ScreenWidth/dc.tilesize);
		}

        // map variables
        //Main dc = (Main) game;
        dc.mapWidth = dc.map[0].length;
        dc.mapHeight = dc.map.length;

        // TODO setting the hero coordinates/type should be done on the server
        // setup the dc.hero character
        float wx = (dc.tilesize * 20) - dc.offset;
        float wy = (dc.tilesize * 18) - dc.tilesize - dc.doubleOffset;

//        System.out.printf("setting character at %s, %s\n", wx, wy);

        // Setting starting position for the hero.
        String coord = wx + " " + wy;
        int id = 0;
        String type = setSkin();
        try {
            id = dis.read();
            System.out.println("reading id: " + id);
            //System.out.println("Sending my player info.");
            serverId = id;
        }catch(IOException e){
            e.printStackTrace();
        }

        dc.hero = new Character(dc, wx, wy, type, id, this, false);
        dc.characters.add(dc.hero);

        try{
            Msg msg = new Msg(serverId,dc.hero.getType(),wx,wy,dc.hero.getHitPoints());
            dos.writeObject(msg);
            System.out.println("write msg type: " + msg.getClass().getSimpleName());
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }

        //give the hero leather armor with no effect
        //public Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified, Image image)
        /*
        Item a = new Item(null, false, Main.im.getCurrentItemID(), dc.hero.getPid(), "", "Armor", "Leather", false, true,
        		ResourceManager.getImage(Main.ARMOR_IRON));

        Main.im.give(a, dc.hero);
        */

        // TODO spawning enemies and items should be done on the server
//        wx = (dc.tilesize * 18) - dc.offset;
//        wy = (dc.tilesize * 18) - dc.tilesize - dc.doubleOffset;
//        //dc.characters.add(new Character(dc, wx, wy, "skeleton_basic", (int) System.nanoTime(), this, true));
//        spawnEnemies(dc, 20);
        // Grabbing ArrayList of enemies.
        ArrayList<String> enemyList = new ArrayList<>();
        try{
            enemyList = (ArrayList) dis.readObject();
            System.out.println("reading enemyList type: " + enemyList.getClass().getSimpleName());
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        for (String e : enemyList) {
            float x = Float.parseFloat(e.split(" ")[2]);
            float y = Float.parseFloat(e.split(" ")[3]);
            int eid = Integer.parseInt(e.split(" ")[0]);
            dc.characters.add(new Character(dc, x, y, e.split(" ")[1], eid, this, true));
        }
        try {
            int maxcol =  dc.map.length - 2;
            int maxrow = dc.map[0].length - 2;
            Main.im.plant(20, rotatedMap, maxcol, maxrow);
        } catch (SlickException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        // render map
        RenderMap.setMap(dc, dc.hero);

        thrownItems = new ArrayList<ThrownItem>();

        // Test Items
//        addTestItems(dc);
        
        //give the mage a ruby staff with random effect
        //public Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified, Image image, int count) throws SlickException{
        if( dc.hero.getInventory().size() == 0 ){
	        if( dc.hero.getType().toLowerCase().contains("mage")){
		        try {
					Item staff = new Item(null, false, -1, -1, "Healing", "Staff", "Ruby", false, true, ResourceManager.getImage(Main.STAFF_RUBY), 1);
					 
					Random rand = new Random();
					rand.setSeed(System.nanoTime());
					
					staff.setEffect(Main.StaffEffects[ rand.nextInt(Main.StaffEffects.length) ]);
					
					Main.im.give(staff, dc.hero);
					
					dc.hero.equipItem(0);
					
					
				} catch (SlickException e1) {
					return;
				}
	        }else if( dc.hero.getType().toLowerCase().contains("archer")){
		        try {
					Item arrow = new Item(null, false, -1, -1, "", "Arrow", "", false, true, ResourceManager.getImage(Main.ARROW_NORMAL), 10);
					
					Main.im.give(arrow, dc.hero);
					
					dc.hero.equipItem(0);
					
					
				} catch (SlickException e1) {
					return;
				}
	        }
        }
        
        targets = new ArrayList<Character>();
        targets.addAll(dc.characters);
        targets.addAll(dc.enemies);
    }

    private boolean wallAdjacent(int row, int col, int[][] map){
        //check if there is a wall or map edge in a horizontally adjacent tile
        try{
            if( map[row-1][col] == 1 ){
                return true;
            }else if( map[row+1][col] == 1 ){
                return true;
            }else if( map[row][col-1] == 1 ){
                return true;
            }else if( map[row][col+1] == 1 ){
                return true;
            }else if( map[row][col] == 1 ){
                return true;
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            return true;
        }
        return false;
    }


    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        messagebox = new Message[messages];

        itemLockTimers = new ArrayList<ItemLockTimer>();
    }


    public void addMessage(String message){
        //check for a duplicate message
    	for( Message m : messagebox ){
    		if( m == null ){
    			break;
    		}
    		if( m.text.equals(message)){
    			return;
    		}
    	}
    	
    	//add a message to the first index of the message box
        //  and shift everything else down
        for( int i = messagebox.length-1; i > 0; i-- ){
            messagebox[i] = messagebox[i-1];
        }
        messagebox[0] = new Message(message);
    }
    
    public Image getArrowImage(String effect, String dir) throws SlickException{
    	 //get image based on effect and direction
        Image image = null;
        if( dir == null ){
        	//spawn arrow item
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL);
            }
        }else if( dir.equals("up") ){
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON_UP);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME_UP);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE_UP);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL_UP);
            }
        }else if( dir.equals("down") ){
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON_DOWN);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME_DOWN);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE_DOWN);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL_DOWN);
            }
        }else if( dir.equals("left") ){
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON_LEFT);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME_LEFT);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE_LEFT);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL_LEFT);
            }
        }else if( dir.equals("right") ){
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON_RIGHT);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME_RIGHT);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE_RIGHT);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL_RIGHT);
            }
        }
        if( image == null ){
            throw new SlickException("Invalid arrow effect " + effect);
        }
        return image;
    }


    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dc = (Main) game;
        // render tiles
        displayMap(dc, g);

        //render all visible items
        renderItems(dc, g);

        // TODO will need to sort the lists and draw in order so players draw on top of others
        // draw other characters
        renderCharacters(dc, g);

        // draw the hero
        dc.hero.animate.render(g);
        
        //draw the hero's visual effects
        if( dc.hero.vfx != null ){
        	dc.hero.vfx.render(g);
        }
        
        //draw the vfx for all characters
        for( Character ch : targets ){
        	if( ch.vfx != null ){
        		ch.vfx.render(g);
        	}
        }
        

        renderHealthBar(dc, g);

        //render messages
        renderMessages(dc, g);

        //display player inventory
        renderInventory(dc, g);

        // render the codex
        renderCodex(dc, g);

        //draw the player's equipped items
        renderEquippedItems(dc, g);

        //display player inventory
//        renderInventory(dc, g);
        if( displayInventory ){
            renderItemBox(dc, g, "Inventory", dc.tilesize, dc.tilesize, dc.tilesize*4, dc.tilesize*8);
            ArrayList<Item> items = dc.hero.getInventory();
            if( items.size() != 0 ){
                int row = 2;
                int col = 1;
                for( int i = 0; i < items.size(); i++ ){
                    g.drawImage(items.get(i).getImage(), col*dc.tilesize, row*dc.tilesize);
                    col++;
                    if( i > 4 && i % 4 == 0 ){
                        row++;
                        col = 1;
                    }
                }
                //draw a square around the selected item
                Color tmp = g.getColor();
                g.setColor(Color.white);
                g.drawRect(
                        (itemx + 1)*dc.tilesize,
                        (itemy + 2)*dc.tilesize,
                        dc.tilesize,
                        dc.tilesize
                );
                try{
                    Item sItem = items.get((itemy*4)+itemx);
                    if( sItem.isIdentified() ){
                        g.drawString(sItem.toString(), dc.tilesize+10, dc.tilesize*8);
                    }else{
                        g.drawString("Unidentified " + sItem.toString(), dc.tilesize+10, (dc.tilesize*8)+(dc.tilesize/4));
                    }
                }catch(IndexOutOfBoundsException ex){

                }
                g.setColor(tmp);
            }
        }
        //draw the player's equipped items
        renderEquippedItems(dc, g);

        // draw test items
//        renderTestItems(dc, g);
//         renderDebug(dc, g);


        //minimal HUD
        g.drawString("HP: " + dc.hero.getHitPoints(), dc.ScreenWidth-150, dc.ScreenHeight-(dc.tilesize*3));
        g.drawString("Mana: " + dc.hero.getMana(), dc.ScreenWidth-150, dc.ScreenHeight-(dc.tilesize*4));
        g.drawString("Strength: "+dc.hero.getStrength(), dc.ScreenWidth-150, dc.ScreenHeight-(dc.tilesize*5));
        g.drawString("Speed: "+dc.hero.getMovementSpeed(), dc.ScreenWidth-150, dc.ScreenHeight-(dc.tilesize*6));
        g.drawString("Coord : " + dc.hero.animate.getPosition(), dc.ScreenWidth-200, dc.ScreenHeight-(dc.tilesize*7));
        g.drawString("Pos   : <" + (int) dc.hero.getTileWorldCoordinates().getX() + ", " +  (int) dc.hero.getTileWorldCoordinates().getY() + ">", dc.ScreenWidth-200, dc.ScreenHeight-(dc.tilesize*9));
        g.drawString("Origin: " + dc.hero.getOrigin().toString(), dc.ScreenWidth-200, dc.ScreenHeight-(dc.tilesize*8));
        g.drawString("Weight: " + dc.hero.getInventoryWeight(), dc.ScreenWidth-300, dc.ScreenHeight-(dc.tilesize*9));

        if (dc.showPath) {
            renderShortestPath(dc, g);
//            renderPathWeights(dc, g);     // this method really only works well when one AI is present
        }

        // display a paused message
        if (paused) {
            renderPauseMessage(dc, g);
            renderActiveCheats(dc, g);
        }
    }

    private void renderPauseMessage(Main dc, Graphics g) {
        // TODO render paused message
        Color tmp = g.getColor();
        g.setColor(new Color(0, 0, 0, .3f));
        g.fillRect(0, 0, dc.ScreenWidth, dc.ScreenHeight);
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("PAUSED", (float) dc.ScreenWidth/2 - 12, (float) dc.ScreenHeight/2 - 10);
        g.setColor(tmp);

        // show controls
        tmp = g.getColor();
        int shift = 465;
        int left = 150;
        int indent = 200;
        g.setColor(new Color(0, 0, 0, .5f));
        g.fillRect( left - 20, shift - 20, 385, 260);
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("Move: w, a, s, d", left, shift);
        g.drawString("Pause: p", left, shift + 25);
        g.drawString("Open/Close Inventory: i", left, shift + 50);
        g.drawString("Traverse Inventory: Arrow Keys", indent, shift + 75);
        g.drawString("Move Item to Inventory: enter", indent, shift + 100);
        g.drawString("Display Codex: o", left, shift + 125);
        g.drawString("drop item: backslash", left, shift + 150);
        g.drawString("unequip item: shift", left, shift + 175);
        g.drawString("attack: space/enter", left, shift + 200);
        g.setColor(tmp);
    }

    private void renderActiveCheats(Main dc, Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(255, 255, 255, 1f));
        // render cheats
        if (!dc.showPath){
            g.drawString("Display Paths: Disabled", 50, 50);
        }
        else {
            g.drawString("Display Paths: Enabled", 50, 50);
        }
        if (dc.collisions) {
            g.drawString("Collisions: Enabled", 50, 75);
        }
        else {
            g.drawString("Collisions: Disabled", 50, 75);
        }
        if (dc.invincible) {
            g.drawString("Invincible: Enabled", 50, 100);
        }
        else {
            g.drawString("Invincible: Disabled", 50, 100);
        }
        g.setColor(tmp);
    }

    /** Renders the AI's shortest path
     *
     */
    private void renderShortestPath(Main dc, Graphics g) {
        // only load arrows if the player wants to show the dijkstra's path
        for (Character ai : dc.characters) {
            if (!ai.ai) {
                continue;
            }
            for (Arrow a : ai.arrows) {
                Vector sc = world2screenCoordinates(dc, a.getWorldCoordinates());
                a.setPosition(sc);
                a.render(g);
            }
        }

    }

    /**
     * set scaled to true to show the values smaller in the top
     * set coords true to see the game coordinates, this really drops fps, be warned
     * @param dc
     * @param g
     */
    private void renderPathWeights(Main dc, Graphics g) {
        boolean scaled = false;
        boolean coords = false;
        for (Character ai : dc.characters) {
            if (ai.weights != null) {
                for (int i = 0; i < ai.weights.length; i++) {
                    for (int j = 0; j < ai.weights[0].length; j++) {
                        Color tmp = g.getColor();
                        g.setColor(new Color(255, 255, 255, 1f));

                        //make the messages fade away based on their timers
                        String msg = String.valueOf((int) ai.weights[i][j]);

                        if (!scaled) {
                            if (ai.weights[i][j] > 200000) {
                                msg = "INF";
                            }
                            Vector wc = new Vector(i * dc.tilesize, j * dc.tilesize);
                            Vector sc = world2screenCoordinates(dc, wc);
                            g.drawString(msg, sc.getX(), sc.getY());
                        }
                        else {
                            g.scale(.5f, .5f);
                            Vector wc = new Vector(2 * i * dc.tilesize, 2 * j * dc.tilesize);
                            Vector sc = world2screenCoordinates(dc, wc);
                            g.drawString(msg, sc.getX(), sc.getY());

                            // draw x, y tile values
                            if (coords) {
                                g.setColor(new Color(255, 255, 255, .8f));
                                wc = new Vector(2 * i * dc.tilesize, 2 * j * dc.tilesize + dc.offset);
                                sc = world2screenCoordinates(dc, wc);
                                String I = String.valueOf(i);
                                String J = String.valueOf(j);
                                msg = "(" + I + "," + J + ")";
                                g.drawString(msg, sc.getX(), sc.getY());
                            }
                            g.scale(2f, 2f);
                        }
                        g.setColor(tmp);
                    }
                }
            }
        }
    }

    /**
     * Render debug information on the screen
     * @param dc
     * @param g
     */
    private void renderDebug(Main dc, Graphics g) {
        for( int i = 0; i < dc.map.length; i++ ){
            for( int j = 0; j < dc.map[i].length; j++ ){
                g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
                //i is y, j is x
            }
        }
        if( displayCodex ){
            //display this box next to the item box
            //  if it is open
            int x = dc.tilesize;
            if( displayInventory ){
                x *= 4;
            }

            renderItemBox(dc, g, "Codex", x, dc.tilesize, dc.tilesize * 10, dc.tilesize*8);

            ArrayList<Item> items = dc.hero.getCodex();
            int row = 2;
            for( Item i : items ){
                g.drawImage(i.getImage(), dc.tilesize, row*dc.tilesize);
                Color tmp = g.getColor();
                g.setColor(Color.white);
                g.drawString(i.toString(), dc.tilesize*2, dc.tilesize*row + dc.tilesize*0.25f);
                g.setColor(tmp);
                row++;
            }
        }
    }

    /**
     * Renders the players equipped items
     * @param dc
     * @param g
     */
    private void renderEquippedItems(Main dc, Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(0, 0, 0, 0.5f));
        g.fillRoundRect(dc.ScreenWidth-(dc.tilesize*5), dc.ScreenHeight-(dc.tilesize*2), dc.tilesize*4, dc.tilesize, 0);
        int x = 0;
        int y = 0;
        for( int i = 0; i < dc.hero.getEquipped().length; i++ ){
            if( dc.hero.getEquipped()[i] != null ){
                x = dc.ScreenWidth-(dc.tilesize*(dc.hero.getEquipped().length+1));
                y = dc.ScreenHeight-(dc.tilesize*2);
                g.drawImage(dc.hero.getEquipped()[i].getImage(), x+(dc.tilesize*i), y);
                Color tmp2 = g.getColor();
                g.setColor(Color.white);
                g.drawString(dc.hero.getEquipped()[i].count+"", x+(dc.tilesize*i), y+15);
                g.drawString(dc.hero.getEquipped()[i].getRequiredLevel()+"", x+(dc.tilesize*i), y);
                g.setColor(tmp2);
                
            }
        }
        g.setColor(Color.white);
        g.drawRect(x+(dc.tilesize*selectedEquippedItem), y, dc.tilesize, dc.tilesize);
        
        g.setColor(tmp);
    }



    /**
     * Renders the codex on the players screen
     * @param dc
     * @param g
     */
    private void renderCodex(Main dc, Graphics g) {
        if( displayCodex ){
            //display this box next to the item box
            //  if it is open
            int x = dc.tilesize;
            if( displayInventory ){
                x *= 4;
            }

            renderItemBox(dc, g, "Codex", x, dc.tilesize, dc.tilesize * 10, dc.tilesize*8);

            ArrayList<Item> items = dc.hero.getCodex();
            int row = 2;
            for( Item i : items ){
                g.drawImage(i.getImage(), dc.tilesize, row*dc.tilesize);
                Color tmp = g.getColor();
                g.setColor(Color.white);
                g.drawString(i.toString(), dc.tilesize*2, dc.tilesize*row + dc.tilesize*0.25f);
                g.setColor(tmp);
                row++;
            }
        }
    }

    /**
     * Render the players inventory
     * @param dc
     * @param g
     */
    private void renderInventory(Main dc, Graphics g) {
        if( displayInventory ){
            Color tmp = g.getColor();
            g.setColor(Color.white);
            
            renderItemBox(dc, g, "Inventory", dc.tilesize, dc.tilesize, dc.tilesize*4, dc.tilesize*8);
            ArrayList<Item> items = dc.hero.getInventory();
            if( items.size() != 0 ){
                int row = 2;
                int col = 1;
                for( int i = 0; i < items.size(); i++ ){
                    g.drawImage(items.get(i).getImage(), col*dc.tilesize, row*dc.tilesize);
                    g.drawString(items.get(i).count+"", ((1+col)*dc.tilesize)-10, ((1+row)*dc.tilesize)-15);
                    g.drawString(items.get(i).getRequiredLevel()+"", ((1+col)*dc.tilesize)-10, ((1+row)*dc.tilesize)-30);
                    col++;
                    if( i > 4 && i % 4 == 0 ){
                        row++;
                        col = 1;
                    }
                }
                //draw a square around the selected item

                g.drawRect(
                        (itemx + 1)*dc.tilesize,
                        (itemy + 2)*dc.tilesize,
                        dc.tilesize,
                        dc.tilesize
                );
                g.setColor(tmp);
            }
        }
    }

    /**
     * Renders messages
     * @param dc
     * @param g
     */
    private void renderMessages(Main dc, Graphics g) {
        for( Message m : messagebox ){
            if( m != null ){
                g.setColor(new Color(0, 0, 0, 0.5f));
                g.fillRoundRect(25, dc.ScreenHeight-(20 * messagebox.length), 400, 20 * messagebox.length, 0);
                g.setColor(Color.red);
                break;
            }
        }

        for( int i = 0; i < messagebox.length; i++ ){
            if( messagebox[i] == null || messagebox[i].text == "" ){
                break;
            }
            Color tmp = g.getColor();
            //make the messages fade away based on their timers
            g.setColor(new Color(255, 0, 0, (float) (messagebox[i].timer*2)/messageTimer));
            g.drawString(messagebox[i].text, 30, dc.ScreenHeight-(20 * (messagebox.length - i)));
            g.setColor(tmp);
        }
    }


    /**
     * Renders the players healthbar if the players health is below 100%
     * @param dc
     * @param g
     */
    private void renderHealthBar(Main dc, Graphics g) {
        Vector sc;
        float x;
        float y;
        float remaining;
        Color tmp = g.getColor();
        int width = 30;
        for (Character ch : dc.characters) {
            if (ch.getHitPoints() == ch.getStartingHitPoints()) {
                continue;
            }
            if (characterInRegion(dc, ch)) {
                sc = world2screenCoordinates(dc, ch);
                x = sc.getX() - offset + 1;
                y = sc.getY() - tilesize + 2;// + offset;
                remaining = (ch.getHitPoints()/ch.getStartingHitPoints())* width;

                // total health
                g.setColor(new Color(255, 0, 0, 0.5f));
                g.fillRoundRect(x, y, width, 3, 0);

                // remaining health
                g.setColor(new Color(0, 255, 0, 0.5f));
                g.fillRoundRect(x, y, remaining, 3, 0);

            }
        }
        g.setColor(tmp);
    }

    /**
     * Renders the visible items on the map
     * @param dc
     * @param g
     */
    private void renderItems(Main dc, Graphics g) {
        // get the items in range
        Vector wc;
        ArrayList<Item> worldItems = Main.im.getWorldItems();
        g.setColor(Color.red);
        for (Item i : worldItems) {
            // convert the world tile coordinates to world pixel coordinates
            wc = new Vector((i.getWorldCoordinates().getX() * dc.tilesize) + (float) (dc.tilesize / 2),
                    (i.getWorldCoordinates().getY() * dc.tilesize) + (float) (dc.tilesize / 2));
            // draw the objects on the screen
            if (objectInRegion(dc, wc)) {
                Vector sc = world2screenCoordinates(dc, wc);
                i.setPosition(sc);
                i.render(g);
            }
        }
    }

    /**
     * Renders new items which are being tested
     * @param dc
     * @param g
     */
    private void renderTestItems(Main dc, Graphics g) {
        Vector wc;
        for (DisplayItem i : dc.testItems) {
            wc = i.getWorldCoordinates();
            // draw the objects on the screen
            if (objectInRegion(dc, wc)) {
                Vector sc = world2screenCoordinates(dc, wc);
                i.setPosition(sc);
                i.render(g);
            }
        }
    }


    /**
     * helper Used for testing new items
     */
    private void addTestItems(Main dc) {
        String[] itemList = new String[] {
                "staff_emerald", "staff_ameythst", "staff_ruby",
                "gloves_red", "gloves_white", "gloves_yellow",
                "robes_blue", "robes_purple",
                "archer_clothes_green", "archer_clothes_iron"
        };
        int position = 16;
        for (int i = 0; i < itemList.length; i++) {
            position += 1;
            dc.testItems.add(new DisplayItem((dc.tilesize * position)- dc.offset, (dc.tilesize * 4)- dc.offset, itemList[i]));
        }
    }

    /*
    Renders the other characters and AI on the screen if they are in the players screen
     */
    private void renderCharacters(Main dc, Graphics g) {
        for (Character ch : dc.characters) {
            if (!ch.equals(dc.hero)) {
                Vector sc = world2screenCoordinates(dc, ch.getWorldCoordinates());
                ch.animate.setPosition(sc);
                if (characterInRegion(dc, ch)) {
                    ch.animate.render(g);
                }
            }

        }
        //*/

    }

    private void renderItemBox(Main dc, Graphics g, String title, int x, int y, int width, int height){
        Color tmp = g.getColor();
        g.setColor(new Color(0, 0, 0, 0.5f));

        //create a rectangle that can fit all the player's items with 4 items per row
        g.fillRoundRect(dc.tilesize, dc.tilesize, width, height, 0);

        g.setColor(Color.white);
        g.drawString(title, dc.tilesize + 10, dc.tilesize + 10);
        g.setColor(tmp);

    }

    private Scanner scan = new Scanner(System.in);

    private String prevks = "";

    private Vector vectorFromKeystroke(String ks){
        if( ks.equals("w") ){
            return new Vector(0, -1);
        }else if( ks.equals("s") ){
            return new Vector(0, 1);
        }else if( ks.equals("a") ){
            return new Vector(-1, 0);
        }else if( ks.equals("d") ){
            return new Vector(1, 0);
        }else{
            return new Vector(0, -1);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Main dc = (Main) game;
        Cheats.enableCheats(dc, input);
        pause(input);
        if (paused) {
            return;
        }
        
        //decrease attack timer
        if( attackCooldown > 0 ){
        	attackCooldown -= delta;
        }
        
        //regenerate some mana if this client is a mage
        if( dc.hero.getType().toLowerCase().contains("mage") && dc.hero.getMana() < dc.hero.getMaxMana() ){
        	dc.hero.setMana(dc.hero.getMana() + 0.05f);
        	if( dc.hero.getMana() > dc.hero.getMaxMana() ){
        		dc.hero.setMana(dc.hero.getMaxMana());
        	}
        }

        
        //implement effects on the character
        //TODO: this should still be done on the client
        // but the active effects list should be communicated
        // to the server so that other clients can see the effect
        // visualizations.
        //dc.hero.implementEffects();
        
        
        //draw visual effects
        for( Character ch : targets ){
        	
        	//TODO: this should be done on the server,
        	// not the client
        	//(client should handle its own effects)
        	ch.implementEffects();
        	
        	
        	ArrayList<String> removedEffects = new ArrayList<String>();
        	
        	//check if the character is now dead (killed by status effect)
        	if( ch.getHitPoints() <= 0 ){
        		//if dead, remove all effects
        		for( Effect e : ch.getActiveEffects() ){
        			removedEffects.add(e.name);
        		}
        		//clear active effects
        		ch.clearActiveEffects();
        	}else{
        		//if not dead update timers and remove effects
        		//  with expired timers
        		removedEffects = ch.updateEffectTimers(16);
        	}
        	
	        //remove visual effects corresponding to removed effects
        	if( ch.vfx != null ){
	        	for( String st : removedEffects ){
	        		ch.vfx.updateVisualEffectTimer(st, 0);
	        	}
        	}
        	
        	//add any new visual effects
        	ch.addVisualEffects();
        	//and update timers
        	ch.updateVisualEffectTimers();
        	
        	
        	//follow the character
        	if( ch.vfx != null ){
        		ch.vfx.setPosition(ch.animate.getPosition());
        	}
        }

        String ks = getKeystroke(input);
        if( prevks.equals("") ){
            prevks = ks;
        }
        //convert key stroke to vector
        Vector lastKnownDirection;
        //System.out.println(ks);
        if( ks.equals("") ){
            lastKnownDirection = vectorFromKeystroke(prevks);
        }else{
            lastKnownDirection = vectorFromKeystroke(ks);
        }
        dc.hero.move(ks);
        positionToServer(dc);  // Get the player's updated position onto the server.
        updateOtherPlayers(dc);

        //cheat code to apply any effect to the character
        if( input.isKeyPressed(Input.KEY_LALT) ){
            System.out.println("Game window frozen, expecting user input.");
            System.out.println("Enter valid effect name (e.g. Healing) ");

            String effect = scan.next().trim();

            if( effect.equals("Iron") ){
                effect = "Iron Skin";
            }
            System.out.println("Got effect '"+effect+"'");

            dc.hero.addEffect(effect,false);
        }


        if( input.isKeyPressed(Input.KEY_I) ){
            displayInventory = !displayInventory;
            displayCodex = false;
        }
        if( input.isKeyPressed(Input.KEY_O) ){
            displayCodex = !displayCodex;
            displayInventory = false;
        }

        if( displayInventory ){
            if( input.isKeyPressed(Input.KEY_UP) ){
                //selectedItem.setY(selectedItem.getY()-1);
                itemy--;
            }else if( input.isKeyPressed(Input.KEY_DOWN) ){
                //selectedItem.setY(selectedItem.getY()+1);
                itemy++;
            }else if( input.isKeyPressed(Input.KEY_LEFT) ){
                //selectedItem.setX(selectedItem.getX()-1);
                itemx--;
            }else if( input.isKeyPressed(Input.KEY_RIGHT) ){
                //selectedItem.setX(selectedItem.getX()+1);
                itemx++;
            }else if( input.isKeyPressed(Input.KEY_ENTER) ){
                System.out.println("Equipping "+((itemy*4)+itemx)+"...");
                //dc.hero.equipItem( dc.hero.getInventory().get((itemy*4)+itemx).getID(), 0);
                try{
                    String m = dc.hero.equipItem((itemy*4)+itemx);
                    itemx = 0;
                    itemy = 0;
                    if( m != null ){
                        addMessage(m);
                    }
                }catch(IndexOutOfBoundsException ex){
                    System.out.println("Out of bounds.");
                }
            }


            if( itemx < 0 ){
                itemx = 0;
            }
            if( itemy < 0 ){
                itemy = 0;
            }
            if( itemx > 3 ){
                itemx = 3;
            }
            if( itemy > dc.hero.getInventory().size()/4){
                itemy = dc.hero.getInventory().size()/4;
            }

            //System.out.println(itemx+", "+itemy);
        }else{
            if( input.isKeyPressed(Input.KEY_LEFT) ){
                selectedEquippedItem--;
            }else if( input.isKeyPressed(Input.KEY_RIGHT) ){
                selectedEquippedItem++;
            }else if( input.isKeyPressed(Input.KEY_ENTER) ){
                //attack with item
                //System.out.println("Attacking with " + dc.hero.getEquipped()[selectedEquippedItem].getType() );
            	
            	Item itm = dc.hero.getEquipped()[selectedEquippedItem];
            	if( attackCooldown <= 0 ){
            		if( dc.hero.getType().toLowerCase().contains("knight") || 
	            			dc.hero.getType().toLowerCase().contains("tank") ){
	            		attack(null, dc, lastKnownDirection);
	            	}else if( canUse(itm, dc.hero) ){
	            		attack(dc.hero.getEquipped()[selectedEquippedItem], dc, lastKnownDirection);
	            	}
            		
	            	//update attack cooldown based on item weight
            		if( itm == null ){
            			attackCooldown = 1000; 
            		}else{
            			//max attack item weight is 60, max cooldown time should be one second
            			attackCooldown = itm.getWeight()*17;
            		}
            	}
            }else if( input.isKeyPressed(Input.KEY_APOSTROPHE) ){
                //use item on own character
                Item i = dc.hero.getEquipped()[selectedEquippedItem];
                if( i != null ){
                
	                if( canUse(i, dc.hero) ){
		                String x = "";
		                if( i.getType().equals("Potion") ){
		                    x = "Drank";
		                }else if( i.getType().equals("Armor") ){
		                    x = "Put on";
		                }else{
		                    x = "Used";
		                }
		                addMessage(x + " " + i.toString());
		                //TODO: add potion effects to character
		                if( i.getType().equals("Potion") || i.getType().equals("Armor") ){
		                    //add effect to character
		                    dc.hero.addEffect(i.getEffect(),false);
		                    addMessage("You are now affected by " + i.getEffect().toLowerCase());
		                }
		
		                //remove the item from hands
		                dc.hero.unequipItem(selectedEquippedItem);
		
		                //only remove the item from the inventory if it is a potion/consumable
		                //armor should remain in the player's inventory
		                if( i.getType().equals("Potion") ){
		                    dc.hero.discardItem(i.getID(), true);
		                }else if( i.getType().equals("Armor") ){
		                    i.identify();
		
		                    //set the hero type to change the armor
		                    if( dc.hero.getType().contains("knight") ){
		                        dc.hero.setType("knight_"+i.getMaterial().toLowerCase());
		                    }else if( dc.hero.getType().contains("tank") ){
		                        dc.hero.setType("tank_"+i.getMaterial().toLowerCase());
		                    }
		                }
	                }
                }

            }else if( input.isKeyPressed(Input.KEY_BACKSLASH) ){


                Item itm = dc.hero.getEquipped()[selectedEquippedItem];
                if( itm != null ){
	                if( itm.getType().equals("Arrow") ){
	                	//reset the item count
	                	itm.count = 1;
	                	
	                	itm.setImage(getArrowImage(itm.getEffect(), null));
	                }
	                dc.hero.unequipItem(selectedEquippedItem);
	
	
	
	                //place the item at the hero's feet
	                Vector wc = new Vector(
	                		(int)(dc.hero.getWorldCoordinates().getX()/dc.tilesize),
	                		(int)(dc.hero.getWorldCoordinates().getY()/dc.tilesize)+1
	                		);
	                Main.im.take(itm, dc.hero, wc, false);
	
	                
	                //reduce the hero's inventory weight
	                
	                //lock the item with a timer
	                itm.lock();
	                //add item lock timer
	                itemLockTimers.add( new ItemLockTimer(itm.getID()) );
	                System.out.println("Locked dropped item.");
	
	                //add the item to the render list
	                // TODO may need to be changed to be added to worldItems
	                itemsToRender.add(itm);
	
	                addMessage("Dropped "+itm.toString()+".");
                }
            }else if( input.isKeyPressed(Input.KEY_RSHIFT) ){
                //return item to inventory
                dc.hero.unequipItem(selectedEquippedItem);
            }

            if( selectedEquippedItem < 0 ){
                selectedEquippedItem = 0;
            }
            if( selectedEquippedItem > dc.hero.getEquipped().length-1 ){
                selectedEquippedItem = dc.hero.getEquipped().length-1;
            }
        }

        //remove dead characters
        for( Character ch : dc.characters ){
        	if( ch.getHitPoints() <= 0 ){
        		if( ch.vfx != null ){
	        		for( Effect e : ch.getActiveEffects() ){
	        			ch.vfx.updateVisualEffectTimer(e.name, 0);
	        		}
	        		ch.vfx = null;
	        		
	        		//remove all active effects from the 
	        		ch.clearActiveEffects();
        		}
        	}
        }
        
        
        dc.characters.removeIf(b -> b.getHitPoints() <= 0);
        
        
        // if the hero has no health, then replace it with a new hero character in the same spot
        if(dc.hero.getHitPoints() <= 0){
            dc.hero = new Character(dc,dc.hero.animate.getX(),dc.hero.animate.getY(),dc.hero.getType(),
                    serverId,this,false);
            dc.characters.add(0,dc.hero);
        }


        // cause AI players to move around
        for( Character ch : dc.characters ) {
            if (ch.ai) {        // if the player is an AI player, move them
                ch.moveAI(delta);
            }
        }

        //advance any thrown items along their path
        ArrayList<ThrownItem> reachedDestination = new ArrayList<ThrownItem>();
        for( ThrownItem ti : thrownItems ){

            //check if a thrown item went off the screen
            if( ti.itm.getWorldCoordinates().getX() < 0 + dc.hero.getOrigin().getX() || ti.itm.getWorldCoordinates().getX() > dc.hero.getOrigin().getX() + (dc.ScreenWidth/dc.tilesize)){
                reachedDestination.add(ti);
                addMessage("thrown " + ti.itm.getType() + " went off screen");
            }else if( ti.itm.getWorldCoordinates().getY() < 0 + dc.hero.getOrigin().getY() || ti.itm.getWorldCoordinates().getY() > dc.hero.getOrigin().getY() + (dc.ScreenHeight/dc.tilesize)){
                reachedDestination.add(ti);
                addMessage("thrown " + ti.itm.getType() + " went off screen");
            }
            if( throwItem(ti, dc) ){
                reachedDestination.add(ti);
                addMessage("thrown " + ti.itm.getType() + " reached destination");
            }

            //check if a thrown item hit a character
            //merge the character and enemy lists
            for( Character ch : targets ){

                if( ch.getPid() == dc.hero.getPid() ){
                    continue;
                }

                //Vector chwc = new Vector( ch.getTileWorldCoordinates().getX()/dc.tilesize, ch.getTileWorldCoordinates().getY()/dc.tilesize);
                float x = ch.getTileWorldCoordinates().getX();
                float y = ch.getTileWorldCoordinates().getY();
                Vector chwc = new Vector((int)x, (int)y);
                //System.out.println(chwc.toString());

                if( Math.abs(chwc.getX() - ti.itm.getWorldCoordinates().getX()) < 1.5 && Math.abs(chwc.getY() - ti.itm.getWorldCoordinates().getY()) < 1.5 ){
                    //addMessage("thrown " + ti.itm.getType() + " hit enemy");

                    //potions do no damage but cause status effects on the target
                    if( ti.itm.getType().equals("Potion") ){
                        ch.takeDamage(0, ti.itm.getEffect(),false);
                        dc.hero.addToCodex(ti.itm);
                        reachedDestination.add(ti);
                    }else if( ti.itm.getType().equals("Arrow") || ti.itm.getType().equals("Staff") ){
                        //roll random damage, similarly to a sword
                        rand.setSeed(System.nanoTime());
                        int r = rand.nextInt(100);

                        float damagePercent = r/(float) 100;

                        String m;
                        if( damagePercent == 0 ){
                            m = "Missed.";
                        }else{
                            if( ch.takeDamage(10*damagePercent, ti.itm.getEffect(),false) ){
                                //set character action to die
                                ch.updateAnimation("die");
                                
                                //ch.vfx = null;
                            }

                            m = "Hit enemy for " + (int) (10*damagePercent) + " damage.";
                            if( damagePercent >= 0.8 ){
                                m = m + " Critical hit!";
                            }
                        }
                        addMessage(m);
                    }
                }
            }

            //check if an item hit a wall tile
            if( rotatedMap[(int) ti.itm.getWorldCoordinates().getX()][(int) ti.itm.getWorldCoordinates().getY()] == 1 ){
                addMessage("thrown " + ti.itm.getType() + " hit wall");

                reachedDestination.add(ti);
            }
        }
        //remove any that have reached their destination
        thrownItems.removeAll(reachedDestination);
        //itemsToRender.removeAll(reachedDestination);
        for( ThrownItem ti : reachedDestination ){
            Main.im.removeFromWorldItems(ti.itm);
        }


        //check if a character has hit an item
        for( Character ch : dc.characters ){
            if( ch.ai ){
                continue;
            }
            float x = ch.getTileWorldCoordinates().getX();
            float y = ch.getTileWorldCoordinates().getY();
            Vector aniPos = new Vector((int)x, (int)y);

            Item i = Main.im.getItemAt(aniPos);
            if( i != null && !i.isLocked() ){
            	if( ch.getInventoryWeight() >= ch.getMaxInventoryWeight() ){
            		addMessage("You cannot carry any more.");
            	}else{
	            		
	            	
	                if( i.isIdentified() ){
	                    addMessage("Picked up " + i.toString() + ".");
	                }else{
	                    addMessage("Picked up unidentified "+i.toString()+".");
	                }
	                
            
	                
	                
	
	                
	                if( i.getType().equals("Arrow") ){
	                	Image image = null;
	                	if( i.getEffect().equals("Poison") ){
	                		image = ResourceManager.getImage(Main.ARROW_POISON_UP);
	                	}else if( i.getEffect().equals("Flame") ){
	                		image = ResourceManager.getImage(Main.ARROW_FLAME_UP);
	                	}else if( i.getEffect().equals("Ice") ){
	                		image = ResourceManager.getImage(Main.ARROW_ICE_UP);
	                	}else if( i.getEffect().equals("") ){
	                		image = ResourceManager.getImage(Main.ARROW_NORMAL_UP);
	                	}
	                	if( image == null){
	                		throw new SlickException("Invalid arrow effect.");
	                	}
	                	
	                	//give the character 5 arrows
	                	Item arrow = new Item(i.getWorldCoordinates(), i.isLocked(), i.getID(), ch.getPid(), i.getEffect(), "Arrow", i.getMaterial(), 
	                			i.isCursed(), i.isIdentified(), image, 5);
	                	Main.im.give(arrow, ch);
	                	i.lock();
	                }else{
	                    //give removes item from the world's inventory
	                    //  and adds it to the player's inventory
	                    Main.im.give(i, ch);
	                }
	                
	                //check if player is weighed down
	                if( ch.getInventoryWeight() >= ch.getMaxInventoryWeight()*0.7 ){
                    	addMessage("You are encumbered.");
                    }
	
	                //stop rendering the item
	                itemsToRender.remove(i);
	                Main.im.removeFromWorldItems(i);
	            }
            }
        }



        //update message timers
        for( int i = 0; i < messagebox.length; i++ ){
            if( messagebox[i] == null ){
                break;
            }
            if( messagebox[i].timer <= 0 ){
                messagebox[i] = null;
            }else{
                messagebox[i].timer -= delta;
            }
        }

        //update item lock timers if applicable
        for( ItemLockTimer t : itemLockTimers ){
            t.timer -= delta;
            if( t.timer <= 0 ){
                Item itm = Main.im.getWorldItemByID(t.itemID);
                if( itm != null ){
                    itm.unlock();
                }else{
                    //if this item is null there is a problem
                    throw new SlickException("Attempted to unlock a null item.");
                }
            }
        }
        //remove expired timers
        itemLockTimers.removeIf(b -> b.timer <= 0);
    }
    private Image getSpellImage(String material) throws SlickException{
    	if( material.equals("Ruby") ){
    		return ResourceManager.getImage(Main.SPELL_RED);
    	}else if( material.equals("Emerald") ){
    		return ResourceManager.getImage(Main.SPELL_GREEN);
    	}else if( material.equals("Amethyst") ){
    		return ResourceManager.getImage(Main.SPELL_PURPLE);
    	}else{
    		throw new SlickException("Invalid staff material '" + material + "'.");
    	}
    }

    private void attack(Item itm, Main dc, Vector direction) throws SlickException{
        //attack with the given item
        //Vector[] directions = {new Vector(0, -1), new Vector(0, 1), new Vector(-1, 0), new Vector(1, 0)};
        String dir = "";
        if( direction.equals(new Vector(0, -1) ) ){
            dir = "up";
        }else if( direction.equals(new Vector(0, 1)) ){
            dir = "down";
        }else if( direction.equals(new Vector(-1, 0)) ){
            dir = "left";
        }else if( direction.equals(new Vector(1, 0)) ){
            dir = "right";
        }else{
            throw new SlickException("Invalid attack direction " + dir);
        }

        if( itm == null || itm.getType().equals("Sword") || itm.getType().equals("Glove")){
            rand.setSeed(System.nanoTime());
            int r = rand.nextInt(100);
            if( r < 50 ){
                //slash
                dc.hero.updateAnimation("slash_" + dir);
                //addMessage("Slashed " + dir );
            }else{
                //jab
                dc.hero.updateAnimation("jab_" + dir );
                //addMessage("Jabbed " + dir );
            }

            for( Character c : dc.characters ){
                if( c.ai ){
                    //if the ai character is within one tilesize of the player
                    //in the given direction
//                    Vector aipos = c.animate.getPosition();
//                    Vector plpos = dc.hero.animate.getPosition();
                    Vector aipos = c.getWorldCoordinates();
                    Vector plpos = dc.hero.getWorldCoordinates();

                    double x = Math.pow(aipos.getX()-plpos.getX(), 2);
                    double y = Math.pow(aipos.getY()-plpos.getY(), 2);
                    float distance = (float) Math.sqrt(x + y);

                    if(  distance <= (dc.tilesize*1.5) ){
                        //roll damage amount
                        rand.setSeed(System.nanoTime());
                        float percentOfMaxDamage = rand.nextInt(100)/(float) 100;

                        float damage = 0;
                        if( itm == null ){
                        	//max damage: 10
                        	damage = 10 * percentOfMaxDamage;
                            
                        	//copy this block here instead of doing a bunch of null checks
                        	if( c.takeDamage(damage, "",false) ){
                                //returns true if the enemy died
                                c.updateAnimation("die");
                                
                                //c.vfx = null;

                            }

                            if( percentOfMaxDamage == 0 ){
                                addMessage("Missed.");
                            }else{
                                String m = "Hit enemy for " + (int) damage + " damage.";
                                if( percentOfMaxDamage >= 0.8 ){
                                    m = m + " Critical hit!";
                                }
                                addMessage(m);
                            }
                            return;
                        }else if( itm.getMaterial().equals("Wooden") ){
                            //max damage: 30
                            damage = 30 * percentOfMaxDamage;
                        }else if( itm.getMaterial().equals("Iron") ){
                            //max damage: 60
                            damage = 60 * percentOfMaxDamage;
                        }else if( itm.getMaterial().equals("Gold") ){
                            //max damage: 100
                            damage = 100 * percentOfMaxDamage;
                        }

                        //pass damage and effect to enemy
                        
                        if( itm.getEffect().equals("Might") ){
                        	damage *= 2;
                        }

                        if( c.takeDamage(damage, itm.getEffect(),false) ){
                            //returns true if the enemy died
                            c.updateAnimation("die");
                            
                            //destroy the character's vfx object
                            //c.vfx = null;

                        }

                        if( percentOfMaxDamage == 0 ){
                            addMessage("Missed.");
                        }else{
                            String m = "Hit enemy for " + (int) damage + " damage.";
                            if( percentOfMaxDamage >= 0.8 ){
                                m = m + " Critical hit!";
                            }
                            addMessage(m);
                        }

                        //reveal the effect to the character
                        // if it is not known
                        if( !itm.isIdentified() ){
                            addMessage("It is " + itm.toString() );
                            itm.identify();
                        }
                    }
                }
            }
            dc.characters.removeIf(b -> b.getHitPoints() <= 0);

        }else if( itm.getType().equals("Potion") ){
        	addThrownItem(dc, itm, itm.getImage(), direction, direction.scale(5), direction.scale(0.1f));
        	
            if( itm.count == 1 ){
            	dc.hero.unequipItem(selectedEquippedItem);
            }
            dc.hero.discardItem(itm, true);

        }else if( itm.getType().equals("Staff") ){
        	int manaCost = 0;
        	if( itm.getMaterial().equals("Ruby") ){
        		//high mana cost
        		manaCost = 20;
        	}else if( itm.getMaterial().equals("Emerald") ){
        		//medium mana cost
        		manaCost = 15;
        	}else if( itm.getMaterial().equals("Amethyst") ){
        		//low mana cost
        		manaCost = 5;
        	}else{
        		throw new SlickException("Invalid staff material '" + itm.getMaterial() + "'.");
        	}
        	
        	
        	if( dc.hero.getMana() < manaCost ){
        		addMessage("You don't have enough mana to use this.");
        		return;
        	}
        	
        	Image spellImage = getSpellImage(itm.getMaterial());
        	
        	/*
        	
        	//create a new Item for the spell
            Vector wc = new Vector((dc.hero.getWorldCoordinates().getX()/dc.tilesize)-0.5f, 
            		(dc.hero.getWorldCoordinates().getY()/dc.tilesize));
            
            Item spell = new Item( wc, true, -1, -1, itm.getEffect(), itm.getType(), "", false, true, spellImage, 1);
            
            //add it to the worldItems
            Main.im.addToWorldItems(spell);
            
            //make a new ThrownItem
            thrownItems.add(new ThrownItem(spell, direction, direction.scale(10000), direction.scale(0.3f) ) );
            */
        	
        	addThrownItem(dc, itm, spellImage, direction, direction.scale(10000), direction.scale(0.2f));
        	
        	//decrease mana
        	dc.hero.setMana(dc.hero.getMana() - manaCost);
        	
        }else if( itm.getType().equals("Arrow") ){
            //Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified, Image image)

        	Image image = getArrowImage(itm.getEffect(), dir);

        	addThrownItem(dc, itm, image, direction, direction.scale(10000), direction.scale(0.3f));
            
            if( itm.count == 1 ){
            	dc.hero.unequipItem(selectedEquippedItem);
            }
            dc.hero.discardItem(itm, true);
        }
    }
    
    private void addThrownItem(Main dc, Item emitter, Image image, Vector direction, Vector destination, Vector step) throws SlickException{
    	//Spawn item at the player's position
        Vector wc = new Vector((dc.hero.getWorldCoordinates().getX()/dc.tilesize)-0.5f, 
        		(dc.hero.getWorldCoordinates().getY()/dc.tilesize));
        
        //the actual item that will be thrown
        Item emissive = new Item(wc, true, -1, -1, emitter.getEffect(), emitter.getType(), "", false, true, image, 1);
        
        //add the emissive to the world items so it can be rendered
        Main.im.addToWorldItems(emissive);
        
        //create the thrown item
        thrownItems.add(new ThrownItem(emissive, direction, destination, step));
    }
    
    
    private boolean canUse(Item i, Character hero){
    	//return true if the hero can use an item
    	//  based on required level and class
    	//return false otherwise
    	
    	//check item level
    	if( i.getRequiredLevel() > hero.getStrength() ){
    		addMessage("You are not strong enough to use this.");
    		return false;
    	}
    	
    	//check class
    	String type = hero.getType().substring(0, hero.getType().indexOf("_")).toLowerCase();
    	for( String st : i.getRequiredClasses() ){
    		if( st.equals(type) ){
    			return true;
    		}
    	}
    	addMessage("This item is for a different class.");
    	return false;
    }

    private boolean throwItem(ThrownItem ti, Main dc){
        //if the item is not at the final location

        //these need to be floored because otherwise the item will go past its destination due to
        //  a small decimal difference
        Vector flooredWC = new Vector( (int) ti.itm.getWorldCoordinates().getX(), (int) ti.itm.getWorldCoordinates().getY() );
        Vector flooredDest = new Vector( (int) ti.finalLocation.getX(), (int) ti.finalLocation.getY() );

        if(  !flooredWC.equals(flooredDest) ){
            ti.itm.setWorldCoordinates(ti.itm.getWorldCoordinates().add(ti.step));
            return false;
        }

        return true;
    }


    // pause the game
    public void pause(Input input) {
        if (input.isKeyPressed(Input.KEY_P)) {
            paused = !paused;
        }
    }


    /*
    get the key being pressed, returns a string
     */
    public String getKeystroke(Input input) {
        String ks = "";
        if (input.isKeyDown(Input.KEY_W)) {
            ks = "w";
        }
        else if (input.isKeyDown(Input.KEY_S)) {
            ks = "s";
        }
        else if (input.isKeyDown(Input.KEY_A)) {
            ks = "a";
        }
        else if (input.isKeyDown(Input.KEY_D)) {
            ks = "d";
        }
        // cheat speed up
        else if (input.isKeyPressed(Input.KEY_4)) {
            ks = "4";
        }
        // cheat slow down
        else if (input.isKeyPressed(Input.KEY_5)) {
            ks = "5";
        }
        return ks;
    }

    /**
      * Update the players position on the server.
      */
    public void positionToServer(Main dc){
        float wx = dc.hero.getWorldCoordinates().getX();
        float wy = dc.hero.getWorldCoordinates().getY();
        Msg toServer = new Msg(serverId,dc.hero.getType(),wx,wy,dc.hero.getHitPoints());
        try {
            dos.writeObject(toServer);
            dos.flush();
            //System.out.println("Wrote 'toServer' type: "+ toServer.getClass().getSimpleName());
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
    Renders the map on screen, only drawing the necessary tiles in view
     */
    public void displayMap(Main dc, Graphics g) {
        for (BaseMap b : dc.maptiles) {
            b.render(g);
        }
    }

    public void updateOtherPlayers(Main dc){
        try {
            Msg read = (Msg)dis.readObject(); // message from server
            //System.out.println("reading 'read' type: " + read.getClass().getSimpleName());
            //System.out.println("("+serverId+") Read: " + read);

            if(read.type.equals("Exit")) {
                dc.characters.removeIf(c -> c.getPid() == read.id);
                return;
            }
            for(Iterator<Character> i = dc.characters.iterator();i.hasNext();){
                Character c = i.next();
                if(c.getPid() == read.id) {
                    if (c.getPid() == serverId) {
                        return;
                    }
                    c.setWorldCoordinates(new Vector(read.wx,read.wy));
                    c.setHitPoints(read.hp);
                    return;
                }

            }
            dc.characters.add(new Character(dc,read.wx,read.wy,read.type,read.id,this,false));
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
    /**
     * Check if the Character is in the Hero's screen +- one tile wide and high
     * @param dc the Main class
     * @param ch the Character instance to check if it is in region
     * @return true if the ch is in screen, else false
     */
    public boolean characterInRegion(Main dc, Character ch){
        float maxX = (dc.tilesize * dc.tilesWide) + dc.hero.pixelX + dc.tilesize;
        float maxY = (dc.tilesize * dc.tilesWide) + dc.hero.pixelY + dc.tilesize;
        float minX = dc.hero.pixelX - dc.tilesize;
        float minY = dc.hero.pixelY - dc.tilesize;
        Vector wc = ch.getWorldCoordinates();
        return minX <= wc.getX() && wc.getX() <= maxX && minY <= wc.getY() && wc.getY() <= maxY;
    }


    /**
     * Check if the object is in the Hero's screen +- one tile wide and high
     * @param dc the Main class
     * @param wc The world coordinate vector of the object
     * @return true if the ch is in screen, else false
     */
    public boolean objectInRegion(Main dc, Vector wc){
        float maxX = (dc.tilesize * dc.tilesWide) + dc.hero.pixelX + dc.tilesize;
        float maxY = (dc.tilesize * dc.tilesWide) + dc.hero.pixelY + dc.tilesize;
        float minX = dc.hero.pixelX - dc.tilesize;
        float minY = dc.hero.pixelY - dc.tilesize;
        return minX <= wc.getX() && wc.getX() <= maxX && minY <= wc.getY() && wc.getY() <= maxY;
    }


    /**
     * Convert the Characters world coordinates to screen coordinates
     * @param dc Main class
     * @param ai an AI character
     */
    public Vector world2screenCoordinates(Main dc, Character ai) {
        // screen coords = AI world coords - Hero's Screen Origin
        float sx = ai.getWorldCoordinates().getX() - dc.hero.pixelX;
        float sy = ai.getWorldCoordinates().getY() - dc.hero.pixelY;
        return new Vector(sx, sy);
    }

    /**
     * Convert the passed in world coordinates to screen coordinates
     * @param dc Main class
     * @param wc worldCoordinates vector of the object to render
     */
    public Vector world2screenCoordinates(Main dc, Vector wc) {
        // screen coords = AI world coords - Hero's Screen Origin
        float sx = wc.getX() - dc.hero.pixelX;
        float sy = wc.getY() - dc.hero.pixelY;
        return new Vector(sx, sy);
    }


    // TODO this will be called from the server side
    /**
     * Populate the world with AI characters
     */
    public void spawnEnemies(Main dc, int numItems) {
        int maxcol =  dc.map.length - 2;
        int maxrow = dc.map[0].length - 2;
        Random rand = new Random();
        while( numItems > 0 ){
            int col = rand.nextInt(maxcol);
            int row = rand.nextInt(maxrow);
            while(row < 2 || col < 2 || dc.map[col][row] == 1){
                col = rand.nextInt(maxcol) - 1;
                row = rand.nextInt(maxrow) - 1;
//                System.out.printf("getting %s, %s\n", col, row);
            }
//            System.out.printf("\nSpawning at %s, %s\n", col, row);
            float wx = (dc.tilesize * row) - dc.offset;
            float wy = (dc.tilesize * col) - dc.tilesize - dc.doubleOffset;
            dc.characters.add(new Character(dc, wx, wy, "skeleton_basic", (int) System.nanoTime(), this, true));

            //create a random item at the given position
            numItems--;
        }
    }



}
