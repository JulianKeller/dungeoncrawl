package client;

import jig.Entity;
import jig.ResourceManager;

import java.io.*;
import java.net.*;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.StateBasedGame;

import java.util.ArrayList;
import server.Msg;


public class Main extends StateBasedGame {
    // server.Server items
    public Socket socket = null;
    public ObjectInputStream dis = null;
    public ObjectOutputStream dos = null;
    public static boolean localMode = false;
    public int serverId;

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
    public static final String SKELETON_BOSS = "resources/skeleton/skeleton_boss2.png";

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
    
    //character effect particles
    public static final String RED_FLOATING_PLUS = "resources/effects/red_plus.png";
    public static final String BLUE_FLOATING_PLUS = "resources/effects/blue_plus.png";
    public static final String GREEN_FLOATING_PLUS = "resources/effects/green_plus.png";
    public static final String YELLOW_FLOATING_PLUS = "resources/effects/yellow_plus.png";
    public static final String GRAY_FLOATING_PLUS = "resources/effects/gray_plus.png";
    public static final String WHITE_FLOATING_PLUS = "resources/effects/white_plus.png";
    
    public static final String FLAME_PARTICLES = "resources/effects/flame.png";
    public static final String POISON_PARTICLES = "resources/effects/poison.png";
    
    public static final String GHOST_FACES = "resources/effects/ghost_faces.png";
    
    public static final String FLIES_EFFECT = "resources/effects/flies.png";
    
    public static final String THORNS_EFFECT = "resources/effects/thorns.png";
    
    public static final String REFLECT_EFFECT = "resources/effects/reflect.png";
    
    public static final String INVISIBLE_EFFECT = "resources/effects/invisibility.png";
    
    public static final String ICE_EFFECT = "resources/effects/ice_shards.png";
    
    public static final String LIGHTNING_EFFECT = "resources/effects/lightning.png";
    
    //player hud
    public static final String BAR_BASE = "resources/hud/bar_base.png";
    public static final String HEALTHBAR_SYM = "resources/hud/healthbar_symbol.png";
    public static final String WEIGHT_SYM = "resources/hud/weight_symbol.png";
    public static final String MANA_SYM = "resources/hud/mana_symbol.png";
    
    //sound effects
    public static final String ARMOR_EQUIP = "resources/sfx/armor_equip.wav";
    public static final String ARMOR_UP = "resources/sfx/armor_up.wav";
    public static final String ARROW_FIRE = "resources/sfx/arrow_fire.wav";
    public static final String BURNING_SE = "resources/sfx/burning.wav";
    public static final String CHARACTER_DEATH = "resources/sfx/character_death.wav";
    public static final String CHARACTER_HIT = "resources/sfx/character_hit.wav";
    public static final String CLICK_SE = "resources/sfx/click.wav";
    public static final String CURSE_SE = "resources/sfx/curse.wav";
    public static final String DAMAGE_UP = "resources/sfx/damage_up.wav";
    public static final String DROP_ITEM = "resources/sfx/drop.wav";
    public static final String ELECTRICITY_SE = "resources/sfx/electricity.wav";
    public static final String ENTER_LEVEL = "resources/sfx/enter.wav";
    public static final String FREEZE_SE = "resources/sfx/freeze.wav";
    public static final String FRIGHT_SE = "resources/sfx/fright.wav";
    public static final String HEALING_SE = "resources/sfx/healing.wav";
    public static final String IDENTIFY_SE = "resources/sfx/identify.wav";
    public static final String INVISIBLE_SE = "resources/sfx/invisible.wav";
    public static final String KNIGHT_PUNCH = "resources/sfx/knight_hit.wav";
    public static final String MANA_SE = "resources/sfx/mana.wav";
    public static final String OPEN_INVENTORY = "resources/sfx/open_inventory.wav";
    public static final String ITEM_PICKUP = "resources/sfx/pickup.wav";
    public static final String POISONED_SE = "resources/sfx/poisoned.wav";
    public static final String POTION_BREAK = "resources/sfx/potion_break.wav";
    public static final String POTION_DRINK = "resources/sfx/potion_drink.wav";
    public static final String POTION_THROW = "resources/sfx/potion_throw.wav";
    public static final String REFLECT_SE = "resources/sfx/reflect.wav";
    public static final String SKELETON_DEATH = "resources/sfx/skeleton_death.wav";
    public static final String SKELETON_HIT = "resources/sfx/skeleton_hit.wav";
    public static final String SPEED_UP = "resources/sfx/speed_up.wav";
    public static final String LAUNCH_SPELL = "resources/sfx/spell_fire.wav";
    public static final String STENCH_SE = "resources/sfx/stench.wav";
    public static final String STRENGTH_UP = "resources/sfx/strength.wav";
    public static final String SWORD_SWING = "resources/sfx/sword_swing.wav";
    public static final String SWORD_EQUIP = "resources/sfx/sword_unsheathe.wav";
    public static final String TANK_PUNCH = "resources/sfx/tank_hit.wav";
    public static final String THORNS_SE = "resources/sfx/thorns.wav";
    public static final String WALL_HIT = "resources/sfx/wall_hit.wav";
    
