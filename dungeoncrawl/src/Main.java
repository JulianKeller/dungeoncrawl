import jig.Entity;
import jig.ResourceManager;

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
    public static final String WALL_TOP = "resources/wall/wall_2dtop.png";

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

    // potions
    public static final String POTION_BLUE = "resources/potions/blue_potion.png";
    public static final String POTION_RED = "resources/potions/red_potion.png";
    public static final String POTION_YELLOW = "resources/potions/yellow_potion.png";
    public static final String POTION_PINK = "resources/potions/pink_potion.png";
    public static final String POTION_ORANGE = "resources/potions/orange_potion.png";

    // Knight
    public static final String KNIGHT_LEATHER = "resources/knight/knight_leather.png";
    public static final String KNIGHT_IRON = "resources/knight/knight_iron.png";

    // Mage

    // Archer

    // Tank

    // Bad Guy

    // Screen Size
    public final int ScreenWidth;
    public final int ScreenHeight;

    // declare entities
    int tileW;
    int tileH;
    int[][] map;
    Entity[][] entities;
    Entity[][] potions;


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

        // POTIONS
        ResourceManager.loadImage(POTION_BLUE);
        ResourceManager.loadImage(POTION_ORANGE);
        ResourceManager.loadImage(POTION_PINK);
        ResourceManager.loadImage(POTION_RED);
        ResourceManager.loadImage(POTION_YELLOW);

        // KNIGHT
        ResourceManager.loadImage(KNIGHT_LEATHER);
        ResourceManager.loadImage(KNIGHT_IRON);
    }

    public static void main(String[] args) {
        AppGameContainer app;
        try {
            app = new AppGameContainer(new Main("Dungeon Crawl", 1024, 672));
            app.setDisplayMode(1024, 672, false);
            app.setVSync(true);
//            app.setShowFPS(false);      // disable fps
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
