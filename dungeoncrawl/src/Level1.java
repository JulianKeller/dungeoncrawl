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
    
    private ArrayList<Item> itemsToRender;

    @Override
    public int getID() {
        return Main.LEVEL1;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        Main dc = (Main) game;
        paused = false;

//        dc.map = RenderMap.getDebugMap(dc);
        try {
            dc.map = RenderMap.getRandomMap(dc);        // grab a randomly generated map
        } catch (IOException e) {
            e.printStackTrace();
        }
        dc.mapTiles = new Entity[dc.map.length][dc.map[0].length];      // initialize the mapTiles
        RenderMap.displayMap(dc);                   // renders the map Tiles

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

        float wx = (dc.tilesize * 4) - dc.xOffset;
        float wy = (dc.tilesize * 4) - dc.yOffset;
        knight = new Character(dc, wx, wy, "knight_iron", 1);
    }


    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
    	//plant some items on the level
		Main.im.plant(5);
    	
    	//then restore the visible items from the world
    	//TODO: make the restoration boundary cover only the screen area + a buffer
    	itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dc = (Main) game;
        // render tiles
        for (int i = 0; i < dc.mapTiles.length; i++) {
            for (int j = 0; j < dc.mapTiles[0].length; j++) {
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