    //music
    public static final String TRACK1 = "resources/music/floating_cities.wav";
    public static final String TRACK2 = "resources/music/ghost_story.wav";
    public static final String TRACK3 = "resources/music/giant_wyrm.wav";
    

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
    Character boss;
    public ArrayList<Character> characters;
    ArrayList<Character> enemies;

    // create an item manager
    public static ItemManager im;
    Entity[][] potions;
    ArrayList<AnimateEntity> animations;
    
    //sound effect manager (client only)
    public static SFXManager sm;
    public ArrayList<Sound> sfxToAdd;

    //item types
    public static final String[] ItemTypes = {"Potion", "Armor", "Sword", "Arrow", "Staff", "Glove"};
    
    //item effects
    public static final String[] PotionEffects = {"Healing", "Strength", "Flame", "Mana", "Invisibility"};
    public static final String[] ArrowEffects = {"Flame", "Poison", "Ice", ""};
    public static final String[] StaffEffects = {"Healing", "Lightning", "Flame", "Ice"};
    public static final String[] ArmorEffects = {"Stench", "Iron Skin", "Thorns", "Swiftness"};
    public static final String[] SwordEffects = {"Fright", "Might", "Flame", "Ice"};
    public static final String[] GloveEffects = {"Swiftness", "Regeneration", "Reflection"};
    
    


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
        
        sfxToAdd = new ArrayList<Sound>();

        
        
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
        ResourceManager.loadImage(SKELETON_BOSS);

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
        
        //character effects
        ResourceManager.loadImage(RED_FLOATING_PLUS);
        ResourceManager.loadImage(GREEN_FLOATING_PLUS);
        ResourceManager.loadImage(BLUE_FLOATING_PLUS);
        ResourceManager.loadImage(GRAY_FLOATING_PLUS);
        ResourceManager.loadImage(YELLOW_FLOATING_PLUS);
        ResourceManager.loadImage(WHITE_FLOATING_PLUS);
        
        ResourceManager.loadImage(FLAME_PARTICLES);
        ResourceManager.loadImage(POISON_PARTICLES);
        
        ResourceManager.loadImage(GHOST_FACES);
        
        ResourceManager.loadImage(FLIES_EFFECT);
        
        ResourceManager.loadImage(THORNS_EFFECT);

        ResourceManager.loadImage(REFLECT_EFFECT);
        
        ResourceManager.loadImage(INVISIBLE_EFFECT);
        
        ResourceManager.loadImage(ICE_EFFECT);
        
        ResourceManager.loadImage(LIGHTNING_EFFECT);
        
        //player hud
        ResourceManager.loadImage(BAR_BASE);
        ResourceManager.loadImage(HEALTHBAR_SYM);
        ResourceManager.loadImage(WEIGHT_SYM);
        ResourceManager.loadImage(MANA_SYM);
        
