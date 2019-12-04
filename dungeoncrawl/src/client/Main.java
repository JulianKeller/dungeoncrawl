package client;

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
    // server.Server items
    public Socket socket = null;
    public ObjectInputStream dis = null;
    public ObjectOutputStream dos = null;
    public static boolean localMode = false;

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

    // path finding arrows
    public static final String ARROW_U = "resources/arrows/up.png";
    public static final String ARROW_D = "resources/arrows/down.png";
    public static final String ARROW_L = "resources/arrows/left.png";
    public static final String ARROW_R = "resources/arrows/right.png";

    // Knight
    public static final String KNIGHT_LEATHER = "resources/knight/knight_leather.png";
    public static final String KNIGHT_IRON = "resources/knight/knight_iron.png";
    public static final String KNIGHT_GOLD = "resources/knight/knight_gold.png";

    // Mage
    public static final String MAGE_LEATHER = "resources/mage/mage_leather.png";
    public static final String MAGE_BLUE = "resources/mage/mage_blue.png";
    public static final String MAGE_PURPLE = "resources/mage/mage_purple.png";

    // Archer
    public static final String ARCHER_LEATHER = "resources/archer/archer_leather.png";
    public static final String ARCHER_IRON = "resources/archer/archer_iron.png";
    public static final String ARCHER_GREEN = "resources/archer/archer_green.png";

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


    /* Items */

    // potions
    public static final String POTION_BLUE = "resources/potions/blue_potion.png";
    public static final String POTION_RED = "resources/potions/red_potion.png";
    public static final String POTION_YELLOW = "resources/potions/yellow_potion.png";
    public static final String POTION_PINK = "resources/potions/pink_potion.png";
    public static final String POTION_ORANGE = "resources/potions/orange_potion.png";

    // Armor
    public static final String ARMOR_GOLD = "resources/armor/gold_armor.png";
    public static final String ARMOR_IRON = "resources/armor/iron_armor.png";

    // robes
    public static final String ROBES_BLUE = "resources/robes/robes-blue.png";
    public static final String ROBES_PURPLE = "resources/robes/robes-purple.png";

    // archer clothes
    public static final String ARCHER_CLOTHES_GREEN = "resources/archer_clothes/archer-green.png";
    public static final String ARCHER_CLOTHES_IRON = "resources/archer_clothes/archer-iron.png";

    // Swords
    public static final String SWORD_WOOD = "resources/swords/wood_sword.png";
    public static final String SWORD_IRON = "resources/swords/iron_sword.png";
    public static final String SWORD_GOLD = "resources/swords/gold_sword.png";

    // Arrow Item
    public static final String ARROW_NORMAL = "resources/arrows/normal_arrows.png";
    public static final String ARROW_ICE = "resources/arrows/ice_arrows.png";
    public static final String ARROW_POISON = "resources/arrows/poison_arrows.png";
    public static final String ARROW_FLAME = "resources/arrows/flaming_arrows.png";
    
    // Fire Arrows
    public static final String ARROW_FLAME_UP = "resources/arrows/up/up_arrow_fire.png";
    public static final String ARROW_FLAME_DOWN = "resources/arrows/down/down_arrow_fire.png";
    public static final String ARROW_FLAME_LEFT = "resources/arrows/left/left_arrow_fire.png";
    public static final String ARROW_FLAME_RIGHT = "resources/arrows/right/right_arrow_fire.png";
    
    // Ice Arrows
    public static final String ARROW_ICE_UP = "resources/arrows/up/up_arrow_ice.png";
    public static final String ARROW_ICE_DOWN = "resources/arrows/down/down_arrow_ice.png";
    public static final String ARROW_ICE_LEFT = "resources/arrows/left/left_arrow_ice.png";
    public static final String ARROW_ICE_RIGHT = "resources/arrows/right/right_arrow_ice.png";
    
    // Normal Arrows
    public static final String ARROW_NORMAL_UP = "resources/arrows/up/up_arrow_normal.png";
    public static final String ARROW_NORMAL_DOWN = "resources/arrows/down/down_arrow_normal.png";
    public static final String ARROW_NORMAL_LEFT = "resources/arrows/left/left_arrow_normal.png";
    public static final String ARROW_NORMAL_RIGHT = "resources/arrows/right/right_arrow_normal.png";
    
    // Poison Arrows
    public static final String ARROW_POISON_UP = "resources/arrows/up/up_arrow_poison.png";
    public static final String ARROW_POISON_DOWN = "resources/arrows/down/down_arrow_poison.png";
    public static final String ARROW_POISON_LEFT = "resources/arrows/left/left_arrow_poison.png";
    public static final String ARROW_POISON_RIGHT = "resources/arrows/right/right_arrow_poison.png";

    // staffs
    public static final String STAFF_RUBY = "resources/staffs/staff-red.png";
    public static final String STAFF_EMERALD = "resources/staffs/staff-green.png";
    public static final String STAFF_AMETHYST = "resources/staffs/staff-purple.png";

    // spells
    public static final String SPELL_RED = "resources/spells/spell-red.png";
    public static final String SPELL_GREEN = "resources/spells/spell-green.png";
    public static final String SPELL_PURPLE = "resources/spells/spell-purple.png";

    // gloves
    public static final String GLOVES_RED = "resources/gloves/gloves-red.png";
    public static final String GLOVES_YELLOW = "resources/gloves/gloves-yellow.png";
    public static final String GLOVES_WHITE = "resources/gloves/gloves-white.png";

    // splash screen
    public static final String TITLE = "resources/splashScreen/title.png";
    public static final String MAP_IMG = "resources/splashScreen/map_image.png";

    // Screen Size
    public final int ScreenWidth;
    public final int ScreenHeight;

    // declare entities
    int offset;
    int tilesize;
    int doubleOffset;
    int yOffset;
    int xOffset;
    int tilesWide;  // num tiles in width
    int tilesHigh; // num tiles in height
    int mapWidth;
    int mapHeight;
    int[][] map;
    boolean showPath = false;   // shows dijkstra or not
    ArrayList<BaseMap> maptiles;
    boolean collisions;
    boolean invincible;
    ArrayList<DisplayItem> testItems;
    Character hero;
    public ArrayList<Character> characters;
    ArrayList<Character> enemies;

    // create an item manager
    public static ItemManager im;
    Entity[][] potions;
    ArrayList<AnimateEntity> animations;

    //item types
    public static final String[] ItemTypes = {"Potion", "Armor", "Sword", "Arrow", "Staff", "Glove"};
    
    //item effects
    public static final String[] PotionEffects = {"Healing", "Strength", "Flame", "Mana", "Invisibility"};
    public static final String[] ArrowEffects = {"Flame", "Poison", "Ice", ""};
    public static final String[] StaffEffects = {"Healing", "Lightning", "Flame", "Ice"};
    public static final String[] ArmorEffects = {"Stench", "Iron Skin", "Thorns", "Swiftness"};
    public static final String[] SwordEffects = {"Fright", "Might", "Flame", "Ice"};
    public static final String[] GloveEffects = {"Swiftness", "Regeneration", "Reflection"};
    
    //displayed item name should be of the form "material type of effect" using whatever fields are filled in


    /**
     * Create a new state based game
     *
     * @param title The name of the game
     */
    public Main(String title, int width, int height) {
        super(title);
        ScreenWidth = width;
        ScreenHeight = height;
        mapWidth = 0;
        mapHeight = 0;
        tilesize = 32;
        tilesHigh = ScreenHeight/tilesize;
        tilesWide = ScreenWidth/tilesize;

        offset = tilesize/2;
        doubleOffset = offset/2;
        xOffset = tilesize - doubleOffset;
        yOffset = tilesize + doubleOffset/2;
        collisions = true;
        invincible = false;

        characters = new ArrayList<>();
        enemies = new ArrayList<>();
        testItems = new ArrayList<>(50);

        Entity.setCoarseGrainedCollisionBoundary(Entity.AABB);    // set a rectangle
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        addState(new SplashScreen());
        addState(new Level());
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

        // KNIGHT
        ResourceManager.loadImage(KNIGHT_LEATHER);
        ResourceManager.loadImage(KNIGHT_IRON);
        ResourceManager.loadImage(KNIGHT_GOLD);

        // MAGE
        ResourceManager.loadImage(MAGE_LEATHER);
        ResourceManager.loadImage(MAGE_BLUE);
        ResourceManager.loadImage(MAGE_PURPLE);

        // ARCHER
        ResourceManager.loadImage(ARCHER_LEATHER);
        ResourceManager.loadImage(ARCHER_IRON);
        ResourceManager.loadImage(ARCHER_GREEN);

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

        // ---- ITEMS ----
        // POTIONS
        ResourceManager.loadImage(POTION_BLUE);
        ResourceManager.loadImage(POTION_ORANGE);
        ResourceManager.loadImage(POTION_PINK);
        ResourceManager.loadImage(POTION_RED);
        ResourceManager.loadImage(POTION_YELLOW);

        // ARMOR
        ResourceManager.loadImage(ARMOR_GOLD);
        ResourceManager.loadImage(ARMOR_IRON);

        // ARCHER CLOTHES
        ResourceManager.loadImage(ARCHER_CLOTHES_GREEN);
        ResourceManager.loadImage(ARCHER_CLOTHES_IRON);

        // ROBES
        ResourceManager.loadImage(ROBES_BLUE);
        ResourceManager.loadImage(ROBES_PURPLE);

        // SWORDS
        ResourceManager.loadImage(SWORD_IRON);
        ResourceManager.loadImage(SWORD_WOOD);
        ResourceManager.loadImage(SWORD_GOLD);

        // ARROWS
        ResourceManager.loadImage(ARROW_NORMAL);
        ResourceManager.loadImage(ARROW_FLAME);
        ResourceManager.loadImage(ARROW_ICE);
        ResourceManager.loadImage(ARROW_POISON);
        
        // FLAME ARROWS
        ResourceManager.loadImage(ARROW_FLAME_UP);
        ResourceManager.loadImage(ARROW_FLAME_DOWN);
        ResourceManager.loadImage(ARROW_FLAME_LEFT);
        ResourceManager.loadImage(ARROW_FLAME_RIGHT);
        
        // ICE ARROWS
        ResourceManager.loadImage(ARROW_ICE_UP);
        ResourceManager.loadImage(ARROW_ICE_DOWN);
        ResourceManager.loadImage(ARROW_ICE_LEFT);
        ResourceManager.loadImage(ARROW_ICE_RIGHT);
        
        // NORMAL ARROWS
        ResourceManager.loadImage(ARROW_NORMAL_UP);
        ResourceManager.loadImage(ARROW_NORMAL_DOWN);
        ResourceManager.loadImage(ARROW_NORMAL_LEFT);
        ResourceManager.loadImage(ARROW_NORMAL_RIGHT);
        
        // POISON ARROWS
        ResourceManager.loadImage(ARROW_POISON_UP);
        ResourceManager.loadImage(ARROW_POISON_DOWN);
        ResourceManager.loadImage(ARROW_POISON_LEFT);
        ResourceManager.loadImage(ARROW_POISON_RIGHT);

        //STAFFS
        ResourceManager.loadImage(STAFF_RUBY);
        ResourceManager.loadImage(STAFF_AMETHYST);
        ResourceManager.loadImage(STAFF_EMERALD);

        // SPELLS
        ResourceManager.loadImage(SPELL_GREEN);
        ResourceManager.loadImage(SPELL_RED);
        ResourceManager.loadImage(SPELL_PURPLE);

        // GLOVES
        ResourceManager.loadImage(GLOVES_RED);
        ResourceManager.loadImage(GLOVES_WHITE);
        ResourceManager.loadImage(GLOVES_YELLOW);

        // TITLE
        ResourceManager.loadImage(TITLE);
        ResourceManager.loadImage(MAP_IMG);


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
    	Main game = new Main("Dungeon Crawl", 1280, 736);
    	im = new ItemManager(game);
        AppGameContainer app;
        try {
            app = new AppGameContainer(game);
            app.setDisplayMode(1280, 736, false);
            app.setVSync(true);
            app.setShowFPS(true);      // disable fps
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
