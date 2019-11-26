package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import jig.ResourceManager;
import jig.Vector;


public class Level extends BasicGameState {
    private Boolean paused;
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

        // initialize itemsToRender
        itemsToRender = new ArrayList<>();
//        itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));
        
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
        
		/*
        float wx = (dc.tilesize * col) - dc.offset;// - dc.xOffset;
        float wy = (dc.tilesize * row) - dc.tilesize - dc.doubleOffset;// - dc.doubleOffset;// - dc.yOffset;
        
        dc.hero = new Character(dc, wx, wy, "dc.hero_iron", 1);
        dc.characters.add(dc.hero);
        */
        // map variables
    	//Main dc = (Main) game;
        dc.mapWidth = dc.map[0].length;
        dc.mapHeight = dc.map.length;

        // setup the dc.hero character
        float wx = (dc.tilesize * 20) - dc.offset;
        float wy = (dc.tilesize * 18) - dc.tilesize - dc.doubleOffset;
        System.out.printf("setting character at %s, %s\n", wx, wy);
        
        dc.hero = new Character(dc, wx, wy, "knight_leather", 1, this, false);
        
        //give the hero leather armor with no effect
        //public Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified, Image image)
        /*
        Item a = new Item(null, false, Main.im.getCurrentItemID(), dc.hero.getPid(), "", "Armor", "Leather", false, true, 
        		ResourceManager.getImage(Main.ARMOR_IRON));
        
        Main.im.give(a, dc.hero);
        */
        dc.characters.add(dc.hero);
        //currentOX = dc.hero.ox;
        //currentOY = dc.hero.oy;
        
                // setup a skeleton enemy
        wx = (dc.tilesize * 20) - dc.offset;
        wy = (dc.tilesize * 16) - dc.tilesize - dc.doubleOffset;
        dc.characters.add(new Character(dc, wx, wy, "skeleton_basic", 2, this, true));

        // render map
        RenderMap.setMap(dc, dc.hero);

        String coord = wx + " " + wy;
        try {
            dos.writeUTF(coord);
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }

        // render map
        RenderMap.setMap(dc, dc.hero);

        itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));
        
        
        thrownItems = new ArrayList<ThrownItem>();

        // test items can be deleted
        /*
        dc.testItems.add(new DisplayItem((dc.tilesize * 4)- dc.offset, (dc.tilesize * 4)- dc.offset, "armor_gold"));
        dc.testItems.add(new DisplayItem((dc.tilesize * 5)- dc.offset, (dc.tilesize * 4)- dc.offset, "armor_iron"));
        dc.testItems.add(new DisplayItem((dc.tilesize * 6)- dc.offset, (dc.tilesize * 4)- dc.offset, "sword_iron"));
        dc.testItems.add(new DisplayItem((dc.tilesize * 7)- dc.offset, (dc.tilesize * 4)- dc.offset, "sword_wood"));
        dc.testItems.add(new DisplayItem((dc.tilesize * 8)- dc.offset, (dc.tilesize * 4)- dc.offset, "sword_gold"));
        */
        
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
        renderItems(dc, g);

        // draw test items
        renderTestItems(dc, g);

        // TODO will need to sort the lists and draw in order so players draw on top of others
        // draw other characters
        renderCharacters(dc, g);

        // draw the hero
        dc.hero.animate.render(g);

        //render messages
        renderMessages(dc, g);

        //display player inventory
        //use the dc.hero for now
        renderInventory(dc, g);

        // render the codex
        renderCodex(dc, g);
        
         //draw the player's equipped items
        renderEquippedItems(dc, g);
        
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
            	try{
	            	Item sItem = items.get((itemy*4)+itemx);
	            	if( sItem.isIdentified() ){
	            		g.drawString(sItem.getMaterial() + " " + sItem.getType() + " of " + sItem.getEffect(), dc.tilesize+10, dc.tilesize*8);
	            	}else{
	            		g.drawString("Unidentified " + sItem.getMaterial() + " " + sItem.getType(), dc.tilesize+10, (dc.tilesize*8)+(dc.tilesize/4));          		
	            	}
            	}catch(IndexOutOfBoundsException ex){
            		
            	}
            	g.setColor(tmp);
        	}
        }
        //draw the player's equipped items
        renderEquippedItems(dc, g);

