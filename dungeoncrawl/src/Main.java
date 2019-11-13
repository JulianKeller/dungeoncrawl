import jig.Entity;
import jig.ResourceManager;

import java.io.*;
import java.net.*;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import java.util.ArrayList;


public class Main extends StateBasedGame {
    // Server items
    public final Socket socket;
    public final ObjectInputStream dis;
    public final ObjectOutputStream dos;
    public static boolean localMode = true;

    // Game States
    public static final int STARTUPSTATE = 0;
    public static final int LEVEL1 = 1;
    public static final int LEVEL2 = 2;
    public static final int GAMEOVER = 3;
    public static final int GAMEWON = 4;

    // Image Resources
    // levels
    public static final String STARTUP_BANNER = "resources/startup/startup_screen.png";


    // walls
    public static final String WALL = "resources/wall/wall_2d.png";
    public static final String WALL_TOP = "resources/wall/wall_2dtop.png";

    // floors
    public static final String FLOOR = "resources/floor/floor_2d.png";
    public static final String SHADOW_FLOOR = "resources/floor/floor_2d_shadow.png";
    public static final String SHADOW_FLOOR_R = "resources/floor/floor_2d_shadow_right.png";
    public static final String SHADOW_FLOOR_CORNER = "resources/floor/floor_2d_shadow_corner.png";
    public static final String SHADOW_FLOOR_DOUBLE_CORNER = "resources/floor/floor_2d_dual_shadows.png";

    // iso
    public static final String ISOFLOOR = "resources/floor/floor_grey.png";
    public static final String ISOWALL = "resources/wall/wall_grey.png";

    // path finding arrows
    public static final String ARROW_U = "resources/arrows/up.png";
    public static final String ARROW_D = "resources/arrows/down.png";
    public static final String ARROW_L = "resources/arrows/left.png";
    public static final String ARROW_R = "resources/arrows/right.png";

    // potions
    public static final String POTION_BLUE = "resources/potions/blue_potion.png";
    public static final String POTION_RED = "resources/potions/red_potion.png";
    public static final String POTION_YELLOW = "resources/potions/yellow_potion.png";
    public static final String POTION_PINK = "resources/potions/pink_potion.png";
    public static final String POTION_ORANGE = "resources/potions/orange_potion.png";

    // Knight
    public static final String KNIGHT_LEATHER = "resources/knight/knight_leather.png";
    public static final String KNIGHT_IRON = "resources/knight/knight_iron.png";
    public static final String KNIGHT_GOLD = "resources/knight/knight_gold.png";

    // Mage
    public static final String MAGE_LEATHER = "resources/mage/mage_leather.png";
    public static final String MAGE_IMPROVED = "resources/mage/mage_improved.png";


    // Archer
    public static final String ARCHER_LEATHER = "resources/archer/archer_leather.png";

    // Tank
    public static final String TANK_LEATHER = "resources/tank/tank_leather.png";
    public static final String TANK_IRON = "resources/tank/tank_iron.png";
    public static final String TANK_GOLD = "resources/tank/tank_gold.png";

    // skeleton
    public static final String SKELETON_BASIC = "resources/skeleton/skeleton_basic.png";
    public static final String SKELETON_LEATHER = "resources/skeleton/skeleton_leather.png";
    public static final String SKELETON_CHAIN = "resources/skeleton/skeleton_chainmail.png";

    // dark elf
    public static final String ICE_ELF = "resources/darkelf/iceelf.png";

    // Screen Size
    public final int ScreenWidth;
    public final int ScreenHeight;

    // declare entities
    int offset;
    int tilesize;
    int doubleOffset;
    int yOffset;
    int xOffset;
    int width;  // num tiles in width
    int height; // num tiles in height
    int[][] map;
    Entity[][] mapTiles;
    boolean collisions;
    
    //item types
    public static final String[] ItemTypes = {"Potion", "Armor", "Sword", "Arrow", "Staff", "Glove"};
    
    //item materials
    public static final String[] ArmorMaterials = {"Leather", "Iron", "Turtle Shell"};
    public static final String[] SwordMaterials = {"Wooden", "Iron", "Gold"};
    public static final String[] StaffMaterials = {"Ruby", "Emerald", "Amethyst"};
    public static final String[] GloveMaterials = {"Leather", "Iron", "Gold"};
    
