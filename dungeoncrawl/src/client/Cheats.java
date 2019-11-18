package client;

import org.newdawn.slick.Input;


/*
Keys in Use:
1: level 1
3: startup state
4: increase speed
5: decrease speed
c: no collisions with walls
 */

public class Cheats {

    public static void enableCheats(Main dc, Input input) {
        // show the path of dijkstra
        if (input.isKeyPressed(Input.KEY_1)) {
            dc.enterState(Main.LEVEL1);
        }
//        else if (input.isKeyPressed(Input.KEY_2)) {
//            dtc.enterState(Game.LEVEL2);
//        }
        else if (input.isKeyPressed(Input.KEY_3)) {
            dc.enterState(Main.STARTUPSTATE);
        }
        else if (input.isKeyPressed(Input.KEY_C)) {     // disable collisions with walls
            dc.collisions = !dc.collisions;
        }
    }
}
