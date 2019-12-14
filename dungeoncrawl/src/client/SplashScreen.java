package client;

import jig.ResourceManager;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class SplashScreen extends BasicGameState {
    ArrayList<Element> characterTypes;
    ArrayList<SubMenu> menus;
    String selectedPlayer;
    String IP = "127.0.0.1";        // default IP

    int menuOption = 0;
    int characterOption = 0;
    boolean selectCharacter = true;
    boolean connect = false;
    boolean enterAddress = false;
    boolean error = false;
    int errorTimer = 0;
    int centerX = 427;
    int xOffset = 267;
    int deleteTimer = 0;


    @Override
    public int getID() {
        return Main.STARTUPSTATE;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Main dc = (Main) game;
        characterTypes = new ArrayList<>(4);
        initCharacterTypes();
        menus = new ArrayList<>(3);
        initSubMenus();
        dc.map = getBackgroundMap();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dc = (Main) game;

        renderNameAndBackground(dc, g);
        renderCharacters(g);
        renderIP(g);
        renderLaunch(g);
        renderControls(g);
        Color tmp = g.getColor();
        g.setColor(new Color(255, 255, 255, 1f));
        if (selectCharacter) {
            g.drawRect(427, 160, 426, 75);
        } else if (enterAddress) {
            g.drawRect(427, 235, 426, 75);
        }
        g.setColor(tmp);
        if (error) {
            renderError(g);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Main dc = (Main) game;
        
        Input input = container.getInput();

        selectNextSubMenu(input);

        if (selectCharacter) {
            selectCharacter(input);
        }
        if (enterAddress) {
            enterIP(input);
        }
        if (input.isKeyPressed(Input.KEY_ENTER)) {
        	SFXManager.playSound("enter_level");
            connectToSever(dc, game);
        }

        if (errorTimer >= 0) {
            errorTimer -= delta;
        } else {
            error = false;
        }
    }


    private void enterIP(Input input) {
        if (deleteTimer > 0) {
            deleteTimer--;
        }
        if (input.isKeyPressed(Input.KEY_0)) {
            IP += "0";
        } else if (input.isKeyPressed(Input.KEY_1)) {
            IP += "1";
        } else if (input.isKeyPressed(Input.KEY_2)) {
            IP += "2";
        } else if (input.isKeyPressed(Input.KEY_3)) {
            IP += "3";
        } else if (input.isKeyPressed(Input.KEY_4)) {
            IP += "4";
        } else if (input.isKeyPressed(Input.KEY_5)) {
            IP += "5";
        } else if (input.isKeyPressed(Input.KEY_6)) {
            IP += "6";
        } else if (input.isKeyPressed(Input.KEY_7)) {
            IP += "7";
        } else if (input.isKeyPressed(Input.KEY_8)) {
            IP += "8";
        } else if (input.isKeyPressed(Input.KEY_9)) {
            IP += "9";
        } else if (input.isKeyPressed(Input.KEY_PERIOD)) {
            IP += ".";
        } else if (input.isKeyPressed(Input.KEY_SPACE)) {
            IP = "192.168.0.";
        } else if (input.isKeyDown(Input.KEY_DELETE) || input.isKeyDown(Input.KEY_BACK) && deleteTimer <= 0) {
            if (IP.length() > 0) {
                IP = IP.substring(0, IP.length() - 1);
            }
            deleteTimer = 7;
        }
    }


    /*
    Allows player to choose their character, currently selected character is in red
     */
    private void selectCharacter(Input input) throws SlickException {
        if (input.isKeyPressed(Input.KEY_LEFT)) {
        	SFXManager.playSound("click");
            if (characterOption <= 0) {
                characterOption = 0;
                return;
            }
            characterOption--;
        } else if (input.isKeyPressed(Input.KEY_RIGHT)) {
        	SFXManager.playSound("click");
            if (characterOption >= 3) {
                characterOption = 3;
                return;
            }
            characterOption++;
        }
        selectedPlayer = characterTypes.get(characterOption).message;

        Element ch = characterTypes.get(characterOption);
        ch.color = new Color(Color.red);
        for (Element e : characterTypes) {
            if (e.equals(ch)) {
                continue;
            }
            e.color = new Color(Color.white);
        }
    }

    /*
    Selects the sub menu, outlines it in a white box
     */
    private void selectNextSubMenu(Input input) throws SlickException {
        if (input.isKeyPressed(Input.KEY_DOWN)) {
        	SFXManager.playSound("click");
            if (menuOption >= 1) {
                menuOption = 1;
            } else {
                menuOption++;
            }
        } else if (input.isKeyPressed(Input.KEY_UP)) {
        	SFXManager.playSound("click");
            if (menuOption <= 0) {
                menuOption = 0;
            } else {
                menuOption--;
            }
        }

        switch (menuOption) {
            case 0:
                selectCharacter = true;
                enterAddress = false;
                break;
            case 1:
                selectCharacter = false;
                enterAddress = true;
                break;
            default:
                selectCharacter = true;
                enterAddress = false;
                break;
        }
    }


    /*
    render game controls
     */
    private void renderControls(Graphics g) {
        Color tmp = g.getColor();
        int shift = 465;
        int left = 150;
        int indent = 200;
        g.setColor(new Color(0, 0, 0, .5f));
        g.fillRect( left - 20, shift - 20, 385, 260);
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("Move: w, a, s, d", left, shift);
        g.drawString("Pause: p", left, shift + 25);
        g.drawString("Open/Close Inventory: i", left, shift + 50);
        g.drawString("Traverse Inventory: Arrow Keys", indent, shift + 75);
        g.drawString("Move Item to Inventory: enter", indent, shift + 100);
        g.drawString("Display Codex: o", left, shift + 125);
        g.drawString("drop item: backslash", left, shift + 150);
        g.drawString("unequip item: shift", left, shift + 175);
        g.drawString("attack: space/enter", left, shift + 200);
        g.setColor(tmp);
    }

    /*
    let the player know there is an error
     */
    private void renderError(Graphics g) {
        String msg = "Error: Check that your IP Address is correct and that the server is running.";
        Color tmp = g.getColor();
        g.setColor(new Color(255, 0, 0, 1f));
        g.drawString(msg, 320, 400);
        g.setColor(tmp);
    }

    private void renderLaunch(Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("Press ", 200 + xOffset, 325);
        if (connect) {
            g.setColor(new Color(0, 255, 0, 1f));
        }
        g.drawString("<enter>", 255 + xOffset, 325);
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString(" to start the game.", 320 + xOffset, 325);
        g.setColor(tmp);
    }

    /**
     * Shows the names of the characters
     *
     * @param g
     */
    private void renderIP(Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("Enter Server IP Address:", 200 + xOffset, 250);
//        g.setColor(new Color(50, 131, 223, 1f));
        if (enterAddress) {
            g.setColor(Color.magenta);
        }
        g.drawString(IP, 200 + xOffset, 275);
        g.setColor(tmp);
    }

    /**
     * Shows the names of the characters
     *
     * @param g
     */
    private void renderCharacters(Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("Select Character:", 200 + xOffset, 175);
        for (Element e : characterTypes) {
            g.setColor(e.color);
            g.drawString(e.message, e.x, e.y);
        }
        g.setColor(tmp);
    }

    private void initCharacterTypes() {
        Color color = new Color(255, 255, 255, 1f);
        characterTypes.add(new Element("Knight", color, 200 + xOffset, 200));
        characterTypes.add(new Element("Tank", color, 300 + xOffset, 200));
        characterTypes.add(new Element("Archer", color, 400 + xOffset, 200));
        characterTypes.add(new Element("Mage", color, 500 + xOffset, 200));
    }

    private void initSubMenus() {
        menus.add(new SubMenu(true, "selectCharacters"));
        menus.add(new SubMenu(false, "connectToServer"));
    }

    /*
    Renders the title of the game
     */
    private void renderNameAndBackground(Main dc, Graphics g) {
        Color tmp = g.getColor();
//        g.scale(.5f, .5f);
        g.drawImage(ResourceManager.getImage(Main.MAP_IMG), 0, 0);
        g.setColor(new Color(0, 0, 0, .5f));
//        g.scale(2, 2);
        g.fillRect(0, 0, dc.ScreenWidth, dc.ScreenHeight);
        g.setColor(tmp);
        g.drawImage(ResourceManager.getImage(Main.TITLE), 0, 0);
    }


    public static int[][] getBackgroundMap() {
        return new int[][] {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
    }



    /**
     * Connects the client to the server
     *

     */
    private void connectToSever(Main dc,StateBasedGame game) {
        // Setting up the connection to the server
        Socket socket = null;
        ObjectOutputStream dos = null;
        ObjectInputStream dis = null;
        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName(IP);

            // establish the connection with server port 5000
            socket = new Socket(ip, 5000);

            // obtaining input and out streams
            dos = new ObjectOutputStream(socket.getOutputStream());
            dis = new ObjectInputStream(socket.getInputStream());


        } catch (Exception e) {
            error = true;
            errorTimer = 6000;
            return;
        }
        dc.socket = socket;
        dc.dis = dis;
        dc.dos = dos;

        ((Level)game.getState(Main.LEVEL1)).setType(selectedPlayer);
        dc.enterState(Main.LEVEL1);
    }


// elements in menus
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

// menues
private class SubMenu {
    boolean selected;
    String name;

    public SubMenu(boolean selected, String name) {
        this.name = name;
        this.selected = selected;
    }
}

}