    //item effects
    public static final String[] PotionEffects = {"Healing", "Strength", "Flame", "Mana", "Invisibility"};
    public static final String[] ArrowEffects = {"Flaming", "Poisoned", "Ice"};
    public static final String[] StaffEffects = {"Healing", "Lightning", "Flame", "Ice"};
    public static final String[] ArmorEffects = {"Stench", "Iron Skin", "Thorns", "Swiftness"};
    public static final String[] SwordEffects = {"Fright", "Might", "Flame", "Ice"};
    public static final String[] GloveEffects = {"Swiftness", "Regeneration", "Reflection"};
    
    //displayed item name should be of the form "material type of effect" using whatever fields are filled in
    
    
    // create an item manager
    public static ItemManager im;
    Entity[][] potions;
    ArrayList<AnimateEntity> animations;
    
    public ArrayList<Character> characters;


    /**
     * Create a new state based game
     *
     * @param title The name of the game
     */
    public Main(String title, int width, int height, Socket socket, ObjectInputStream dis, ObjectOutputStream dos) {
        super(title);
        ScreenWidth = width;
        ScreenHeight = height;

        tilesize = 32;
        offset = tilesize/2;
        doubleOffset = offset/2;
        xOffset = tilesize - doubleOffset;
        yOffset = tilesize + doubleOffset/2;
        collisions = true;
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;

        characters = new ArrayList<Character>();

        Entity.setCoarseGrainedCollisionBoundary(Entity.AABB);    // set a rectangle
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        addState(new StartUpState());
        addState(new Level1());
        addState(new GameOver());

        // load images
        // startup
        ResourceManager.loadImage(STARTUP_BANNER);

        // arrows
        ResourceManager.loadImage(ARROW_U);
        ResourceManager.loadImage(ARROW_D);
        ResourceManager.loadImage(ARROW_L);
        ResourceManager.loadImage(ARROW_R);

        // walls and floors
        ResourceManager.loadImage(WALL);
        ResourceManager.loadImage(WALL_TOP);
        ResourceManager.loadImage(FLOOR);
        ResourceManager.loadImage(SHADOW_FLOOR);
        ResourceManager.loadImage(SHADOW_FLOOR_R);
        ResourceManager.loadImage(SHADOW_FLOOR_CORNER);
        ResourceManager.loadImage(SHADOW_FLOOR_DOUBLE_CORNER);

        // ISO
        ResourceManager.loadImage(ISOFLOOR);
        ResourceManager.loadImage(ISOWALL);

        // POTIONS
        ResourceManager.loadImage(POTION_BLUE);
        ResourceManager.loadImage(POTION_ORANGE);
        ResourceManager.loadImage(POTION_PINK);
        ResourceManager.loadImage(POTION_RED);
        ResourceManager.loadImage(POTION_YELLOW);

        // KNIGHT
        ResourceManager.loadImage(KNIGHT_LEATHER);
        ResourceManager.loadImage(KNIGHT_IRON);
        ResourceManager.loadImage(KNIGHT_GOLD);

        // MAGE
        ResourceManager.loadImage(MAGE_LEATHER);
        ResourceManager.loadImage(MAGE_IMPROVED);

        // ARCHER
        ResourceManager.loadImage(ARCHER_LEATHER);

        // TANK
        ResourceManager.loadImage(TANK_LEATHER);
        ResourceManager.loadImage(TANK_IRON);
        ResourceManager.loadImage(TANK_GOLD);

        // SKELETON
        ResourceManager.loadImage(SKELETON_BASIC);
        ResourceManager.loadImage(SKELETON_CHAIN);
        ResourceManager.loadImage(SKELETON_LEATHER);

        // ELF
        ResourceManager.loadImage(ICE_ELF);
    }

    // Send close to the server and close connections before exiting.
    @Override
    public boolean closeRequested(){
        if(!localMode) {
            try {
                dos.writeUTF("Exit");
                dos.flush();
                socket.close();
                dos.close();
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
        return false;
    }

    public static void main(String[] args) {
        // Setting up the connection to the server
        Socket socket = null;
        ObjectInputStream dis = null;
        ObjectOutputStream dos = null;
        if(!localMode) {
            try {
                byte[] ipAddr = new byte[]{127, 0, 0, 1};

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
        }
    	Main game = new Main("Dungeon Crawl", 1280, 768, socket, dis, dos);
    	im = new ItemManager(game);
        AppGameContainer app;
        try {
            app = new AppGameContainer(game);
            app.setDisplayMode(1280, 768, false);
            app.setVSync(true);
//            app.setShowFPS(false);      // disable fps
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
