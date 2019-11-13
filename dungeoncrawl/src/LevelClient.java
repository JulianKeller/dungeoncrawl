import jig.Entity;
import jig.Vector;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import java.net.*;
import java.io.*;

public class LevelClient extends BasicGameState {
    Socket socket;
    ObjectInputStream dis;
    ObjectOutputStream dos;
    Character knight;
    Vector currentOrigin;

    @Override
    public int getID() {
        return Main.LEVELCLIENT;
    }
    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        Main dc = (Main)game;
        // Server sockets for reading/writing to server.
        this.socket = dc.socket;
        this.dis = dc.dis;
        this.dos = dc.dos;

        dc.width = dc.ScreenWidth/dc.tilesize;
        dc.height = dc.ScreenHeight/dc.tilesize;
        // Grab the map from the Server
        try {
            Integer[][] mapData = (Integer[][])this.dis.readObject();
            // Convert it into an 2d int array
            dc.map = new int[mapData.length][mapData[0].length];
            for(int i = 0; i < mapData.length; i++) {
                for (int j = 0; j < mapData[i].length; j++) {
                    dc.map[i][j] = mapData[i][j];
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        dc.mapTiles = new Entity[dc.map.length][dc.map[0].length];      // initialize the mapTiles
        float wx = (dc.tilesize * 4) - dc.offset;// - dc.xOffset;
        float wy = (dc.tilesize * 4) - dc.tilesize - dc.doubleOffset;// - dc.doubleOffset;// - dc.yOffset;
        knight = new Character(dc,wx,wy,"knight_iron",1);

        currentOrigin = knight.origin;

        RenderMap.setMap(dc, knight.origin);                   // renders the map Tiles
    }
    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dc = (Main)game;
        for (int i = 0; i < dc.map.length; i++) {
            for (int j = 0; j < dc.map[i].length; j++) {
                if (dc.mapTiles[i][j] == null)
                    continue;
                dc.mapTiles[i][j].render(g);
            }
        }
        knight.animate.render(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Main dc = (Main)game;
        Input input = container.getInput();
        getKeystroke(input);
    }

    public void getKeystroke(Input input) {
        try {
            if (input.isKeyDown(Input.KEY_W)) {
                dos.writeUTF("w");
            } else if (input.isKeyDown(Input.KEY_S)) {
                dos.writeUTF("s");
            } else if (input.isKeyDown(Input.KEY_A)) {
                dos.writeUTF("a");
            } else if (input.isKeyDown(Input.KEY_D)) {
                dos.writeUTF("d");
            }
        }catch(IOException e){
            e.printStackTrace();
        }

    }
}
