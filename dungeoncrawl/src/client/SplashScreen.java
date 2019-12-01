package client;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class SplashScreen extends BasicGameState {
    @Override
    public int getID() {
        return Main.STARTUPSTATE;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {

    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Main dc = (Main) game;
        connectToSever(dc);
    }

    private void connectToSever(Main dc) {
        // Setting up the connection to the server
        Socket socket = null;
        ObjectInputStream dis = null;
        ObjectOutputStream dos = null;
            try {
                byte[] ipAddr = new byte[]{127,0,0,1};

                // getting localhost ip
                InetAddress ip = InetAddress.getByAddress(ipAddr);

                // establish the connection with server port 5000
                socket = new Socket(ip, 5000);

                // obtaining input and out streams
                dis = new ObjectInputStream(socket.getInputStream());
                dos = new ObjectOutputStream(socket.getOutputStream());

            } catch (Exception e) {
                e.printStackTrace();
            }
        dc.socket = socket;
        dc.dis = dis;
        dc.dos = dos;
        dc.enterState(Main.LEVEL1);
        }
    
}
