package client;

import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class SplashScreen extends BasicGameState {
    ArrayList<Element> characterTypes;
    String selectedPlayer;
    
    int option = 0;
    boolean selectCharacter = true;
    boolean connect = false;
    
    @Override
    public int getID() {
        return Main.STARTUPSTATE;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Main dc = (Main) game;
        characterTypes = new ArrayList<>(4);
        initCharacterTypes();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        Main dc = (Main) game;
        System.out.println("Welcome to the Splashscreen");

    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dc = (Main) game;
        renderName(dc, g);
        renderCharacters(dc, g);
        if (selectCharacter) {
            g.drawRect(160, 160, 500, 75);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Main dc = (Main) game;
        Input input = container.getInput();

        if (selectCharacter) {
            selectCharacter(input);
        }
        if (connect) { 
            if (input.isKeyPressed(Input.KEY_ENTER)) {
                connectToSever(dc);
            }
        }
    }
    
    private void selectCharacter(Input input) {
        if (input.isKeyPressed(Input.KEY_LEFT)) {
            if (option <= 0) {
                option = 0;
                return;
            }
            option--;
        }
        else if (input.isKeyPressed(Input.KEY_RIGHT)) {
            if (option >= 3) {
                option = 3;
                return;
            }
            option++;
        }
        else if (input.isKeyPressed(Input.KEY_ENTER)) {
            selectedPlayer = characterTypes.get(option).message;
        }

        Element ch = characterTypes.get(option);
        ch.color = new Color(Color.red);
        for (Element e : characterTypes) {
            if (e.equals(ch)) {
                continue;
            }
            e.color = new Color(Color.white);
        }

    }

    private void selectNextSubMenu(Input input) {
        if (input.isKeyPressed(Input.KEY_UP)) {

        }
        else if (input.isKeyPressed(Input.KEY_DOWN)) {

        }
    }

    /**
     * Shows the names of the characters
     * @param dc
     * @param g
     */
    private void renderCharacters(Main dc, Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("Select Character:", 200, 175);
        for (Element e : characterTypes) {
            g.setColor(e.color);
            g.drawString(e.message, e.x, e.y);
        }
        g.setColor(tmp);
    }

    private void initCharacterTypes() {
        Color color = new Color(255, 255, 255, 1f);
        characterTypes.add(new Element("Knight", color, 200, 200));
        characterTypes.add(new Element("Tank", color, 300, 200));
        characterTypes.add(new Element("Archer", color, 400, 200));
        characterTypes.add(new Element("Mage", color, 500, 200));
    }
    
    /*
    Renders the title of the game
     */
    private void renderName(Main dc, Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(0, 0, 0, .3f));
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("Dungeon Crawl", (float) dc.ScreenWidth/2 - 12, 50);
        g.setColor(tmp);
    }


    /**
     * Connects the client to the server
     * @param dc
     */
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
        
        
        private class Element {
            String message;
            Color color;
            float x;
            float y;
            
            public Element(String message, Color color, float x, float y) {
                this.message = message;
                this.color = color;
                this.x = x;
                this.y = y;
            }
        }
    
}
