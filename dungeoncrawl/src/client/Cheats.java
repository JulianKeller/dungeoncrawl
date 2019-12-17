package client;

import org.newdawn.slick.Input;


/*
Keys in Use:
1: level 1
3: startup state
4: increase speed
5: decrease speed
c: no collisions with walls
space: pause game
 */

public class Cheats {

    public static void enableCheats(Main dc, Input input) {
        // show the path of dijkstra
        if (input.isKeyPressed(Input.KEY_0)) {
            dc.enterState(Main.STARTUPSTATE);
        }
        if (input.isKeyPressed(Input.KEY_1)) {
            dc.enterState(Main.LEVEL1);
        }
        else if (input.isKeyPressed(Input.KEY_2)) {
//            dc.enterState(Main.STARTUPSTATE);     // TODO
        }
        else if (input.isKeyPressed(Input.KEY_3)) {
            dc.enterState(Main.STARTUPSTATE);
        }
        // 4 and 5 are used to increase speed
//        else if (input.isKeyPressed(Input.KEY_4)) {
//              RESERVED
//        }
//        else if (input.isKeyPressed(Input.KEY_5)) {
//              RESERVED
//        }
        // pathfinding
        else if (input.isKeyPressed(Input.KEY_6)) {
            dc.showPath = !dc.showPath;
        }
        else if (input.isKeyPressed(Input.KEY_7)) {
            dc.invincible = !dc.invincible;
        }
        else if (input.isKeyPressed(Input.KEY_8)) {
            for (Character ai : dc.enemies) {
                ai.setHitPoints(0);
                ai.updateAnimation("die");
            }
        }
        else if (input.isKeyPressed(Input.KEY_C)) {     // disable collisions with walls
            dc.collisions = !dc.collisions;
        }
    }
}