        //music tracks
        ResourceManager.loadMusic(TRACK1);
        ResourceManager.loadMusic(TRACK2);
        ResourceManager.loadMusic(TRACK3);
        
    }
    
    public static void loadSounds() throws SlickException{
        //sounds
        //to add a sound, load it into the ResourceManager
        //then add it to the SFXManager
    	
    	
    	
    	ResourceManager.loadSound(ARMOR_EQUIP);
    	ResourceManager.loadSound(ARMOR_UP);
    	ResourceManager.loadSound(ARROW_FIRE);
    	ResourceManager.loadSound(BURNING_SE);
    	ResourceManager.loadSound(CHARACTER_DEATH);
    	ResourceManager.loadSound(CHARACTER_HIT);
    	ResourceManager.loadSound(CLICK_SE);
    	ResourceManager.loadSound(CURSE_SE);
    	ResourceManager.loadSound(DAMAGE_UP);
    	ResourceManager.loadSound(DROP_ITEM);
    	ResourceManager.loadSound(ELECTRICITY_SE);
    	ResourceManager.loadSound(ENTER_LEVEL);
    	ResourceManager.loadSound(FREEZE_SE);
    	ResourceManager.loadSound(FRIGHT_SE);
    	ResourceManager.loadSound(HEALING_SE);
    	ResourceManager.loadSound(IDENTIFY_SE);
    	ResourceManager.loadSound(INVISIBLE_SE);
    	ResourceManager.loadSound(KNIGHT_PUNCH);
    	ResourceManager.loadSound(MANA_SE);
    	ResourceManager.loadSound(OPEN_INVENTORY);
    	ResourceManager.loadSound(ITEM_PICKUP);
    	ResourceManager.loadSound(POISONED_SE);
    	ResourceManager.loadSound(POTION_BREAK);
    	ResourceManager.loadSound(POTION_DRINK);
    	ResourceManager.loadSound(POTION_THROW);
    	ResourceManager.loadSound(REFLECT_SE);
    	ResourceManager.loadSound(SKELETON_DEATH);
    	ResourceManager.loadSound(SKELETON_HIT);
    	ResourceManager.loadSound(SPEED_UP);
    	ResourceManager.loadSound(LAUNCH_SPELL);
    	ResourceManager.loadSound(STENCH_SE);
    	ResourceManager.loadSound(STRENGTH_UP);
    	ResourceManager.loadSound(SWORD_SWING);
    	ResourceManager.loadSound(SWORD_EQUIP);
    	ResourceManager.loadSound(TANK_PUNCH);
    	ResourceManager.loadSound(THORNS_SE);
    	ResourceManager.loadSound(WALL_HIT);
    	
    	SFXManager.addSound( "equip_armor", ResourceManager.getSound(ARMOR_EQUIP));
    	SFXManager.addSound( "armor_up", ResourceManager.getSound(ARMOR_UP));
    	SFXManager.addSound( "shoot_arrow", ResourceManager.getSound(ARROW_FIRE));
    	SFXManager.addSound( "burning", ResourceManager.getSound(BURNING_SE));
    	SFXManager.addSound( "character_death", ResourceManager.getSound(CHARACTER_DEATH));
    	SFXManager.addSound( "character_hit", ResourceManager.getSound(CHARACTER_HIT));
    	SFXManager.addSound( "click", ResourceManager.getSound(CLICK_SE));
    	SFXManager.addSound( "curse", ResourceManager.getSound(CURSE_SE));
    	SFXManager.addSound( "damage_up", ResourceManager.getSound(DAMAGE_UP));
    	SFXManager.addSound( "item_drop", ResourceManager.getSound(DROP_ITEM));
    	SFXManager.addSound( "electricity", ResourceManager.getSound(ELECTRICITY_SE));
    	SFXManager.addSound( "enter_level", ResourceManager.getSound(ENTER_LEVEL));
    	SFXManager.addSound( "freezing", ResourceManager.getSound(FREEZE_SE));
    	SFXManager.addSound( "fright", ResourceManager.getSound(FRIGHT_SE));
    	SFXManager.addSound( "healing", ResourceManager.getSound(HEALING_SE));
    	SFXManager.addSound( "identify", ResourceManager.getSound(IDENTIFY_SE));
    	SFXManager.addSound( "invisible", ResourceManager.getSound(INVISIBLE_SE));
    	SFXManager.addSound( "knight_punch", ResourceManager.getSound(KNIGHT_PUNCH));
    	SFXManager.addSound( "mana_up", ResourceManager.getSound(MANA_SE));
    	SFXManager.addSound( "open_inventory", ResourceManager.getSound(OPEN_INVENTORY));
    	SFXManager.addSound( "item_pickup", ResourceManager.getSound(ITEM_PICKUP));
    	SFXManager.addSound( "poisoned", ResourceManager.getSound(POISONED_SE));
    	SFXManager.addSound( "potion_break", ResourceManager.getSound(POTION_BREAK));
    	SFXManager.addSound( "potion_drink", ResourceManager.getSound(POTION_DRINK));
    	SFXManager.addSound( "potion_throw", ResourceManager.getSound(POTION_THROW));
    	SFXManager.addSound( "reflecting", ResourceManager.getSound(REFLECT_SE));
    	SFXManager.addSound( "skeleton_death", ResourceManager.getSound(SKELETON_DEATH));
    	SFXManager.addSound( "skeleton_hit", ResourceManager.getSound(SKELETON_HIT));
    	SFXManager.addSound( "speed_up", ResourceManager.getSound(SPEED_UP));
    	SFXManager.addSound( "launch_spell", ResourceManager.getSound(LAUNCH_SPELL));
    	SFXManager.addSound( "stench", ResourceManager.getSound(STENCH_SE));
    	SFXManager.addSound( "strength_up", ResourceManager.getSound(STRENGTH_UP));
    	SFXManager.addSound( "sword_swing", ResourceManager.getSound(SWORD_SWING));
    	SFXManager.addSound( "equip_sword", ResourceManager.getSound(SWORD_EQUIP));
    	SFXManager.addSound( "tank_punch", ResourceManager.getSound(TANK_PUNCH));
    	SFXManager.addSound( "thorns", ResourceManager.getSound(THORNS_SE));
    	SFXManager.addSound( "wall_hit", ResourceManager.getSound(WALL_HIT));
    }

    // Send close to the server and close connections before exiting.
    @Override
    public boolean closeRequested(){
        if(!localMode) {
            try {
                dos.writeObject(new Msg(serverId,"Exit",0,0,0, false));
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

    public static void main(String[] args) throws SlickException {
    	Main game = new Main("Dungeon Crawl", 1280, 736);
    	im = new ItemManager(game);
    	
    	loadSounds();
    	
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
