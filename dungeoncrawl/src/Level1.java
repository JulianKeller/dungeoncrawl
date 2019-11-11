import jig.Entity;
import jig.Vector;

import java.io.IOException;
import java.util.ArrayList;

import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class Level1 extends BasicGameState {
    private Boolean paused;
    Character knight;
    Vector currentOrigin;
    
    private ArrayList<Item> itemsToRender;
    
    //array of messages to print to the screen
    private String[] messagebox;
    private int messages = 2; //number of messages printed at one time

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
        
        messagebox = new String[messages]; //display four messages at a time


//        dc.map = RenderMap.getDebugMap(dc);
        try {
            dc.map = RenderMap.getRandomMap(dc);        // grab a randomly generated map
        } catch (IOException e) {
            e.printStackTrace();
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
        
        dc.characters.add(knight);
        
        currentOrigin = knight.origin;
        RenderMap.setMap(dc, knight.origin);                   // renders the map Tiles
        
        itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));
    }


    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
    	messagebox = new String[messages]; //display four messages at a time
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
    	messagebox[0] = message;
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
        for( int i = 0; i < messagebox.length; i++ ){
        	if( messagebox[i] == null || messagebox[i] == "" ){
        		break;
        	}
        	g.drawString(messagebox[i], 30, dc.ScreenHeight-(20 * (messagebox.length - i)));
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
	        	addMessage("Picked up " + i.getMaterial() + " " +i.getType() + " of " + i.getEffect() + ".");
	        	//give removes item from the world's inventory
	        	//  and adds it to the player's inventory
	        	Main.im.give(i.getID(), ch.getPid());
	        	
	        	//stop rendering the item
	        	itemsToRender.remove(i);
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
