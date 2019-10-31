import jig.Entity;
import jig.Vector;

import java.sql.SQLException;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class Level1 extends BasicGameState {
    private Boolean paused;
    
    private ArrayList<Item> itemsToRender;

    @Override
    public int getID() {
        return Main.LEVEL1;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        Main dc = (Main) game;
        paused = false;

        // 32 x 21 map
        dc.map = new int[][] {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1},
                {1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                };

        // 10x10 map
//        dtc.map = new int[][] {
//                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
//                {1, 1, 0, 1, 1, 1, 1, 0, 0, 1},
//                {1, 1, 0, 1, 0, 0, 0, 0, 0, 1},
//                {1, 1, 0, 1, 1, 1, 1, 0, 1, 1},
//                {1, 1, 0, 1, 1, 1, 1, 0, 0, 1},
//                {1, 1, 0, 1, 1, 1, 1, 0, 1, 1},
//                {1, 1, 1, 1, 0, 0, 0, 0, 0, 1},
//                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
//                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},};

        dc.entities = new Entity[dc.map.length][dc.map[0].length];

//        displayISOmap(dc);
        display2Dmap(dc);
    }

    // draw an isometric map
    public void displayISOmap(Main dc) {
        dc.tileH = 17;
        dc.tileW = 34;
        int x = 0, y = 0;
        for (int i = 0; i < dc.map.length; i++) {
            for (int j = 0; j < dc.map[i].length; j++) {
                if (i == j) {
                    x = dc.ScreenWidth/2 + i * dc.tileW/2;
                    y = dc.tileW * i + j * dc.tileH + dc.tileH;
                }
                else if (i < j) {
                    x = dc.ScreenWidth/2 + dc.tileW/2 * i;
                    y = dc.tileH * j;
                }
                else if (i > j) {
                    x = dc.ScreenWidth/2 - i * dc.tileW/2 - dc.tileW/2;
                    y = dc.tileH * i + j * dc.tileH/2 + dc.tileH;
                }

                if (dc.map[i][j] == 1) {
                    dc.entities[i][j] = new Wall(x, y, "iso");

                }
                else if (dc.map[i][j] == 0) {
                    dc.entities[i][j] = new Floor(x, y, "iso");
                }
            }
        }
    }

    // Draw the 2D map
    public void display2Dmap(Main dc) {
        dc.tileH = 32;
        dc.tileW = 32;
        System.out.printf("rows: %s, cols: %s\n", dc.map.length, dc.map[0].length);
        int x, y;
        for (int i = 0; i < dc.map.length; i++) {
            for (int j = 0; j < dc.map[i].length; j++) {
                x = j * dc.tileH + dc.tileH/2;        // columns
                y = i * dc.tileW + dc.tileW/2;        // rows
                // WALL
                if (dc.map[i][j] == 1) {
                    if (i+1 >= dc.map.length) {
                        dc.entities[i][j] = new Wall(x, y, "top");
                    }
                    else if (dc.map[i+1][j] == 0) {
                        dc.entities[i][j] = new Wall(x, y, "border");
                    }
                    else if (dc.map[i+1][j] == 1) {
                        dc.entities[i][j] = new Wall(x, y, "top");
                    }
                }
                else if (dc.map[i][j] == 0) {
                    if (i+1 < dc.map.length && dc.map[i+1][j] == 1) {
                        dc.entities[i][j] = new Floor(x, y, "shadow");
                    }
                    else if (j-1 > 0 && dc.map[i][j-1] == 1) {
                        dc.entities[i][j] = new Floor(x, y, "shadow_right");
                    }
                    else {
                        dc.entities[i][j] = new Floor(x, y, "normal");
                    }

                }
            }
        }
    }


    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
    	//plant some items on the level
    	try {
			Main.im.plant(5);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//then restore the visible items from the database to render them
    	//TODO: make the restoration boundary cover only the screen area + a buffer
    	try {
			itemsToRender = Main.im.restore(new Vector(0, 0), new Vector(100, 100));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {

    	
    	
    	Main dtc = (Main) game;
//        g.drawImage(ResourceManager.getImage(Game.LEVEL_BACKGROUND), 0, 0);
        for (int i = 0; i < dtc.entities.length; i++) {
            for (int j = 0; j < dtc.entities[0].length; j++) {
                if (dtc.entities[i][j] == null)
                    continue;
                dtc.entities[i][j].render(g);
            }
        }
        
        //render all visible items
        g.setColor(Color.red);
        for( Item i : itemsToRender){
        	System.out.println("Drawing item at "+i.getWorldCoordinates().getX()+", "+i.getWorldCoordinates().getY());
        	//TODO: draw item images
        	//for now, use ovals
        	g.drawOval((i.getWorldCoordinates().getX()*dtc.tileW)+(dtc.tileW/2), (i.getWorldCoordinates().getY()*dtc.tileH)+(dtc.tileH/2), 4, 4);
        }
        


    }



    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Main dtc = (Main) game;
        Cheats.enableCheats(dtc, input);
        pause(input);
        if (paused) {
            return;
        }
    }


    // pause the game
    public void pause(Input input) {
        if (input.isKeyPressed(Input.KEY_SPACE)) {
            paused = !paused;
        }
    }

}
