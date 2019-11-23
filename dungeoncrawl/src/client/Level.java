package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import jig.Vector;

import java.net.*;
import java.io.*;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import jig.Vector;


public class Level extends BasicGameState {
    private Boolean paused;
    //Character dc.hero;

    int currentOX;
    int currentOY;

    Vector currentOrigin;

    private Random rand;
    
    private int[][] rotatedMap;

    Socket socket;
    ObjectInputStream dis;
    ObjectOutputStream dos;
    String serverMessage;
    
    private final int messageTimer = 2000;

    
    private ArrayList<Item> itemsToRender;
    
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

    @Override
    public int getID() {
        return Main.LEVEL1;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        serverMessage = "";
        Main dc = (Main) game;
        if(dc.socket == null){
            System.out.println("ERROR: Make sure you start the server before starting the client!");
            System.exit(1);
        }
        paused = false;
        dc.tilesWide = dc.ScreenWidth/dc.tilesize;
        dc.tilesHigh = dc.ScreenHeight/dc.tilesize;

        messagebox = new Message[messages]; //display four messages at a time

            // TODO This section is the original map generator.
//            dc.map = client.RenderMap.getDebugMap(dc);
//            try {
//                dc.map = client.RenderMap.getRandomMap();        // grab a randomly generated map
//            } catch (IOException e) {
//                e.printStackTrace();
//            // server.Server sockets for reading/writing to server.

        this.socket = dc.socket;
        this.dis = dc.dis;
        this.dos = dc.dos;


        //dc.map = RenderMap.getDebugMap(dc);
        ///*


        // TODO this section requires that you run the server prior to client.Main.
        // Grab the map from the server.Server

        try {
            Integer[][] mapData = (Integer[][]) this.dis.readObject();
            // Convert it into an 2d int array
            dc.map = new int[mapData.length][mapData[0].length];
            for (int i = 0; i < mapData.length; i++) {
                for (int j = 0; j < mapData[i].length; j++) {
                    dc.map[i][j] = mapData[i][j];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //*/
        //dc.mapTiles = new Entity[dc.map.length][dc.map[0].length];      // initialize the mapTiles
        System.out.printf("Map Size: %s, %s\n", dc.map.length, dc.map[0].length);



        

        
  
        
        //rotated map verified correct
        rotatedMap = new int[dc.map[0].length][dc.map.length];
        for( int i = 0; i < dc.map.length; i++ ){
        	for( int j = 0; j < dc.map[i].length; j++ ){
        		//g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
        		//i is y, j is x
        		rotatedMap[j][i] = dc.map[i][j];
        	}
        }
        
        try {
			Main.im.plant(5, rotatedMap);
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));
        
        //find a tile with no walls in its horizontal adjacencies
        rand = new Random();
        rand.setSeed(System.nanoTime());
        
        int row = rand.nextInt(dc.ScreenHeight/dc.tilesize);
		int col = rand.nextInt(dc.ScreenWidth/dc.tilesize);

		while( dc.map[row][col] == 1 || wallAdjacent(row, col, dc.map) ){
			//spawn on a floor tile with 4 adjacent floor tiles
	        row = rand.nextInt(dc.ScreenHeight/dc.tilesize);
			col = rand.nextInt(dc.ScreenWidth/dc.tilesize);
		}

        // map variables
        dc.mapWidth = dc.map[0].length;
        dc.mapHeight = dc.map.length;

        // setup the dc.hero character
        float wx = (dc.tilesize * 20) - dc.offset;
        float wy = (dc.tilesize * 18) - dc.tilesize - dc.doubleOffset;
        System.out.printf("setting character at %s, %s\n", wx, wy);
        dc.hero = new Character(dc, wx, wy, "knight_iron", 1, false);
        dc.characters.add(dc.hero);
        currentOX = dc.hero.ox;
        currentOY = dc.hero.oy;

        // render map
        RenderMap.setMap(dc, dc.hero);

        String coord = wx + " " + wy;
        try {
            dos.writeUTF(coord);
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }

        dc.hero = new Character(dc, wx, wy, "knight_leather", 1, false);

        wx = (dc.tilesize * 20) - dc.offset;
        wy = (dc.tilesize * 16) - dc.tilesize - dc.doubleOffset;
        dc.characters.add(new Character(dc, wx, wy, "skeleton_basic", 2, true));

        //dc.characters.add(dc.hero);
        currentOX = dc.hero.ox;
        currentOY = dc.hero.oy;

        // render map
        RenderMap.setMap(dc, dc.hero);



        itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));

        // test items can be deleted
        /*
        dc.testItems.add(new DisplayItem((dc.tilesize * 4)- dc.offset, (dc.tilesize * 4)- dc.offset, "armor_gold"));
        dc.testItems.add(new DisplayItem((dc.tilesize * 5)- dc.offset, (dc.tilesize * 4)- dc.offset, "armor_iron"));
        dc.testItems.add(new DisplayItem((dc.tilesize * 6)- dc.offset, (dc.tilesize * 4)- dc.offset, "sword_iron"));
        dc.testItems.add(new DisplayItem((dc.tilesize * 7)- dc.offset, (dc.tilesize * 4)- dc.offset, "sword_wood"));
        dc.testItems.add(new DisplayItem((dc.tilesize * 8)- dc.offset, (dc.tilesize * 4)- dc.offset, "sword_gold"));
        */

//        dc.testItems.add(new DisplayItem((dc.tilesize * 14)- dc.offset, (dc.tilesize * 4)- dc.offset, "arrow_ice"));
//        dc.testItems.add(new DisplayItem((dc.tilesize * 15)- dc.offset, (dc.tilesize * 4)- dc.offset, "arrow_poison"));
//        dc.testItems.add(new DisplayItem((dc.tilesize * 16)- dc.offset, (dc.tilesize * 4)- dc.offset, "arrow_flame"));
//        dc.testItems.add(new DisplayItem((dc.tilesize * 17)- dc.offset, (dc.tilesize * 4)- dc.offset, "arrow_normal"));
//        dc.testItems.add(new DisplayItem((dc.tilesize * 8)- dc.offset, (dc.tilesize * 4)- dc.offset, "arrow_normal"));

        
        /*
        currentOrigin = dc.hero.origin;
        RenderMap.setMap(dc, dc.hero.origin);                   // renders the map Tiles
        */
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

    	//TODO: make the restoration boundary cover only the screen area + a buffer
    	itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));
    }


    public void addMessage(String message){
    	//add a message to the first index of the message box
    	//  and shift everything else down
    	for( int i = messagebox.length-1; i > 0; i-- ){
    		messagebox[i] = messagebox[i-1];
    	}
    	messagebox[0] = new Message(message);
    }
    

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dc = (Main) game;
        // render tiles
        /*
        for (int i = 0; i < dc.map.length; i++) {
            for (int j = 0; j < dc.map[i].length; j++) {
                if (dc.mapTiles[i][j] == null)
                    continue;
                dc.mapTiles[i][j].render(g);
            }
        }
        */
        displayMap(dc, g);
        
        //render all visible items
        g.setColor(Color.red);
        for( Item i : itemsToRender){
        	//System.out.println("Drawing item at "+i.getWorldCoordinates().getX()+", "+i.getWorldCoordinates().getY());
        	//TODO: draw item images
        	//for now, use ovals
        	//g.drawOval((i.getWorldCoordinates().getX()*dc.tilesize)+(dc.tilesize/2), (i.getWorldCoordinates().getY()*dc.tilesize)+(dc.tilesize/2), 4, 4);
        	
        	i.setPosition((i.getWorldCoordinates().getX()*dc.tilesize)+(dc.tilesize/2), (i.getWorldCoordinates().getY()*dc.tilesize)+(dc.tilesize/2));
        	i.render(g);
        	
        }

        // draw test items
        for (DisplayItem i : dc.testItems) {
            i.render(g);
        }


        // TODO will need to sort the lists and draw in order
        // draw other characters
        renderCharacters(dc, g);

        // draw the hero
        dc.hero.animate.render(g);

        //render messages
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

        
        //display player inventory
        //use the dc.hero for now
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
            	g.setColor(tmp);
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
        		g.drawString(i.getMaterial()+" "+i.getType()+" of "+i.getEffect(), dc.tilesize*2, dc.tilesize*row + dc.tilesize*0.25f);
        		g.setColor(tmp);
        		row++;
        	}
        }
        
        //draw the player's equipped items
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
        	}
        }
        g.setColor(Color.white);
        g.drawRect(x+(dc.tilesize*selectedEquippedItem), y, dc.tilesize, dc.tilesize);
        g.setColor(tmp);
        
        
        
        
        //print the tile values on the screen
        /*
        for( int i = 0; i < dc.map.length; i++ ){
        	for( int j = 0; j < dc.map[i].length; j++ ){
        		g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
        		//i is y, j is x
        	}
        }
        //*/
        
        /*
        if( rotatedMap != null ){
        	for( int i = 0; i < rotatedMap.length; i++ ){
        		for( int j = 0; j < rotatedMap[i].length; j++ ){
        			g.drawString(""+rotatedMap[i][j], i*dc.tilesize, j*dc.tilesize);
        		}
        	}
        }
        //*/
        
    }

    /*
    Renders the other characters and AI on the screen if they are in the players screen
     */
    private void renderCharacters(Main dc, Graphics g) {
        for (Character ch : dc.characters) {
            Vector sc = world2screenCoordinates(dc, ch);
            ch.animate.setPosition(sc);
            if (characterInRegion(dc, ch)) {
                ch.animate.render(g);
            }
        }
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



    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Main dc = (Main) game;
        Cheats.enableCheats(dc, input);
        pause(input);
        if (paused) {
            return;
        }
        dc.hero.move(getKeystroke(input));
        /*
        if (currentOrigin.getX() != dc.hero.origin.getX() && currentOrigin.getY() != dc.hero.origin.getY()) {
            RenderMap.setMap(dc, dc.hero.origin);
            currentOrigin = dc.hero.origin;
        }
        */
        
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
        		String m = dc.hero.equipItem((itemy*4)+itemx);
        		if( m != null ){
        			addMessage(m);
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
        		//TODO: add use functionality
        		//throw/attack with item (i.e. throw potion or swing sword)
        		addMessage("threw "+dc.hero.getEquipped()[selectedEquippedItem]+".");
        	}else if( input.isKeyPressed(Input.KEY_APOSTROPHE) ){
        		//use item on own character
        		Item i = dc.hero.getEquipped()[selectedEquippedItem];
        		String x = "";
        		if( i.getType().equals("Potion") ){
        			x = "Drank";
        		}else if( i.getType().equals("Armor") ){
        			x = "Put on";
        		}else{
        			x = "Used";
        		}
        		addMessage(x + " " + i.getMaterial() + " " + i.getType() + " of " + i.getEffect() );
        		//TODO: add potion effects to character
        		if( i.getType().equals("Potion") || i.getType().equals("Armor") ){
        			//add effect to character
        			dc.hero.addEffect(i.getEffect());
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
        		}
        		
        	}else if( input.isKeyPressed(Input.KEY_BACKSLASH) ){
        		
        		
        		Item itm = dc.hero.getEquipped()[selectedEquippedItem];
        		dc.hero.unequipItem(selectedEquippedItem);
        		

        		
        		//place the item on the world at the dc.hero's position
        		//get world coords of dc.hero position 
        		Vector wc = new Vector((int) dc.hero.animate.getX()/dc.tilesize, (int) dc.hero.animate.getY()/dc.tilesize);
        		System.out.println("placing item at "+wc.getX() + ", " + wc.getY());
        		//this will remove the item from the dc.hero's inventory and place it on the world
        		
        		Main.im.take(itm.getID(), dc.hero.getPid(), wc, false);
        		
        		//lock the item with a timer
        		itm.lock();
        		//add item lock timer
        		itemLockTimers.add( new ItemLockTimer(itm.getID()) );
        		System.out.println("Locked dropped item.");
    
        		//add the item to the render list
        		itemsToRender.add(itm);
        		
        		addMessage("Dropped "+dc.hero.getEquipped()[selectedEquippedItem]+".");
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
        

        // cause AI players to move around
        for( Character ch : dc.characters ) {
            if (ch.ai) {        // if the player is an AI player, move them
                ch.moveAI();
            }
        }

        //check if a character has hit an item
        for( Character ch : dc.characters ){
        	float x = (ch.animate.getX()/dc.tilesize);
        	float y = (ch.animate.getY()/dc.tilesize);
        	Vector aniPos = new Vector((int)x, (int)y);
        	
	        Item i = Main.im.getItemAt(aniPos);
	        if( i != null && !i.isLocked() ){
	        	if( i.isIdentified() ){
	        		addMessage("Picked up " + i.getMaterial() + " " +i.getType() + " of " + i.getEffect() + ".");
	        	}else{
	        		addMessage("Picked up unidentified "+i.getMaterial().toLowerCase()+" "+i.getType().toLowerCase()+".");
	        	}
	        	//give removes item from the world's inventory
	        	//  and adds it to the player's inventory
	        	Main.im.give(i.getID(), ch.getPid());
	        	
	        	//stop rendering the item
	        	itemsToRender.remove(i);
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


    // pause the game
    public void pause(Input input) {
        if (input.isKeyPressed(Input.KEY_SPACE)) {
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
        try{
            dos.writeUTF(ks);
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
        return ks;
    }
    
    /**
     * Update the players position on the server.
     */
   public void positionToServer(Vector wc){
       String position = wc.getX() + " " + wc.getY();
      //System.out.println("Client position: "+ position);
       try {
           this.dos.writeUTF(position);
           this.dos.flush();
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
        String update = "";
        boolean isRendered = false;
        try {
            update = dis.readUTF();
             //Return if there are no other players.
                if(update.equals("1")){
                    //System.out.println("No other clients.");
                    return;
                }
                int num_characters = Integer.parseInt(update);
                int id = 0;
                    update = dis.readUTF();
                    //System.out.println(update);
                    id = Integer.parseInt(update.split(" ")[1]);
                    // Go through and check to see if the character already exists.
                    for (Iterator<Character> i = dc.characters.iterator(); i.hasNext(); ) {
                        Character c = i.next();
                        if (c.getPid() == id) {
                            c.animate.setPosition(Float.parseFloat(update.split(" ")[2]),
                                    Float.parseFloat(update.split(" ")[3]));
                            isRendered = true;
                            break;
                        }
                    }
                    if (!isRendered) {
                        Float wx = Float.parseFloat(update.split(" ")[2]);
                        Float wy = Float.parseFloat(update.split(" ")[3]);
                        String type = update.split(" ")[0];
                        dc.characters.add(new Character(dc, wx, wy, type, id, false));
                        return;
                    }
        } catch(IOException e){
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
     * @param dc Main class
     * @param ai an AI character
     * TODO update the ai characters from the render method
     *      only care about keeping track of enemies world position.
     *      Then in the render method, I convert world coordinates to screen coordinates
     *      If the ai is in the screen, then render, else don't render
     */
    public Vector world2screenCoordinates(Main dc, Character ai) {
        // screen coords = AI world coords - Hero's Screen Origin
        float sx = ai.getWorldCoordinates().getX() - dc.hero.pixelX;
        float sy = ai.getWorldCoordinates().getY() - dc.hero.pixelY;
        return new Vector(sx, sy);
    }


    /**
     * Reads the new player coordinates and walks the player accordingly.
//     */
//    public void getNewPlayerCoord(){
//        try {
//            serverMessage = dis.readUTF();
//            if (!serverMessage.equals("")) {
////                System.out.println("server.Server says: Move valid.  New coordinates: "+ serverMessage);
//                dc.hero.moveSmoothTranslationHelper();
//            } else{
////                System.out.println("server.Server says: Move invalid/No Button Pressed.");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
