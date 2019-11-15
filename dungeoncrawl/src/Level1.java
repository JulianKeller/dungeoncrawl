import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import jig.Entity;
import jig.Vector;

public class Level1 extends BasicGameState {
    private Boolean paused;
    Character knight;
    Vector currentOrigin;
    private Random rand;
    
    private int[][] rotatedMap;
    
    private final int messageTimer = 2000;
    
    private ArrayList<Item> itemsToRender;
    
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

    @Override
    public int getID() {
        return Main.LEVEL1;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        Main dc = (Main) game;
        paused = false;
        dc.width = dc.ScreenWidth/dc.tilesize;
        dc.height = dc.ScreenHeight/dc.tilesize;
        
        messagebox = new Message[messages]; //display four messages at a time


        //dc.map = RenderMap.getDebugMap(dc);
        ///*
        try {
            dc.map = RenderMap.getRandomMap(dc);        // grab a randomly generated map
        } catch (IOException e) {
            e.printStackTrace();
        }
        //*/
        dc.mapTiles = new Entity[dc.map.length][dc.map[0].length];      // initialize the mapTiles


        

        
  
        
        //rotated map verified correct
        rotatedMap = new int[dc.map[0].length][dc.map.length];
        for( int i = 0; i < dc.map.length; i++ ){
        	for( int j = 0; j < dc.map[i].length; j++ ){
        		//g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
        		//i is y, j is x
        		rotatedMap[j][i] = dc.map[i][j];
        	}
        }
        
        Main.im.plant(5, rotatedMap);
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
        
        float wx = (dc.tilesize * col) - dc.offset;// - dc.xOffset;
        float wy = (dc.tilesize * row) - dc.tilesize - dc.doubleOffset;// - dc.doubleOffset;// - dc.yOffset;
        
        knight = new Character(dc, wx, wy, "knight_iron", 1);
        dc.characters.add(knight);
        
        currentOrigin = knight.origin;
        RenderMap.setMap(dc, knight.origin);                   // renders the map Tiles
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
        // TODO only render in the screen
//        int origin = knight.screenOrigin;     // TODO this is equal to the players offset
//        int h = dc.height + origin;
//        int w = dc.width + origin;
        for (int i = 0; i < dc.map.length; i++) {
            for (int j = 0; j < dc.map[i].length; j++) {
                if (dc.mapTiles[i][j] == null)
                    continue;
                dc.mapTiles[i][j].render(g);
            }
        }
        
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

        // render potions
//        for (int i = 0; i < dc.potions.length; i++) {
//            for (int j = 0; j < dc.potions[0].length; j++) {
//                if (dc.potions[i][j] == null)
//                    continue;
//                dc.potions[i][j].render(g);
//            }
//        }

        knight.animate.render(g);
        
        
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



    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Main dc = (Main) game;
        Cheats.enableCheats(dc, input);
        pause(input);
        if (paused) {
            return;
        }
        knight.move(getKeystroke(input));
        if (currentOrigin.getX() != knight.origin.getX() && currentOrigin.getY() != knight.origin.getY()) {
            RenderMap.setMap(dc, knight.origin);
            currentOrigin = knight.origin;
        }
        
        //check if a character has hit an item
        for( Character ch : dc.characters ){
        	float x = (ch.animate.getX()/dc.tilesize);
        	float y = (ch.animate.getY()/dc.tilesize);
        	Vector aniPos = new Vector((int)x, (int)y);
        	
	        Item i = Main.im.getItemAt(aniPos);
	        if( i != null ){
	        	if( i.isIdentified() ){
	        		addMessage("Picked up " + i.getMaterial() + " " +i.getType() + " of " + i.getEffect() + ".");
	        	}else{
	        		addMessage("Picked up unidentified "+i.getType().toLowerCase()+".");
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
        String ks = null;
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
        return ks;
    }



}
