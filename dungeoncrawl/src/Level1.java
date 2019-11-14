import jig.Entity;
import jig.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class Level1 extends BasicGameState {
    private Boolean paused;
    Character knight;
    Vector currentOrigin;
    Socket socket;
    ObjectInputStream dis;
    ObjectOutputStream dos;
    String serverMessage;

    
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
        serverMessage = "";
        Main dc = (Main) game;
        paused = false;
        dc.width = dc.ScreenWidth/dc.tilesize;
        dc.height = dc.ScreenHeight/dc.tilesize;
        
        messagebox = new Message[messages]; //display four messages at a time

        // TODO This section is the original map generator.
        if(Main.localMode) {
            dc.map = RenderMap.getDebugMap(dc);
            try {
                dc.map = RenderMap.getRandomMap();        // grab a randomly generated map
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Server sockets for reading/writing to server.
            this.socket = dc.socket;
            this.dis = dc.dis;
            this.dos = dc.dos;

            dc.width = dc.ScreenWidth / dc.tilesize;
            dc.height = dc.ScreenHeight / dc.tilesize;
            // TODO this section requires that you run the server prior to Main.
            // Grab the map from the Server
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
        }
        dc.mapTiles = new Entity[dc.map.length][dc.map[0].length];      // initialize the mapTiles


        // TODO can be removed. Just here to display potions
//        dc.potions = new Entity[dc.map.length][dc.map[0].length];   // TODO make arraylist
//        dc.potions[10][10] = new Potion(10 * dc.tilesize + dc.tilesize/2, 10 * dc.tilesize + dc.tilesize/2, "health");
//        dc.potions[10][4] = new Potion(10 * dc.tilesize + dc.tilesize/2, 4 * dc.tilesize + dc.tilesize/2, "manna");
//        dc.potions[10][8] = new Potion(10 * dc.tilesize + dc.tilesize/2, 8 * dc.tilesize + dc.tilesize/2, "fire");
//        dc.potions[10][2] = new Potion(10 * dc.tilesize + dc.tilesize/2, 2 * dc.tilesize + dc.tilesize/2, "strength");
//        dc.potions[10][6] = new Potion(10 * dc.tilesize + dc.tilesize/2, 6 * dc.tilesize + dc.tilesize/2, "invisibility");

        // TODO can be removed, here to demo animations/characters
        dc.animations = new ArrayList<>(200);
//        AnimateEntity.testAllCharacterAnimations(dc);

        float wx = (dc.tilesize * 4) - dc.offset;// - dc.xOffset;
        float wy = (dc.tilesize * 4) - dc.tilesize - dc.doubleOffset;// - dc.doubleOffset;// - dc.yOffset;
        knight = new Character(dc, wx, wy, "knight_iron", 1);
        String coord = wx + " " + wy;
        if(!Main.localMode) {
            try {
                dos.writeUTF(coord);
                dos.flush();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        
        dc.characters.add(knight);
        
        currentOrigin = knight.origin;
        RenderMap.setMap(dc, knight.origin);                   // renders the map Tiles
        
        itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));
    }


    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
    	messagebox = new Message[messages]; //display four messages at a time
    	//plant some items on the level
		Main.im.plant(5);
    	
    	//then restore the visible items from the world
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
        	g.drawOval((i.getWorldCoordinates().getX()*dc.tilesize)+(dc.tilesize/2), (i.getWorldCoordinates().getY()*dc.tilesize)+(dc.tilesize/2), 4, 4);
        	
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
        if(!Main.localMode){
            getNewPlayerCoord();
        }
        if(Main.localMode) {
            if (currentOrigin.getX() != knight.origin.getX() && currentOrigin.getY() != knight.origin.getY()) {
                RenderMap.setMap(dc, knight.origin);
                currentOrigin = knight.origin;
            }
        }
        
        //check if a character has hit an item
        for( Character ch : dc.characters ){
        	float x = (ch.animate.getX()/dc.tilesize);
        	float y = (ch.animate.getY()/dc.tilesize);
        	Vector aniPos = new Vector((int)x, (int)y);
        	
	        Item i = Main.im.getItemAt(aniPos);
	        if( i != null ){
	        	addMessage("Picked up " + i.getMaterial() + " " +i.getType() + " of " + i.getEffect() + ".");
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
        if(!Main.localMode){
            try{
                dos.writeUTF(ks);
                dos.flush();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return ks;
    }

    /**
     * Reads the new player coordinates and walks the player accordingly.
     */
    public void getNewPlayerCoord(){
        if(!Main.localMode) {
            try {
                serverMessage = dis.readUTF();
                if (!serverMessage.equals("")) {
                    knight.walk(Float.parseFloat
                            (serverMessage.split(" ")[0]), Float.parseFloat(serverMessage.split(" ")[1]));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
