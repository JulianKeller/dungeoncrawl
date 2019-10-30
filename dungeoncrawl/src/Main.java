import jig.Entity;
import jig.ResourceManager;

import java.sql.SQLException;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;


public class Main extends StateBasedGame {
    // Game States
    public static final int STARTUPSTATE = 0;
    public static final int LEVEL1 = 1;
    public static final int LEVEL2 = 2;
    public static final int GAMEOVER = 3;
    public static final int GAMEWON = 4;

    // Image Resources
    // levels
    public static final String STARTUP_BANNER = "resources/startup/startup_screen.png";


    public static final String WALL = "resources/wall/wall_2d.png";
    public static final String WALL_TOP = "resources/floor/wall_2dtop.png";

    public static final String FLOOR = "resources/floor/floor_2d.png";
    public static final String SHADOW_FLOOR = "resources/floor/floor_2d_shadow.png";
    public static final String SHADOW_FLOOR_R = "resources/floor/floor_2d_shadow_right.png";

    // iso
    public static final String ISOFLOOR = "resources/floor/floor_grey.png";
    public static final String ISOWALL = "resources/wall/wall_grey.png";

    // path finding arrows
    public static final String ARROW_U = "resources/arrows/up.png";
    public static final String ARROW_D = "resources/arrows/down.png";
    public static final String ARROW_L = "resources/arrows/left.png";
    public static final String ARROW_R = "resources/arrows/right.png";

    // Screen Size
    public final int ScreenWidth;
    public final int ScreenHeight;

    // declare entities
    int tileW;
    int tileH;
    int[][] map;
    Entity[][] entities;
    
    //item types
    public static final String[] ItemTypes = {"Potion", "Armor", "Sword", "Arrow", "Staff", "Glove"};
    
    //item materials
    public static final String[] ArmorMaterials = {"Leather", "Iron", "Turtle Shell"};
    public static final String[] SwordMaterials = {"Wooden", "Iron", "Gold"};
    public static final String[] StaffMaterials = {"Ruby", "Emerald", "Amethyst"};
    public static final String[] GloveMaterials = {"Leather", "Iron", "Gold"};
    
    //item effects
    public static final String[] PotionEffects = {"Healing", "Strength", "Flame", "Mana", "Invisibility"};
    public static final String[] ArrowMaterials = {"Flaming", "Poisoned", "Ice"};
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

        tileW = 32;     // TODO set to 17 for iso
        tileH = 32;     // TODO set to 34 for iso
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

        // ISO
        ResourceManager.loadImage(ISOFLOOR);
        ResourceManager.loadImage(ISOWALL);

    }

    public static void main(String[] args) {
    	Main game = new Main("Dungeon Crawl", 1024, 672);
    	try {
			ItemManager im = new ItemManager(game);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        AppGameContainer app;
        try {
            app = new AppGameContainer(game);
            app.setDisplayMode(1024, 672, false);
            app.setVSync(true);
//            app.setShowFPS(false);      // disable fps
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