//         renderDebug(dc, g);
        
        
        //minimal HUD
        g.drawString("HP: " + dc.hero.getHitPoints(), dc.ScreenWidth-150, dc.ScreenHeight-(dc.tilesize*3));
        g.drawString("Mana: " + dc.hero.getMana(), dc.ScreenWidth-150, dc.ScreenHeight-(dc.tilesize*4));
        g.drawString("Strength: "+dc.hero.getStrength(), dc.ScreenWidth-150, dc.ScreenHeight-(dc.tilesize*5));
        g.drawString("Speed: "+dc.hero.getMovementSpeed(), dc.ScreenWidth-150, dc.ScreenHeight-(dc.tilesize*6));
        
        
        

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
        		g.drawString(i.getMaterial()+" "+i.getType()+" of "+i.getEffect(), dc.tilesize*2, dc.tilesize*row + dc.tilesize*0.25f);
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
                g.drawString(i.getMaterial()+" "+i.getType()+" of "+i.getEffect(), dc.tilesize*2, dc.tilesize*row + dc.tilesize*0.25f);
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
        for (DisplayItem i : dc.testItems) {
            i.render(g);
        }
    }

    /*
    Renders the other characters and AI on the screen if they are in the players screen
     */
    private void renderCharacters(Main dc, Graphics g) {
        for (Character ch : dc.characters) {
            Vector sc = world2screenCoordinates(dc, ch.getWorldCoordinates());
            ch.animate.setPosition(sc);
            if (characterInRegion(dc, ch)) {
                ch.animate.render(g);
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
        
        //implement effects on the character
        dc.hero.implementEffects();
        //reduce the effect timers by a constant value each frame
        //  if delta is used instead of a constant, tabbing away from
        //  the game window can cause all effects to disappear instantly
        dc.hero.updateEffectTimers(16);
        
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
        /*
        if (currentOrigin.getX() != dc.hero.origin.getX() && currentOrigin.getY() != dc.hero.origin.getY()) {
            RenderMap.setMap(dc, dc.hero.origin);
            currentOrigin = dc.hero.origin;
        }
        */
        
        //cheat code to apply any effect to the character
        if( input.isKeyPressed(Input.KEY_LALT) ){
        	System.out.println("Game window frozen, expecting user input.");
        	System.out.println("Enter valid effect name (e.g. Healing): ");
        	
        	String effect = scan.next().trim();
        	
        	if( effect.equals("Iron") ){
        		effect = "Iron Skin";
        	}
        	System.out.println("Got effect '"+effect+"'");
        	
        	dc.hero.addEffect(effect);
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
        		String m = dc.hero.equipItem((itemy*4)+itemx);
        		itemx = 0;
        		itemy = 0;
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
        		//attack with item
        		//System.out.println("Attacking with " + dc.hero.getEquipped()[selectedEquippedItem].getType() );
        		attack(dc.hero.getEquipped()[selectedEquippedItem], dc, lastKnownDirection);
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
        			
        			//set the hero type to change the armor
        			if( dc.hero.getType().contains("knight") ){
        				dc.hero.setType("knight_"+i.getMaterial().toLowerCase());
        			}else if( dc.hero.getType().contains("tank") ){
        				dc.hero.setType("tank_"+i.getMaterial().toLowerCase());
        			}
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
                // TODO may need to be changed to be added to worldItems
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
        
        //advance any thrown items along their path
        ArrayList<ThrownItem> reachedDestination = new ArrayList<ThrownItem>();
        for( ThrownItem ti : thrownItems ){
        	
        	//check if a thrown item went off the screen
        	if( ti.itm.getWorldCoordinates().getX() < 0 || ti.itm.getWorldCoordinates().getX() > dc.ScreenWidth/dc.tilesize){
        		reachedDestination.add(ti);
        	}else if( ti.itm.getWorldCoordinates().getY() < 0 || ti.itm.getWorldCoordinates().getY() > dc.ScreenHeight/dc.tilesize){
        		reachedDestination.add(ti);
        	}
        	if( throwItem(ti, dc) ){
        		reachedDestination.add(ti);
        	}
        	
        	//check if a thrown item hit a character
        	for( Character ch : dc.characters){
        		
        		if( ch.getPid() == dc.hero.getPid() ){
        			continue;
        		}
        		
        		Vector chwc = new Vector( ch.animate.getX()/dc.tilesize, ch.animate.getY()/dc.tilesize);
        	
        		//System.out.println(chwc.toString());
        		
        		if( Math.abs(chwc.getX() - ti.itm.getWorldCoordinates().getX()) < 1.5 && Math.abs(chwc.getY() - ti.itm.getWorldCoordinates().getY()) < 1.5 ){
        			addMessage("thrown " + ti.itm.getType() + " hit enemy");
        			
        			//potions do no damage but cause status effects on the target
        			ch.takeDamage(0, ti.itm.getEffect());
        			dc.hero.addToCodex(ti.itm);
        			reachedDestination.add(ti);
        			//Main.im.removeFromWorldItems(ti.itm);
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
    	
    	if( itm.getType().equals("Sword") ){
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
					Vector aipos = c.animate.getPosition();
					Vector plpos = dc.hero.animate.getPosition();
					
					double x = Math.pow(aipos.getX()-plpos.getX(), 2); 
					double y = Math.pow(aipos.getY()-plpos.getY(), 2);
					float distance = (float) Math.sqrt(x + y);
					
    				if(  distance <= (dc.tilesize*1.5) ){
    					//roll damage amount
    					rand.setSeed(System.nanoTime());
    					float percentOfMaxDamage = rand.nextInt(100)/(float) 100;
    					
    					float damage = 0;
    					if( itm.getMaterial().equals("Wooden") ){
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
    					
    					if( c.takeDamage(damage, itm.getEffect()) ){
    						//returns true if the enemy died
    						c.updateAnimation("die");
    						
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
    						addMessage("It is " + itm.getType() + " of " + itm.getEffect() );
    						itm.identify();
    					}
    				}
				}
			}
			dc.characters.removeIf(b -> b.getHitPoints() <= 0);
			
    	}else if( itm.getType().equals("Potion") ){
    		//ranged attack, need to throw potion
    		//throw potion image 5 tiles in the direction the character
    		//  is facing
    		
    		Vector heroWC = new Vector( (int) (dc.hero.animate.getX()/dc.tilesize), (int) (dc.hero.animate.getY()/dc.tilesize));
    		
    		itm.setWorldCoordinates(heroWC);
    		
    		dc.hero.unequipItem(selectedEquippedItem);
    		
    		
    		//throw the potion but do not identify it unless it hits an enemy
    		Main.im.take(itm, dc.hero, itm.getWorldCoordinates(), false);
    		
    		Vector destination = heroWC.add(direction.scale(5));

    		thrownItems.add(new ThrownItem(itm, direction, destination, direction.scale(0.1f)));
    		
    	}else if( itm.getType().equals("Staff") ){
    		
    	}else if( itm.getType().equals("Arrow") ){
    		//Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified, Image image)
    		
    		
    		
    		//get image based on effect and direction
    		Image image = null;
    		if( dir.equals("up") ){
    			if( itm.getEffect().equals("Poison") ){
    				image = ResourceManager.getImage(Main.ARROW_POISON_UP);
    			}else if( itm.getEffect().equals("Flame") ){
    				image = ResourceManager.getImage(Main.ARROW_FLAME_UP);
    			}else if( itm.getEffect().equals("Ice") ){
    				image = ResourceManager.getImage(Main.ARROW_ICE_UP);
    			}else if( itm.getEffect().equals("") ){
    				image = ResourceManager.getImage(Main.ARROW_NORMAL_UP);
    			}
    		}else if( dir.equals("down") ){
    			if( itm.getEffect().equals("Poison") ){
    				image = ResourceManager.getImage(Main.ARROW_POISON_DOWN);
    			}else if( itm.getEffect().equals("Flame") ){
    				image = ResourceManager.getImage(Main.ARROW_FLAME_DOWN);
    			}else if( itm.getEffect().equals("Ice") ){
    				image = ResourceManager.getImage(Main.ARROW_ICE_DOWN);
    			}else if( itm.getEffect().equals("") ){
    				image = ResourceManager.getImage(Main.ARROW_NORMAL_DOWN);
    			}
    		}else if( dir.equals("left") ){
    			if( itm.getEffect().equals("Poison") ){
    				image = ResourceManager.getImage(Main.ARROW_POISON_LEFT);
    			}else if( itm.getEffect().equals("Flame") ){
    				image = ResourceManager.getImage(Main.ARROW_FLAME_LEFT);
    			}else if( itm.getEffect().equals("Ice") ){
    				image = ResourceManager.getImage(Main.ARROW_ICE_LEFT);
    			}else if( itm.getEffect().equals("") ){
    				image = ResourceManager.getImage(Main.ARROW_NORMAL_LEFT);
    			}
    		}else if( dir.equals("right") ){
    			if( itm.getEffect().equals("Poison") ){
    				image = ResourceManager.getImage(Main.ARROW_POISON_RIGHT);
    			}else if( itm.getEffect().equals("Flame") ){
    				image = ResourceManager.getImage(Main.ARROW_FLAME_RIGHT);
    			}else if( itm.getEffect().equals("Ice") ){
    				image = ResourceManager.getImage(Main.ARROW_ICE_RIGHT);
    			}else if( itm.getEffect().equals("") ){
    				image = ResourceManager.getImage(Main.ARROW_NORMAL_RIGHT);
    			}
    		}
    		if( image == null ){
    			throw new SlickException("Invalid arrow effect " + itm.getEffect());
    		}
    		
    		//spawn at the world coordinate of the player's animation
    		Vector wc = new Vector(dc.hero.animate.getX()/dc.tilesize, (dc.hero.animate.getY()/dc.tilesize)-1);
    		Item flyingArrow = new Item(wc, true, -1, -1, itm.getEffect(), itm.getType(), "", false, true, image);
    		
    		//add this to the list of items so it can be rendered
    		Main.im.addToWorldItems(flyingArrow);
    		
    		//this thrown item should travel until it hits a wall or an enemy
    		//  thus the final destination is effectively infinite (past the level boundary)
    		thrownItems.add( new ThrownItem(flyingArrow, direction, direction.scale(10000), direction.scale(0.1f) )); 
    	}
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
	            dc.characters.add(new Character(dc, wx, wy, type, id, this, false));
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


}
