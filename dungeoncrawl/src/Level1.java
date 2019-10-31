import jig.Entity;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.util.ArrayList;

public class Level1 extends BasicGameState {
    private Boolean paused;
    ArrayList<AnimateEntity> animated;

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
        display2Dmap(dc);

        // draw potions
        dc.potions = new Entity[dc.map.length][dc.map[0].length];   // TODO make arraylist
        dc.potions[10][10] = new Potion(10 * dc.tileH + dc.tileH/2, 10 * dc.tileW + dc.tileW/2, "health");
        dc.potions[10][4] = new Potion(10 * dc.tileH + dc.tileH/2, 4 * dc.tileW + dc.tileW/2, "manna");
        dc.potions[10][8] = new Potion(10 * dc.tileH + dc.tileH/2, 8 * dc.tileW + dc.tileW/2, "fire");
        dc.potions[10][2] = new Potion(10 * dc.tileH + dc.tileH/2, 2 * dc.tileW + dc.tileW/2, "strength");
        dc.potions[10][6] = new Potion(10 * dc.tileH + dc.tileH/2, 6 * dc.tileW + dc.tileW/2, "invisibility");

        animated = new ArrayList<>(20);
        String[] variations = new String[] {"spell_up", "spell_left", "spell_down", "spell_right"};
        for (int i = 0; i < variations.length; i++) {
            animated.add(new AnimateEntity((i + 3) * dc.tileH + dc.tileH/2, 4 * dc.tileW, variations[i], 100));
        }

    }

    // Draw the 2D map
    public void display2Dmap(Main dc) {
//        System.out.printf("rows: %s, cols: %s\n", dc.map.length, dc.map[0].length);
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
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dc = (Main) game;
//        g.drawImage(ResourceManager.getImage(Game.LEVEL_BACKGROUND), 0, 0);
        // render tiles
        for (int i = 0; i < dc.entities.length; i++) {
            for (int j = 0; j < dc.entities[0].length; j++) {
                if (dc.entities[i][j] == null)
                    continue;
                dc.entities[i][j].render(g);
            }
        }

        // render potions
        for (int i = 0; i < dc.potions.length; i++) {
            for (int j = 0; j < dc.potions[0].length; j++) {
                if (dc.potions[i][j] == null)
                    continue;
                dc.potions[i][j].render(g);
            }
        }

        // display the animated entities
        for (AnimateEntity a : animated) {
            a.render(g);
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
    }


    // pause the game
    public void pause(Input input) {
        if (input.isKeyPressed(Input.KEY_SPACE)) {
            paused = !paused;
        }
    }

}
