package client;

import org.newdawn.slick.Input;

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
//                else if (input.isKeyPressed(Input.KEY_4)) {
//            dtc.enterState(Game.GAMEOVER);
//        }
//        else if (input.isKeyPressed(Input.KEY_5)) {
//            dtc.enterState(Game.GAMEWON);
//        }
//        if (input.isKeyPressed(Input.KEY_6)) {
//            dtc.showPath = !dtc.showPath;
//        }
//        else if (input.isKeyPressed(Input.KEY_7)) {
//            dtc.infiniteLives = !dtc.infiniteLives;
//        }
        else if (input.isKeyPressed(Input.KEY_C)) {
            dc.collisions = !dc.collisions;
        }
    }
}
