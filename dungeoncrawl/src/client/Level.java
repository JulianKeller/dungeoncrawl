package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import server.Msg;
import server.ItemMsg;



import jig.Vector;


import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.openal.AudioImpl;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


import client.MovingEntity.Effect;
import jig.ResourceManager;
import server.Server;


public class Level extends BasicGameState {
    private Boolean paused;
    private Random rand;

    private String type;

    private int[][] rotatedMap;

    Socket socket;
    ObjectInputStream inStream;
    ObjectOutputStream outStream;
    String serverMessage;

    int tilesize = 32;
    int offset = tilesize/2;
    int doubleOffset = offset/2;

    private final int messageTimer = 2000;
    
    private int attackCooldown = 0;


    private ArrayList<Item> itemsToRender;
    
    private ArrayList<Character> targets; //union of enemies and characters

    //whether to display player inventory/codex on the screen
    /*
    private boolean displayInventory = false;
    private boolean displayCodex = false;
    private boolean displayCharacterSheet = false;
    */
    private int itemx = 0;
    private int itemy = 0; //which item is currently selected in the inventory
    private int selectedEquippedItem = 0; //item selected in the hotbar

    private class Message{
        protected int timer = messageTimer;
        protected String text;
        protected Message(String text){
            this.text = text;
        }
    }

    //array of messages to print to the screen
    private Message[] messagebox;
    private int messages = 4; //number of messages printed at one time

    private class ItemLockTimer{
        //timer to unlock an item so it can be picked up again
        int itemID;
        int timer = 3000; //unlock when this hits zero

        public ItemLockTimer(int id){
            itemID = id;
        }
    }
    private ArrayList<ItemLockTimer> itemLockTimers;

    public void setType(String t){
        type = t;
    }

    public String setSkin(){
        switch(type){
            case "Knight":
                return "knight_leather";
            case "Mage":
                return "mage_leather";
            case "Archer":
                return "archer_leather";
            case "Tank":
                return "tank_leather";
            default:
                break;
        }
        return "";
    }

    private class ThrownItem{
        Item itm;
        Vector direction;
        Vector finalLocation;
        Vector step;

        public ThrownItem(Item itm, Vector direction, Vector finalLocation, Vector step){
            this.itm = itm;
            this.direction = direction;
            this.finalLocation = finalLocation;
            this.step = step;
        }
    }

    private ArrayList<ThrownItem> thrownItems;

    @Override
    public int getID() {
        return Main.LEVEL1;
    }
    
    //music tracks
    private Music currentTrack = null;
    private Music[] tracks = new Music[3];

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        serverMessage = "";
        Main dc = (Main) game;
        paused = false;
        
        //let there be sound \o/
        container.setSoundOn(true);

        messagebox = new Message[messages]; //display four messages at a time

        this.socket = dc.socket;
        this.inStream = dc.dis;
        this.outStream = dc.dos;


        // Grab the map from the server.Server

        try {
            dc.map = (int[][]) inStream.readObject();
//            System.out.println("reading dc.map type: " + dc.map.getClass().getSimpleName());
//           System.out.println("I got the map!");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

//        System.out.printf("Map Size: %s, %s\n", dc.map[0].length, dc.map.length);

        //rotated map verified correct
        rotatedMap = new int[dc.map[0].length][dc.map.length];
        for( int i = 0; i < dc.map.length; i++ ){
            for( int j = 0; j < dc.map[i].length; j++ ){
                //g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
                //i is y, j is x
                rotatedMap[j][i] = dc.map[i][j];
            }
        }



        // initialize itemsToRender
        itemsToRender = new ArrayList<>();
//        itemsToRender = Main.im.itemsInRegion(new Vector(0, 0), new Vector(100, 100));

        //find a tile with no walls in its horizontal adjacencies
        rand = new Random();
        rand.setSeed(System.nanoTime());

        int row = rand.nextInt(dc.ScreenHeight/dc.tilesize);
        int col = rand.nextInt(dc.ScreenWidth/dc.tilesize);

        while(dc.map[row][col] == 1 || wallAdjacent(row, col, dc.map) ){
            //spawn on a floor tile with 4 adjacent floor tiles
            row = rand.nextInt(dc.ScreenHeight/dc.tilesize);
            col = rand.nextInt(dc.ScreenWidth/dc.tilesize);
        }

        // map variables
        //Main dc = (Main) game;
        dc.mapWidth = dc.map[0].length;
        dc.mapHeight = dc.map.length;

        // TODO setting the hero coordinates/type should be done on the server
        // setup the dc.hero character
        float wx = (dc.tilesize * 20) - dc.offset;
        float wy = (dc.tilesize * 18) - dc.tilesize - dc.doubleOffset;

//        System.out.printf("setting character at %s, %s\n", wx, wy);

        // Setting starting position for the hero.
        String coord = wx + " " + wy;
        int id = 0;
        String type = setSkin();
        try {
            id = inStream.readInt();
//            System.out.println("reading id: " + id);
            //System.out.println("Sending my player info.");
            dc.serverId = id;
        }catch(IOException e){
            e.printStackTrace();
        }
        dc.hero = new Character(dc, wx, wy, type, id, this, false);
        dc.characters.add(dc.hero);

        receiveEnemyList(dc);
        receiveItemList();

        // render map
        RenderMap.setMap(dc, dc.hero);

        thrownItems = new ArrayList<ThrownItem>();

        // Test Items
//        addTestItems(dc);
        
        //give the mage a ruby staff with random effect
        //public Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified, Image image, int count) throws SlickException{
        if( dc.hero.getInventory().size() == 0 ){
            if( dc.hero.getType().toLowerCase().contains("mage")){
                try {
                    Item staff = new Item(null, false, -1, -1, "Healing", "Staff", "Ruby", false, true, ResourceManager.getImage(Main.STAFF_RUBY), 1);

                    Random rand = new Random();
                    rand.setSeed(System.nanoTime());

                    staff.setEffect(Main.StaffEffects[ rand.nextInt(Main.StaffEffects.length) ]);

                    Main.im.give(staff, dc.hero);

                } catch (SlickException e1) {
                    return;
                }
            }else if( dc.hero.getType().toLowerCase().contains("archer")){
                try {
                    Item arrow = new Item(null, false, -1, -1, "", "Arrow", "", false, true, ResourceManager.getImage(Main.ARROW_NORMAL), 10);

                    Main.im.give(arrow, dc.hero);


                } catch (SlickException e1) {
                    return;
                }
            }
        }
        targets = new ArrayList<Character>();
        targets.addAll(dc.characters);
        targets.addAll(dc.enemies);
        
        //pick music track from available tracks
        currentTrack = tracks[ rand.nextInt(tracks.length) ];
        
        currentTrack.play(1, 0.3f);
        
        //add item boxes
        itemBoxes.add(new ItemBox("Inventory", 4, 8));
        itemBoxes.add(new ItemBox("Codex", 10, 8));
        itemBoxes.add(new ItemBox("Character", 5, 6));
        
    }

    private void receiveItemList(){
        try {
            ArrayList<ItemMsg> fromServer = (ArrayList)inStream.readObject();
            //System.out.println("Read type: "+fromServer.getClass().getSimpleName());
            for(ItemMsg i : fromServer) {
                try {
                    System.out.println("Adding item: "+i.type);
                    Item item = new Item(new Vector(i.wx,i.wy),false,i.id,i.oid,i.effect,
                            i.type,i.material,i.cursed,i.identified,null,i.count);
                    setItemImage(item);
                    Main.im.addToWorldItems(item);
                }catch(SlickException e){
                    e.printStackTrace();
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void receiveEnemyList(Main dc){
        //Grabbing ArrayList of enemies.
        ArrayList<Msg> enemyList = new ArrayList<>();
        try{
            enemyList = (ArrayList<Msg>) inStream.readObject();
//            System.out.println("reading enemyList type: " + enemyList.getClass().getSimpleName());
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        for (Msg e : enemyList) {
            float x = e.wx;
            float y = e.wy;
            int eid = e.id;
            dc.enemies.add(new Character(dc, x, y, e.type, eid, this, true));
//            System.out.println("Created AI " + e.toString());
        }
    }
    private void setItemImage(Item i) throws SlickException{
        //get an image based on item type
        Image image = null;
        if( i.getType().equals("Potion") ){
            //int r = rand.nextInt(5);
            switch( i.getMaterial() ){
                case "Blue":
                    image = ResourceManager.getImage(Main.POTION_BLUE);
                    //material = "Blue";
                    break;
                case "Orange":
                    image = ResourceManager.getImage(Main.POTION_ORANGE);
                    //material = "Orange";
                    break;
                case "Pink":
                    image = ResourceManager.getImage(Main.POTION_PINK);
                    //material = "Pink";
                    break;
                case "Red":
                    image = ResourceManager.getImage(Main.POTION_RED);
                    //material = "Red";
                    break;
                case "Yellow":
                    image = ResourceManager.getImage(Main.POTION_YELLOW);
                    //material = "Yellow";
                    break;
            }
        }else if( i.getType().equals("Sword") ){
            //wood, iron, gold
            if( i.getMaterial().equals("Wooden") ){
                image = ResourceManager.getImage(Main.SWORD_WOOD);
            }else if( i.getMaterial().equals("Iron") ){
                image = ResourceManager.getImage(Main.SWORD_IRON);
            }else if( i.getMaterial().equals("Gold") ){
                image = ResourceManager.getImage(Main.SWORD_GOLD);
            }else{
                throw new SlickException("Invalid sword material '" + i.getMaterial() + "'.");
            }
        }else if( i.getType().equals("Armor") ){
            //iron, gold
            if( i.getMaterial().equals("Iron") ){
                image = ResourceManager.getImage(Main.ARMOR_IRON);
            }else if( i.getMaterial().equals("Gold") ){
                image = ResourceManager.getImage(Main.ARMOR_GOLD);
            }else{
                throw new SlickException("Invalid armor material '" + i.getMaterial() + "'.");
            }
        }else if( i.getType().equals("Arrow") ){
            if( i.getEffect().equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME);
            }else if( i.getEffect().equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON);
            }else if( i.getEffect().equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE);
            }else if( i.getEffect().equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL);
            }else{
                throw new SlickException("Invalid arrow effect '" + i.getEffect() + "'.");
            }
        }else if( i.getType().equals("Gloves") ){
            if( i.getMaterial().equals("Red") ){
                image = ResourceManager.getImage(Main.GLOVES_RED);
            }else if( i.getMaterial().equals("White") ){
                image = ResourceManager.getImage(Main.GLOVES_WHITE);
            }else if( i.getMaterial().equals("Yellow") ){
                image = ResourceManager.getImage(Main.GLOVES_YELLOW);
            }else{
                throw new SlickException("Invalid glove material '" + i.getMaterial() + "'.");
            }
        }else if( i.getType().equals("Staff") ){
            if( i.getMaterial().equals("Ruby") ){
                image = ResourceManager.getImage(Main.STAFF_RUBY);
            }else if( i.getMaterial().equals("Emerald") ){
                image = ResourceManager.getImage(Main.STAFF_EMERALD);
            }else if( i.getMaterial().equals("Amethyst") ){
                image = ResourceManager.getImage(Main.STAFF_AMETHYST);
            }else{
                throw new SlickException("Invalid staff material '" + i.getMaterial() + "'.");
            }
        }
        i.setImage(image);
    }
    private boolean wallAdjacent(int row, int col, int[][] map){
        //check if there is a wall or map edge in a horizontally adjacent tile
        try{
            if( map[row-1][col] == 1 ){
                return true;
            }else if( map[row+1][col] == 1 ){
                return true;
            }else if( map[row][col-1] == 1 ){
                return true;
            }else if( map[row][col+1] == 1 ){
                return true;
            }else if( map[row][col] == 1 ){
                return true;
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            return true;
        }
        return false;
    }


    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        messagebox = new Message[messages];

        itemLockTimers = new ArrayList<ItemLockTimer>();
        
        tracks[0] = ResourceManager.getMusic(Main.TRACK1);
        tracks[1] = ResourceManager.getMusic(Main.TRACK2);
        tracks[2] = ResourceManager.getMusic(Main.TRACK3);
        
    }


    public void addMessage(String message){
        //check for a duplicate message
        for( Message m : messagebox ){
            if( m == null ){
                break;
            }
            if( m.text.equals(message)){
                return;
            }
        }

        //add a message to the first index of the message box
        //  and shift everything else down
        for( int i = messagebox.length-1; i > 0; i-- ){
            messagebox[i] = messagebox[i-1];
        }
        messagebox[0] = new Message(message);
    }
    
    public Image getArrowImage(String effect, String dir) throws SlickException{
    	 //get image based on effect and direction
        Image image = null;
        if( dir == null ){
        	//spawn arrow item
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL);
            }
        }else if( dir.equals("up") ){
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON_UP);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME_UP);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE_UP);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL_UP);
            }
        }else if( dir.equals("down") ){
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON_DOWN);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME_DOWN);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE_DOWN);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL_DOWN);
            }
        }else if( dir.equals("left") ){
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON_LEFT);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME_LEFT);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE_LEFT);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL_LEFT);
            }
        }else if( dir.equals("right") ){
            if( effect.equals("Poison") ){
                image = ResourceManager.getImage(Main.ARROW_POISON_RIGHT);
            }else if( effect.equals("Flame") ){
                image = ResourceManager.getImage(Main.ARROW_FLAME_RIGHT);
            }else if( effect.equals("Ice") ){
                image = ResourceManager.getImage(Main.ARROW_ICE_RIGHT);
            }else if( effect.equals("") ){
                image = ResourceManager.getImage(Main.ARROW_NORMAL_RIGHT);
            }
        }
        if( image == null ){
            throw new SlickException("Invalid arrow effect " + effect);
        }
        return image;
    }


    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dc = (Main) game;
        // render tiles
        displayMap(dc, g);

        //render all visible items
        renderItems(dc, g);

        // TODO will need to sort the lists and draw in order so players draw on top of others
        // draw other characters
        renderCharacters(dc, g);

        renderEnemies(dc, g);

        // draw the hero
        dc.hero.animate.render(g);
        
        //draw the hero's visual effects
        if( dc.hero.vfx != null ){
        	dc.hero.vfx.render(g);
        }
        
        //draw the vfx for all characters
        for( Character ch : targets ){
        	if( ch.vfx != null ){
        		ch.vfx.render(g);
        	}
        }
        

        renderHealthBar(dc, g);

        //render messages
        renderMessages(dc, g);
        
        //render item box containers
        renderItemBoxes(g, dc);

        //display player inventory
        renderInventory(dc, g);

        // render the codex
        renderCodex(dc, g);
        
        renderCharacterSheet(dc, g);

        //draw the player's equipped items
        renderEquippedItems(dc, g);

        if (dc.showPath) {
            renderShortestPath(dc, g);
        }
        
        int baseWidth = 256;
        float currentHealthBarWidth = baseWidth * (dc.hero.getHitPoints()/dc.hero.getStartingHitPoints());
        
        float currentWeightBarWidth = baseWidth * ((float) dc.hero.getInventoryWeight()/ (float) dc.hero.getMaxInventoryWeight());
        if( currentWeightBarWidth > 256 ){
        	currentWeightBarWidth = 256;
        }
        
        //render visual hud
        float alpha = 1;
        if( dc.hero.animate.getX() > dc.ScreenWidth - (baseWidth+100) && dc.hero.animate.getY() > dc.ScreenHeight-((dc.tilesize*5)+100) ){
        	alpha = 0.2f;
        }
        Color tmp = g.getColor();
        g.setColor(new Color(255, 0, 0, alpha) );
        g.fillRoundRect(dc.ScreenWidth - ((256 + dc.tilesize)-(baseWidth-currentHealthBarWidth)), dc.ScreenHeight-(dc.tilesize*3), currentHealthBarWidth, dc.tilesize, 0);
        g.setColor(tmp);
        g.drawImage(ResourceManager.getImage(Main.BAR_BASE), dc.ScreenWidth - (256 + dc.tilesize), dc.ScreenHeight - (dc.tilesize*3));

        g.drawImage(ResourceManager.getImage(Main.HEALTHBAR_SYM), dc.ScreenWidth - dc.tilesize  - dc.tilesize/2, dc.ScreenHeight - (dc.tilesize*3));

        //render inventory weight bar
        if( dc.hero.getInventoryWeight() != 0 ){
	        tmp = g.getColor();
	        g.setColor(new Color(0, 255, 0, alpha) );
	        g.fillRoundRect(dc.ScreenWidth - ((256 + dc.tilesize)-(baseWidth-currentWeightBarWidth)), dc.ScreenHeight-(dc.tilesize*4), currentWeightBarWidth, dc.tilesize, 0);
	        g.setColor(tmp);
        }
        
        g.drawImage(ResourceManager.getImage(Main.BAR_BASE), dc.ScreenWidth - (256 + dc.tilesize), dc.ScreenHeight-(dc.tilesize*4));
        g.drawImage(ResourceManager.getImage(Main.WEIGHT_SYM), dc.ScreenWidth - dc.tilesize - dc.tilesize/2, dc.ScreenHeight - (dc.tilesize*4));
        
        //render mana bar if character is a mage
        if( dc.hero.getType().toLowerCase().contains("mage") ){
        	
        	float currentManaBarWidth = baseWidth * (dc.hero.getMana()/dc.hero.getMaxMana());
            tmp = g.getColor();
            g.setColor(new Color(0, 0, 255, alpha) );
            g.fillRoundRect(dc.ScreenWidth - ((256 + dc.tilesize)-(baseWidth-currentManaBarWidth)), dc.ScreenHeight-(dc.tilesize*5), currentManaBarWidth, dc.tilesize, 0);
            g.setColor(tmp);
            
            g.drawImage(ResourceManager.getImage(Main.BAR_BASE), dc.ScreenWidth - (256 + dc.tilesize), dc.ScreenHeight-(dc.tilesize*5));
            g.drawImage(ResourceManager.getImage(Main.MANA_SYM), dc.ScreenWidth - dc.tilesize - dc.tilesize/2, dc.ScreenHeight - (dc.tilesize*5));
        }
        
        
        
        // display a paused message
        if (paused) {
            renderPauseMessage(dc, g);
            renderActiveCheats(dc, g);
        }
    }

    private void renderPauseMessage(Main dc, Graphics g) {
        // TODO render paused message
        Color tmp = g.getColor();
        g.setColor(new Color(0, 0, 0, .3f));
        g.fillRect(0, 0, dc.ScreenWidth, dc.ScreenHeight);
        g.setColor(new Color(255, 255, 255, 1f));
        g.drawString("PAUSED", (float) dc.ScreenWidth/2 - 12, (float) dc.ScreenHeight/2 - 10);
        g.setColor(tmp);

        // show controls
        tmp = g.getColor();
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

    private void renderActiveCheats(Main dc, Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(255, 255, 255, 1f));
        // render cheats
        if (!dc.showPath){
            g.drawString("Display Paths: Disabled", 50, 50);
        }
        else {
            g.drawString("Display Paths: Enabled", 50, 50);
        }
        if (dc.collisions) {
            g.drawString("Collisions: Enabled", 50, 75);
        }
        else {
            g.drawString("Collisions: Disabled", 50, 75);
        }
        if (dc.invincible) {
            g.drawString("Invincible: Enabled", 50, 100);
        }
        else {
            g.drawString("Invincible: Disabled", 50, 100);
        }
        g.setColor(tmp);
    }

    /** Renders the AI's shortest path
     *
     */
    private void renderShortestPath(Main dc, Graphics g) {
        // only load arrows if the player wants to show the dijkstra's path
        for (Character ai : dc.characters) {
            if (!ai.ai) {
                continue;
            }
            for (Arrow a : ai.arrows) {
                Vector sc = world2screenCoordinates(dc, a.getWorldCoordinates());
                a.setPosition(sc);
                a.render(g);
            }
        }

    }

    /**
     * set scaled to true to show the values smaller in the top
     * set coords true to see the game coordinates, this really drops fps, be warned
     * @param dc
     * @param g
     */
    private void renderPathWeights(Main dc, Graphics g) {
        boolean scaled = false;
        boolean coords = false;
        if (dc.hero.weights != null) {
            for (int i = 0; i < dc.hero.weights.length; i++) {
                for (int j = 0; j < dc.hero.weights[0].length; j++) {
                    Color tmp = g.getColor();
                    g.setColor(new Color(255, 255, 255, 1f));

                    //make the messages fade away based on their timers
                    String msg = String.valueOf((int) dc.hero.weights[i][j]);

                    if (!scaled) {
                        if (dc.hero.weights[i][j] > 200000) {
                            msg = "INF";
                        }
                        Vector wc = new Vector(i * dc.tilesize, j * dc.tilesize);
                        Vector sc = world2screenCoordinates(dc, wc);
                        g.drawString(msg, sc.getX(), sc.getY());
                    }
                    else {
                        g.scale(.5f, .5f);
                        Vector wc = new Vector(2 * i * dc.tilesize, 2 * j * dc.tilesize);
                        Vector sc = world2screenCoordinates(dc, wc);
                        g.drawString(msg, sc.getX(), sc.getY());

                        // draw x, y tile values
                        if (coords) {
                            g.setColor(new Color(255, 255, 255, .8f));
                            wc = new Vector(2 * i * dc.tilesize, 2 * j * dc.tilesize + dc.offset);
                            sc = world2screenCoordinates(dc, wc);
                            String I = String.valueOf(i);
                            String J = String.valueOf(j);
                            msg = "(" + I + "," + J + ")";
                            g.drawString(msg, sc.getX(), sc.getY());
                        }
                        g.scale(2f, 2f);
                    }
                    g.setColor(tmp);
                }
            }
        }
    }

    /**
     * Render debug information on the screen
     * @param dc
     * @param g
     */
    private void renderDebug(Main dc, Graphics g) {
        for( int i = 0; i < dc.map.length; i++ ){
            for( int j = 0; j < dc.map[i].length; j++ ){
                g.drawString(""+dc.map[i][j], j*dc.tilesize, i*dc.tilesize);
                //i is y, j is x
            }
        }

    }

    /**
     * Renders the players equipped items
     * @param dc
     * @param g
     */
    private void renderEquippedItems(Main dc, Graphics g) {
        Color tmp = g.getColor();
        g.setColor(new Color(0, 0, 0, 0.5f));
        g.fillRoundRect(dc.ScreenWidth-(dc.tilesize*5), dc.ScreenHeight-(dc.tilesize*2), dc.tilesize*4, dc.tilesize, 0);
        int x = 0;
        int y = 0;
        for( int i = 0; i < dc.hero.getEquipped().length; i++ ){
            if( dc.hero.getEquipped()[i] != null ){
                x = dc.ScreenWidth-(dc.tilesize*(dc.hero.getEquipped().length+1));
                y = dc.ScreenHeight-(dc.tilesize*2);
                g.drawImage(dc.hero.getEquipped()[i].getImage(), x+(dc.tilesize*i), y);
                Color tmp2 = g.getColor();
                g.setColor(Color.white);
                g.drawString(dc.hero.getEquipped()[i].count+"", x+(dc.tilesize*i), y+15);
                g.drawString(dc.hero.getEquipped()[i].getRequiredLevel()+"", x+(dc.tilesize*i), y);
                g.setColor(tmp2);
                

            }
        }

        g.setColor(Color.white);
        if( dc.hero.getEquipped()[selectedEquippedItem] != null ){
        	g.drawString(dc.hero.getEquipped()[selectedEquippedItem].toString(), dc.ScreenWidth - (256 + dc.tilesize), dc.ScreenHeight-(dc.tilesize));
        }
        if( x > 0 ){
        	g.drawRect(x+(dc.tilesize*selectedEquippedItem), y, dc.tilesize, dc.tilesize);
        }
        
        g.setColor(tmp);
    }
    
    
    private void renderCharacterSheet(Main dc, Graphics g){
    	//display useful character information to the player
		ItemBox ib = getItemBoxByTitle("Character");
		if( ib != null && ib.visible ){

    		
    		Color tmp = g.getColor();
    		g.setColor(Color.white);

    		ib.addString(g, dc, "Strength:   " + dc.hero.getStrength(), 0, 1);
    		ib.addString(g, dc, "Speed:      " + dc.hero.getMovementSpeed(), 0, 2);
    		ib.addString(g, dc, "Max mana:   " + (int) dc.hero.getMaxMana(), 0, 3);
    		ib.addString(g, dc, "Max health: " + (int) dc.hero.getStartingHitPoints(), 0, 4);
    		ib.addString(g, dc, "Max weight: " + (int) dc.hero.getMaxInventoryWeight(), 0, 5);
    		
    		g.setColor(tmp);
		}
	}
    	



    /**
     * Renders the codex on the players screen
     * @param dc
     * @param g
     */
    private void renderCodex(Main dc, Graphics g) {
    	ItemBox ib = getItemBoxByTitle("Codex");
    	if( ib != null && ib.visible ){
            ArrayList<Item> items = dc.hero.getCodex();
            int row = 1;
            for( Item i : items ){
            	ib.addImage(g, dc, i.getImage(), 0, row);
            	Color tmp = g.getColor();
            	g.setColor(Color.white);
            	ib.addString(g, dc, i.toString(), 1, row);
            	g.setColor(tmp);
            	
                row++;
            }
    	}
    }

    /**
     * Render the players inventory
     * @param dc
     * @param g
     */
    private void renderInventory(Main dc, Graphics g) {

        
        ItemBox ib = getItemBoxByTitle("Inventory");
        if( ib != null && ib.visible ){
        	Color tmp = g.getColor();
            g.setColor(Color.white);
            ArrayList<Item> items = dc.hero.getInventory();
            if( items.size() != 0 ){
                int row = 1;
                int col = 0;
                for( int i = 0; i < items.size(); i++ ){
                	
                	ib.addImage(g, dc, items.get(i).getImage(), col, row);
                	
                	ib.addStringFineGrained(g, dc, items.get(i).count+"", ((1+col)*dc.tilesize)-10, ((1+row)*dc.tilesize)-15);
                	
                	ib.addStringFineGrained(g, dc, items.get(i).getRequiredLevel()+"", ((1+col)*dc.tilesize)-10, ((1+row)*dc.tilesize)-30);

                    col++;
                    if( i > 4 && i % 4 == 0 ){
                        row++;
                        col = 1;
                    }
                }
                //draw a square around the selected item

                g.drawRect(
                        (itemx + 1)*dc.tilesize,
                        (itemy + 2)*dc.tilesize,
                        dc.tilesize,
                        dc.tilesize
                );
                
                
                //draw a string representation of the the selected item
                g.drawString( items.get((itemy*4)+itemx).toString(), dc.tilesize, 9*dc.tilesize);
                
                g.setColor(tmp);
            }
        }
    }

    /**
     * Renders messages
     * @param dc
     * @param g
     */
    private void renderMessages(Main dc, Graphics g) {
        for( Message m : messagebox ){
            if( m != null ){
                g.setColor(new Color(0, 0, 0, 0.5f));
                g.fillRoundRect(25, dc.ScreenHeight-(20 * messagebox.length), 400, 20 * messagebox.length, 0);
                g.setColor(Color.red);
                break;
            }
        }

        for( int i = 0; i < messagebox.length; i++ ){
            if( messagebox[i] == null || messagebox[i].text == "" ){
                break;
            }
            Color tmp = g.getColor();
            //make the messages fade away based on their timers
            g.setColor(new Color(255, 0, 0, (float) (messagebox[i].timer*2)/messageTimer));
            g.drawString(messagebox[i].text, 30, dc.ScreenHeight-(20 * (messagebox.length - i)));
            g.setColor(tmp);
        }
    }


    /**
     * Renders the players healthbar if the players health is below 100%
     * @param dc
     * @param g
     */
    private void renderHealthBar(Main dc, Graphics g) {
        Vector sc;
        float x;
        float y;
        float remaining;
        Color tmp = g.getColor();
        int width = 30;
        for (Character ch : dc.characters) {
            if (ch.getHitPoints() == ch.getStartingHitPoints()) {
                continue;
            }
            if (characterInRegion(dc, ch)) {
                sc = world2screenCoordinates(dc, ch);
                x = sc.getX() - offset + 1;
                y = sc.getY() - tilesize + 2;// + offset;
                remaining = (ch.getHitPoints()/ch.getStartingHitPoints())* width;

                // total health
                g.setColor(new Color(255, 0, 0, 0.5f));
                g.fillRoundRect(x, y, width, 3, 0);

                // remaining health
                g.setColor(new Color(0, 255, 0, 0.5f));
                g.fillRoundRect(x, y, remaining, 3, 0);

            }
        }
        g.setColor(tmp);
    }

    /**
     * Renders the visible items on the map
     * @param dc
     * @param g
     */
    private void renderItems(Main dc, Graphics g) {
        // get the items in range
        Vector wc;
        ArrayList<Item> worldItems = Main.im.getWorldItems();
        g.setColor(Color.red);
        for (Item i : worldItems) {
            // convert the world tile coordinates to world pixel coordinates
            wc = new Vector((i.getWorldCoordinates().getX() * dc.tilesize) + (float) (dc.tilesize / 2),
                    (i.getWorldCoordinates().getY() * dc.tilesize) + (float) (dc.tilesize / 2));
            // draw the objects on the screen
            if (objectInRegion(dc, wc)) {
                Vector sc = world2screenCoordinates(dc, wc);
                i.setPosition(sc);
                i.render(g);
            }
        }
    }

    /**
     * Renders new items which are being tested
     * @param dc
     * @param g
     */
    private void renderTestItems(Main dc, Graphics g) {
        Vector wc;
        for (DisplayItem i : dc.testItems) {
            wc = i.getWorldCoordinates();
            // draw the objects on the screen
            if (objectInRegion(dc, wc)) {
                Vector sc = world2screenCoordinates(dc, wc);
                i.setPosition(sc);
                i.render(g);
            }
        }
    }


    /**
     * helper Used for testing new items
     */
    private void addTestItems(Main dc) {
        String[] itemList = new String[] {
                "staff_emerald", "staff_ameythst", "staff_ruby",
                "gloves_red", "gloves_white", "gloves_yellow",
                "robes_blue", "robes_purple",
                "archer_clothes_green", "archer_clothes_iron"
        };
        int position = 16;
        for (int i = 0; i < itemList.length; i++) {
            position += 1;
            dc.testItems.add(new DisplayItem((dc.tilesize * position)- dc.offset, (dc.tilesize * 4)- dc.offset, itemList[i]));
        }
    }

    /*
    Renders the other characters on the screen if they are in the players screen
     */
    private void renderCharacters(Main dc, Graphics g) {
        for (Character ch : dc.characters) {
            if (!ch.equals(dc.hero)) {
                Vector sc = world2screenCoordinates(dc, ch.getWorldCoordinates());
                ch.animate.setPosition(sc);
                if (characterInRegion(dc, ch)) {
                    ch.animate.render(g);
                }
            }
        }
    }


    /*
    Renders the  AI on the screen if they are in the players screen
     */
    private void renderEnemies(Main dc, Graphics g) {
        for (Character ai : dc.enemies) {
            if (ai.getHitPoints() <= 0) {
                continue;
            }
            Vector sc = world2screenCoordinates(dc, ai.getWorldCoordinates());
            ai.animate.setPosition(sc);
            if (characterInRegion(dc, ai)) {
                ai.animate.render(g);
            }
        }
    }
    
    private class ItemBox{
    	private String title;
    	private int width;
    	private int height;
    	private int x;
    	private boolean visible;
    	
    	public ItemBox(String title, int width, int height){
    		this.title = title;
    		this.width = width;
    		this.height = height;
    	}
    	
    	public void setX(int x){
    		this.x = x;
    	}
    	
    	public void render(Graphics g, Main dc){
            Color tmp = g.getColor();
            g.setColor(new Color(0, 0, 0, 0.5f));

            //create a rectangle that can fit all the player's items with 4 items per row
            //System.out.println("Drawing " + title + " at " + x + ", " + y);
            g.fillRoundRect(x*dc.tilesize, dc.tilesize, width*dc.tilesize, height*dc.tilesize, 0);

            g.setColor(Color.white);
            g.drawString(title, x*dc.tilesize + 10, dc.tilesize + 10);
            g.setColor(tmp);
    	}
    	
    	/**
    	 * Add an image to this ItemBox
    	 * 
    	 * @param x - x position of image relative to item box
    	 * @param y - y position of image relative to item box
    	 */
    	public void addImage(Graphics g, Main dc, Image img, int x, int y){
    		g.drawImage(img, (x + this.x)*dc.tilesize, (y+1)*dc.tilesize);
    	}
    	
    	
    	/**
    	 * Add a string to this ItemBox
    	 * 
    	 * @param x - x position of string relative to item box
    	 * @param y - y position of string relative to item box
    	 */
    	public void addString(Graphics g, Main dc, String text, int x, int y){
    		g.drawString(text, (x + this.x)*dc.tilesize, (y+1)*dc.tilesize);
    	}
    	
    	public void addStringFineGrained(Graphics g, Main dc, String text, float screenX, float screenY){
    		//add a string using screen coordinates instead of tile coordinates
    		g.drawString(text, screenX + (this.x*dc.tilesize), screenY + dc.tilesize);
    	}
    	

    }
    
    private ArrayList<ItemBox> itemBoxes = new ArrayList<ItemBox>();
    
    private void renderItemBoxes(Graphics g, Main dc){
    	//render all the item boxes that need to be displayed
    	
    	int nextX = 1;
    	
    	for( ItemBox ib : itemBoxes ){
    		//get the correct x value based on open item boxes
    		if( ib.visible ){
    			ib.setX(nextX);
    			nextX += ib.width;
    			ib.render(g, dc);
    		}
    	}
    }
    
    private ItemBox getItemBoxByTitle(String title){
    	for( ItemBox ib : itemBoxes ){
    		if( ib.title.equals(title) ){
    			return ib;
    		}
    	}
    	return null;
    }
    


    private Scanner scan = new Scanner(System.in);

    private String prevks = "";

    private Vector vectorFromKeystroke(String ks){
        if( ks.equals("w") ){
            return new Vector(0, -1);
        }else if( ks.equals("s") ){
            return new Vector(0, 1);
        }else if( ks.equals("a") ){
            return new Vector(-1, 0);
        }else if( ks.equals("d") ){
            return new Vector(1, 0);
        }else{
            return new Vector(0, -1);
        }
    }
    
    
    private int songChangeTimer = 3000;
    

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Main dc = (Main) game;
        Cheats.enableCheats(dc, input);
        pause(input);
        if (paused) {
            return;
        }
        
        if( input.isKeyPressed(Input.KEY_RBRACKET) ){
        	//change music in 3 seconds
        	currentTrack.stop();
        }
        
        //pick new music if the current track has expired 3 seconds ago
        if( !currentTrack.playing() ){
        	//decrement timer
        	songChangeTimer -= delta;
        	
        	//change the song if the timer has expired
        	if( songChangeTimer <= 0 ){
        		songChangeTimer = 3000;
        		currentTrack = tracks[ rand.nextInt(tracks.length) ];
        		currentTrack.play(1, 0.3f);
        	}
        }
        
        
        //decrease attack timer
        if( attackCooldown > 0 ){
        	attackCooldown -= delta;
        }
        
        //regenerate some mana if this client is a mage
        if( dc.hero.getType().toLowerCase().contains("mage") && dc.hero.getMana() < dc.hero.getMaxMana() ){
        	dc.hero.setMana(dc.hero.getMana() + 0.05f);
        	if( dc.hero.getMana() > dc.hero.getMaxMana() ){
        		dc.hero.setMana(dc.hero.getMaxMana());
        	}
        }
        
        //draw visual effects
        for( Character ch : targets ){
        	
        	//TODO: this should be done on the server,
        	// not the client
        	//(client should handle its own effects)
        	ch.implementEffects();
        	
        	
        	ArrayList<String> removedEffects = new ArrayList<String>();
        	
        	//check if the character is now dead (killed by status effect)
        	if( ch.getHitPoints() <= 0 ){
        		//if dead, remove all effects
        		for( Effect e : ch.getActiveEffects() ){
        			removedEffects.add(e.name);
        		}
        		//clear active effects
        		ch.clearActiveEffects();
        	}else{
        		//if not dead update timers and remove effects
        		//  with expired timers
        		removedEffects = ch.updateEffectTimers(16);
        	}
        	
	        //remove visual effects corresponding to removed effects
        	if( ch.vfx != null ){
	        	for( String st : removedEffects ){
	        		ch.vfx.updateVisualEffectTimer(st, 0, false);
	        	}
        	}
        	
        	//add any new visual effects
        	ch.addVisualEffects();
        	//and update timers
        	ch.updateVisualEffectTimers();
        	
        	
        	//follow the character
        	if( ch.vfx != null ){
        		ch.vfx.setPosition(ch.animate.getPosition());
        	}
        }

        String ks = getKeystroke(input, dc);
        dc.hero.move(ks);
        //positionToServer(dc);  // Get the player's updated position onto the server.
        updateOtherPlayers(dc);

        sendEnemyStatusToServer(dc);
        readEnemyStatusFromServer(dc);
        readWeightsFromServer(dc);



        //cheat code to apply any effect to the character
        if( input.isKeyPressed(Input.KEY_LALT) ){
            System.out.println("Game window frozen, expecting user input.");
            System.out.println("Enter valid effect name (e.g. Healing) ");

            String effect = scan.next().trim();

            if( effect.equals("Iron") ){
                effect = "Iron Skin";
            }
            System.out.println("Got effect '"+effect+"'");

            dc.hero.addEffect(effect,false);
        }


        if( input.isKeyPressed(Input.KEY_I) ){
        	SFXManager.playSound("open_inventory");
            
            ItemBox ib = getItemBoxByTitle("Inventory");
            if( ib != null ){
            	ib.visible = !ib.visible;
            }
        }
        if( input.isKeyPressed(Input.KEY_O) ){
        	SFXManager.playSound("open_inventory");

            ItemBox ib = getItemBoxByTitle("Codex");
            if( ib != null ){
            	ib.visible = !ib.visible;
            }
        }
        if( input.isKeyPressed(Input.KEY_U) ){
        	SFXManager.playSound("open_inventory");

        	ItemBox ib = getItemBoxByTitle("Character");
        	if( ib != null ){
        		ib.visible = !ib.visible;
        	}
        }

        ItemBox ib = getItemBoxByTitle("Inventory");
        if( ib != null && ib.visible ){
            if( input.isKeyPressed(Input.KEY_UP) ){
                //selectedItem.setY(selectedItem.getY()-1);
                itemy--;
            }else if( input.isKeyPressed(Input.KEY_DOWN) ){
                //selectedItem.setY(selectedItem.getY()+1);
                itemy++;
            }else if( input.isKeyPressed(Input.KEY_LEFT) ){
                //selectedItem.setX(selectedItem.getX()-1);
                itemx--;
            }else if( input.isKeyPressed(Input.KEY_RIGHT) ){
                //selectedItem.setX(selectedItem.getX()+1);
                itemx++;
            }else if( input.isKeyPressed(Input.KEY_ENTER) ){
                System.out.println("Equipping "+((itemy*4)+itemx)+"...");
                //dc.hero.equipItem( dc.hero.getInventory().get((itemy*4)+itemx).getID(), 0);
                try{
                    String m = dc.hero.equipItem((itemy*4)+itemx);
                    itemx = 0;
                    itemy = 0;
                    if( m != null ){
                        addMessage(m);
                    }
                }catch(IndexOutOfBoundsException ex){
                    System.out.println("Out of bounds.");
                }
            }


            if( itemx < 0 ){
                itemx = 0;
            }
            if( itemy < 0 ){
                itemy = 0;
            }
            if( itemx > 3 ){
                itemx = 3;
            }
            if( itemy > dc.hero.getInventory().size()/4){
                itemy = dc.hero.getInventory().size()/4;
            }

            //System.out.println(itemx+", "+itemy);
        }else{
            if( input.isKeyPressed(Input.KEY_LEFT) ){
                selectedEquippedItem--;
            }else if( input.isKeyPressed(Input.KEY_RIGHT) ){
                selectedEquippedItem++;
            }else if( input.isKeyPressed(Input.KEY_ENTER) ){
                //attack with item
                //System.out.println("Attacking with " + dc.hero.getEquipped()[selectedEquippedItem].getType() );
            	
            	Item itm = dc.hero.getEquipped()[selectedEquippedItem];
            	if( attackCooldown <= 0 ){
	            	if( itm == null || canUse(itm, dc.hero) ){
	            		attack(itm, dc);
	            		if( itm != null && !itm.isIdentified() ){
		            		itm.identify();
		            		System.out.println("Identified " + itm.toString());
		            		addMessage("It is " + itm.toString());
		            		if( itm.isCursed() ){
		            			SFXManager.playSound("curse");
		            		}else{
		            			SFXManager.playSound("identify");
		            		}
	            		}
	            	}
            		
	            	//update attack cooldown based on item weight
            		if( itm == null ){
            			attackCooldown = 1000; 
            		}else{
            			//max attack item weight is 60, max cooldown time should be one second
            			attackCooldown = itm.getWeight()*17;
            		}
            	}

                //itm = dc.hero.getEquipped()[selectedEquippedItem];
                if( attackCooldown <= 0 ){
                    if( dc.hero.getType().toLowerCase().contains("knight") ||
                            dc.hero.getType().toLowerCase().contains("tank") ){
                        attack(null, dc);
                    }else if( canUse(itm, dc.hero) ){
                        attack(dc.hero.getEquipped()[selectedEquippedItem], dc);
                    }

                    //update attack cooldown based on item weight
                    if( itm == null ){
                        attackCooldown = 1000;
                    }else{
                        //max attack item weight is 60, max cooldown time should be one second
                        attackCooldown = itm.getWeight()*17;
                    }
                }
            }else if( input.isKeyPressed(Input.KEY_APOSTROPHE) ){
                //use item on own character
                Item i = dc.hero.getEquipped()[selectedEquippedItem];
                if( i != null ){
                	
                	if( !i.isIdentified() ){
                		i.identify();
                		addMessage("It is " + i.toString());
                	}
	                if( canUse(i, dc.hero) ){
		                String x = "";
		                if( i.getType().equals("Potion") ){
		                    x = "Drank";
		                    SFXManager.playSound("potion_drink");
		                }else if( i.getType().equals("Armor") ){
		                    x = "Put on";
		                    SFXManager.playSound("equip_armor");
		                }else{
		                    x = "Used";
		                }
		                addMessage(x + " " + i.toString());
		                
		                if( i.getType().equals("Potion") || i.getType().equals("Armor") ){
		                	//add effect to character
		                    dc.hero.addEffect(i.getEffect(),false);
		                    addMessage("You are now affected by " + i.getEffect().toLowerCase());
		                }
		
		                //remove the item from hands
		                dc.hero.unequipItem(selectedEquippedItem);
		
		                //only remove the item from the inventory if it is a potion/consumable
		                //armor should remain in the player's inventory
		                if( i.getType().equals("Potion") ){
		                    dc.hero.discardItem(i.getID(), true);
		                }else if( i.getType().equals("Armor") ){
		                    i.identify();
		
		                    //set the hero type to change the armor
		                    if( dc.hero.getType().contains("knight") ){
		                        dc.hero.setType("knight_"+i.getMaterial().toLowerCase());
		                    }else if( dc.hero.getType().contains("tank") ){
		                        dc.hero.setType("tank_"+i.getMaterial().toLowerCase());
		                    }
		                }
	                }
                }

            }else if( input.isKeyPressed(Input.KEY_BACKSLASH) ){


                Item itm = dc.hero.getEquipped()[selectedEquippedItem];
                if( itm != null ){
	                if( itm.getType().equals("Arrow") ){
	                	//reset the item count
	                	itm.count = 1;
	                	
	                	itm.setImage(getArrowImage(itm.getEffect(), null));
	                }
	                dc.hero.unequipItem(selectedEquippedItem);
	
	
	
	                //place the item at the hero's feet
	                Vector wc = new Vector(
	                		(int)(dc.hero.getWorldCoordinates().getX()/dc.tilesize),
	                		(int)(dc.hero.getWorldCoordinates().getY()/dc.tilesize)+1
	                		);
	                Main.im.take(itm, dc.hero, wc, false);
	                
	                //play drop sound
	                SFXManager.playSound("item_drop");
	                
	                //reduce the hero's inventory weight
	                
	                //lock the item with a timer
	                itm.lock();
	                //add item lock timer
	                itemLockTimers.add( new ItemLockTimer(itm.getID()) );
	                System.out.println("Locked dropped item.");
	
	                //add the item to the render list
	                // TODO may need to be changed to be added to worldItems
	                itemsToRender.add(itm);
	
	                addMessage("Dropped "+itm.toString()+".");
                }
            }else if( input.isKeyPressed(Input.KEY_RSHIFT) ){
                //return item to inventory
                dc.hero.unequipItem(selectedEquippedItem);
            }

            if( selectedEquippedItem < 0 ){
                selectedEquippedItem = 0;
            }
            if( selectedEquippedItem > dc.hero.getEquipped().length-1 ){
                selectedEquippedItem = dc.hero.getEquipped().length-1;
            }
        }

        //remove dead characters
        for( Character ch : dc.characters ){
        	if( ch.getHitPoints() <= 0 ){
        		if( ch.vfx != null ){
	        		for( Effect e : ch.getActiveEffects() ){
	        			ch.vfx.updateVisualEffectTimer(e.name, 0, false);
	        		}
	        		ch.vfx = null;
	        		
	        		//remove all active effects from the 
	        		ch.clearActiveEffects();
        		}
        	}
        }
        
        
        dc.characters.removeIf(b -> b.getHitPoints() <= 0);
        
        
        // if the hero has no health, then replace it with a new hero character in the same spot
        if(dc.hero.getHitPoints() <= 0){
            dc.hero = new Character(dc,dc.hero.animate.getX(),dc.hero.animate.getY(),dc.hero.getType(),
                    dc.serverId,this,false);
            dc.characters.add(0,dc.hero);
        }


        // cause AI players to move around
        for( Character ch : dc.enemies ) {
            if (ch.ai) {        // if the player is an AI player, move them
                ch.moveAI(delta);
            }
        }

        //advance any thrown items along their path
        ArrayList<ThrownItem> reachedDestination = new ArrayList<ThrownItem>();
        for( ThrownItem ti : thrownItems ){

            //check if a thrown item went off the screen
            if( ti.itm.getWorldCoordinates().getX() < 0 + dc.hero.getOrigin().getX() || ti.itm.getWorldCoordinates().getX() > dc.hero.getOrigin().getX() + (dc.ScreenWidth/dc.tilesize)){
                reachedDestination.add(ti);
                addMessage("thrown " + ti.itm.getType() + " went off screen");
            }else if( ti.itm.getWorldCoordinates().getY() < 0 + dc.hero.getOrigin().getY() || ti.itm.getWorldCoordinates().getY() > dc.hero.getOrigin().getY() + (dc.ScreenHeight/dc.tilesize)){
                reachedDestination.add(ti);
                addMessage("thrown " + ti.itm.getType() + " went off screen");
            }
            if( throwItem(ti, dc) ){
                reachedDestination.add(ti);
                addMessage("thrown " + ti.itm.getType() + " reached destination");
            }

            //check if a thrown item hit a character
            //merge the character and enemy lists
            ArrayList<Character> targets = new ArrayList<Character>();
            targets.addAll(dc.characters);
            targets.addAll(dc.enemies);
            for( Character ch : targets ){

                if( ch.getPid() == dc.hero.getPid() ){
                    continue;
                }

                //Vector chwc = new Vector( ch.getTileWorldCoordinates().getX()/dc.tilesize, ch.getTileWorldCoordinates().getY()/dc.tilesize);
                float x = ch.getTileWorldCoordinates().getX();
                float y = ch.getTileWorldCoordinates().getY();
                Vector chwc = new Vector((int)x, (int)y);
                //System.out.println(chwc.toString());

                if( Math.abs(chwc.getX() - ti.itm.getWorldCoordinates().getX()) < 1.5 && Math.abs(chwc.getY() - ti.itm.getWorldCoordinates().getY()) < 1.5 ){
                    //addMessage("thrown " + ti.itm.getType() + " hit enemy");

                    //potions do no damage but cause status effects on the target
                    if( ti.itm.getType().equals("Potion") ){
                        ch.takeDamage(0, ti.itm.getEffect(),false);
                        dc.hero.addToCodex(ti.itm);
                        reachedDestination.add(ti);
                    }else if( ti.itm.getType().equals("Arrow") || ti.itm.getType().equals("Staff") ){
                        //roll random damage, similarly to a sword
                        rand.setSeed(System.nanoTime());
                        int r = rand.nextInt(100);

                        float damagePercent = r/(float) 100;

                        String m;
                        if( damagePercent == 0 ){
                            m = "Missed.";
                        }else{
                            if( ch.takeDamage(10*damagePercent, ti.itm.getEffect(),false) ){
                                //set character action to die
                                ch.updateAnimation("die");
                                
                                //ch.vfx = null;
                            }

                            m = "Hit enemy for " + (int) (10*damagePercent) + " damage.";
                            if( damagePercent >= 0.8 ){
                                m = m + " Critical hit!";
                            }
                        }
                        addMessage(m);
                        if( ti.itm.getType().equals("Arrow") ){
                        	reachedDestination.add(ti);
                        }
                    }
                }
            }

            //check if an item hit a wall tile
            if( rotatedMap[(int) ti.itm.getWorldCoordinates().getX()][(int) ti.itm.getWorldCoordinates().getY()] == 1 ){
                addMessage("thrown " + ti.itm.getType() + " hit wall");

                reachedDestination.add(ti);
            }
        }
        //remove any that have reached their destination
        thrownItems.removeAll(reachedDestination);
        //itemsToRender.removeAll(reachedDestination);
        for( ThrownItem ti : reachedDestination ){
        	if( ti.itm.getType().equals("Potion") ){
        		SFXManager.playSound("potion_break");
        	}else{
        		SFXManager.playSound("wall_hit");
        	}
            Main.im.removeFromWorldItems(ti.itm);
        }


        //check if a character has hit an item
        for( Character ch : dc.characters ){
            if( ch.ai ){
                continue;
            }
            float x = ch.getTileWorldCoordinates().getX();
            float y = ch.getTileWorldCoordinates().getY();
            Vector aniPos = new Vector((int)x, (int)y);

            Item i = Main.im.getItemAt(aniPos);
            if( i != null && !i.isLocked() ){
            	if( ch.getInventoryWeight() >= ch.getMaxInventoryWeight() ){
            		addMessage("You cannot carry any more.");
            	}else{
	            		
	            	
	                if( i.isIdentified() ){
	                    addMessage("Picked up " + i.toString() + ".");
	                }else{
	                    addMessage("Picked up unidentified "+i.toString()+".");
	                }
	                
            
	                
	                
	
	                
	                if( i.getType().equals("Arrow") ){
	                	Image image = null;
	                	if( i.getEffect().equals("Poison") ){
	                		image = ResourceManager.getImage(Main.ARROW_POISON_UP);
	                	}else if( i.getEffect().equals("Flame") ){
	                		image = ResourceManager.getImage(Main.ARROW_FLAME_UP);
	                	}else if( i.getEffect().equals("Ice") ){
	                		image = ResourceManager.getImage(Main.ARROW_ICE_UP);
	                	}else if( i.getEffect().equals("") ){
	                		image = ResourceManager.getImage(Main.ARROW_NORMAL_UP);
	                	}
	                	if( image == null){
	                		throw new SlickException("Invalid arrow effect.");
	                	}
	                	
	                	//give the character 5 arrows
	                	Item arrow = new Item(i.getWorldCoordinates(), i.isLocked(), i.getID(), ch.getPid(), i.getEffect(), "Arrow", i.getMaterial(), 
	                			i.isCursed(), i.isIdentified(), image, 5);
	                	Main.im.give(arrow, ch);
	                	i.lock();
	                }else{
	                    //give removes item from the world's inventory
	                    //  and adds it to the player's inventory
	                    Main.im.give(i, ch);
	                }
	                
	                //check if player is weighed down
	                if( ch.getInventoryWeight() >= ch.getMaxInventoryWeight()*0.7 ){
                    	addMessage("You are encumbered.");
                    }
	
	                
	                SFXManager.playSound("item_pickup");
	                //stop rendering the item
	                itemsToRender.remove(i);
	                Main.im.removeFromWorldItems(i);
	            }
            }
        }



        //update message timers
        for( int i = 0; i < messagebox.length; i++ ){
            if( messagebox[i] == null ){
                break;
            }
            if( messagebox[i].timer <= 0 ){
                messagebox[i] = null;
            }else{
                messagebox[i].timer -= delta;
            }
        }

        //update item lock timers if applicable
        for( ItemLockTimer t : itemLockTimers ){
            t.timer -= delta;
            if( t.timer <= 0 ){
                Item itm = Main.im.getWorldItemByID(t.itemID);
                if( itm != null ){
                    itm.unlock();
                }else{
                    //if this item is null there is a problem
                    throw new SlickException("Attempted to unlock a null item.");
                }
            }
        }
        //remove expired timers
        itemLockTimers.removeIf(b -> b.timer <= 0);
    }
    private Image getSpellImage(String material) throws SlickException{
    	if( material.equals("Ruby") ){
    		return ResourceManager.getImage(Main.SPELL_RED);
    	}else if( material.equals("Emerald") ){
    		return ResourceManager.getImage(Main.SPELL_GREEN);
    	}else if( material.equals("Amethyst") ){
    		return ResourceManager.getImage(Main.SPELL_PURPLE);
    	}else{
    		throw new SlickException("Invalid staff material '" + material + "'.");
    	}
    }
    
    private Vector dirStringToVector(String dir) throws SlickException{
    	if( dir.equals("up") ){
    		return new Vector(0, -1);
    	}else if( dir.equals("down") ){
    		return new Vector(0, 1);
    	}else if( dir.equals("left") ){
    		return new Vector(-1, 0);
    	}else if( dir.equals("right") ){
    		return new Vector(1, 0);
    	}else{
    		throw new SlickException("Invalid directional string.");
    	}
    }

    private void attack(Item itm, Main dc) throws SlickException{
        //attack with the given item
    	String direction = dc.hero.direction.split("_")[1];
    	
    	Vector directionVector = dirStringToVector(direction);
        
        System.out.println("Attacking in the '" + direction + "' direction");

        if( itm == null || itm.getType().equals("Sword") || itm.getType().equals("Gloves")){
        	//play the attack sound
        	if( itm == null ){
        		if( dc.hero.getType().toLowerCase().contains("knight") ){
        			SFXManager.playSound("knight_punch");
        		}else if( dc.hero.getType().toLowerCase().contains("tank") ){
        			SFXManager.playSound("tank_punch");
        		}else{
        			//if the class isn't knight or tank, it cannot punch
        			return;
        		}
        	}else if( itm.getType().equals("Sword") ){
        	
        		SFXManager.playSound("sword_swing");
        	}else if( itm.getType().equals("Gloves") ){
        		SFXManager.playSound("tank_punch");
        	}
            rand.setSeed(System.nanoTime());
            int r = rand.nextInt(100);
            if( r < 50 ){
                //slash
                dc.hero.updateAnimation("slash_" + dc.hero.direction.split("_")[1]);
                //addMessage("Slashed " + dir );
            }else{
                //jab
                dc.hero.updateAnimation("jab_" + dc.hero.direction.split("_")[1] );
                //addMessage("Jabbed " + dir );
            }

            for( Character c : dc.characters ){
                if( c.ai ){
                    //if the ai character is within one tilesize of the player
                    //in the given direction
                    Vector aipos = c.getWorldCoordinates();
                    Vector plpos = dc.hero.getWorldCoordinates();

                    double x = Math.pow(aipos.getX()-plpos.getX(), 2);
                    double y = Math.pow(aipos.getY()-plpos.getY(), 2);
                    float distance = (float) Math.sqrt(x + y);

                    if(  distance <= (dc.tilesize*1.5) ){
                        //roll damage amount
                        rand.setSeed(System.nanoTime());
                        float percentOfMaxDamage = rand.nextInt(100)/(float) 100;

                        float damage = 0;
                        if( itm == null ){
                        	//max damage: 10
                        	damage = 10 * percentOfMaxDamage;
                            
                        	//copy this block here instead of doing a bunch of null checks
                        	if( c.takeDamage(damage, "",false) ){
                                //returns true if the enemy died
                                c.updateAnimation("die");
                                
                                //c.vfx = null;

                            }

                            if( percentOfMaxDamage == 0 ){
                                addMessage("Missed.");
                            }else{
                                String m = "Hit enemy for " + (int) damage + " damage.";
                                if( percentOfMaxDamage >= 0.8 ){
                                    m = m + " Critical hit!";
                                }
                                addMessage(m);
                            }
                            return;
                        }else if( itm.getMaterial().equals("Wooden") ){
                            //max damage: 30
                            damage = 30 * percentOfMaxDamage;
                        }else if( itm.getMaterial().equals("Iron") ){
                            //max damage: 60
                            damage = 60 * percentOfMaxDamage;
                        }else if( itm.getMaterial().equals("Gold") ){
                            //max damage: 100
                            damage = 100 * percentOfMaxDamage;
                        }

                        //pass damage and effect to enemy
                        
                        if( itm.getEffect().equals("Might") ){
                        	damage *= 2;
                        }

                        if( c.takeDamage(damage, itm.getEffect(),false) ){
                            //returns true if the enemy died
                            c.updateAnimation("die");
                            
                            //destroy the character's vfx object
                            //c.vfx = null;

                        }

                        if( percentOfMaxDamage == 0 ){
                            addMessage("Missed.");
                        }else{
                            String m = "Hit enemy for " + (int) damage + " damage.";
                            if( percentOfMaxDamage >= 0.8 ){
                                m = m + " Critical hit!";
                            }
                            addMessage(m);
                        }

                        //reveal the effect to the character
                        // if it is not known
                        if( !itm.isIdentified() ){
                            addMessage("It is " + itm.toString() );
                            itm.identify();
                        }
                    }
                }
            }
            dc.characters.removeIf(b -> b.getHitPoints() <= 0);

        }else if( itm.getType().equals("Potion") ){
        	SFXManager.playSound("potion_throw");
        	addThrownItem(dc, itm, itm.getImage(), directionVector, directionVector.scale(5), directionVector.scale(0.1f));
        	
            if( itm.count == 1 ){
            	dc.hero.unequipItem(selectedEquippedItem);
            }
            dc.hero.discardItem(itm, true);

        }else if( itm.getType().equals("Staff") ){
        	SFXManager.playSound("launch_spell");
        	int manaCost = 0;
        	if( itm.getMaterial().equals("Ruby") ){
        		//high mana cost
        		manaCost = 20;
        	}else if( itm.getMaterial().equals("Emerald") ){
        		//medium mana cost
        		manaCost = 15;
        	}else if( itm.getMaterial().equals("Amethyst") ){
        		//low mana cost
        		manaCost = 5;
        	}else{
        		throw new SlickException("Invalid staff material '" + itm.getMaterial() + "'.");
        	}
        	
        	
        	if( dc.hero.getMana() < manaCost ){
        		addMessage("You don't have enough mana to use this.");
        		return;
        	}
        	
        	Image spellImage = getSpellImage(itm.getMaterial());
        	
        	addThrownItem(dc, itm, spellImage, directionVector, directionVector.scale(10000), directionVector.scale(0.2f));
        	
        	//decrease mana
        	dc.hero.setMana(dc.hero.getMana() - manaCost);
        	
        }else if( itm.getType().equals("Arrow") ){
            //Item(Vector wc, boolean locked, int id, int oid, String effect, String type, String material, boolean cursed, boolean identified, Image image)

        	System.out.println("throwing arrow " + dc.hero.direction.split("_")[1] );
        	
        	SFXManager.playSound("shoot_arrow");
        	
        	Image image = getArrowImage(itm.getEffect(), dc.hero.direction.split("_")[1]);

        	addThrownItem(dc, itm, image, directionVector, directionVector.scale(10000), directionVector.scale(0.3f));
            
            if( itm.count == 1 ){
            	dc.hero.unequipItem(selectedEquippedItem);
            }
            dc.hero.discardItem(itm, true);
        }
    }
    
    private void addThrownItem(Main dc, Item emitter, Image image, Vector direction, Vector destination, Vector step) throws SlickException{
    	//Spawn item at the player's position
        Vector wc = new Vector((dc.hero.getWorldCoordinates().getX()/dc.tilesize)-0.5f, 
        		(dc.hero.getWorldCoordinates().getY()/dc.tilesize));
        
        //the actual item that will be thrown
        Item emissive = new Item(wc, true, -1, -1, emitter.getEffect(), emitter.getType(), "", false, true, image, 1);
        
        //add the emissive to the world items so it can be rendered
        Main.im.addToWorldItems(emissive);
        
        //create the thrown item
        thrownItems.add(new ThrownItem(emissive, direction, destination, step));
    }
    
    
    private boolean canUse(Item i, Character hero){
    	//return true if the hero can use an item
    	//  based on required level and class
    	//return false otherwise
    	
    	//check item level
    	if( i.getRequiredLevel() > hero.getStrength() ){
    		addMessage("You are not strong enough to use this.");
    		return false;
    	}
    	
    	//check class
    	String type = hero.getType().substring(0, hero.getType().indexOf("_")).toLowerCase();
    	for( String st : i.getRequiredClasses() ){
    		if( st.equals(type) ){
    			return true;
    		}
    	}
    	addMessage("This item is for a different class.");
    	return false;
    }

    private boolean throwItem(ThrownItem ti, Main dc){
        //if the item is not at the final location

        //these need to be floored because otherwise the item will go past its destination due to
        //  a small decimal difference
        Vector flooredWC = new Vector( (int) ti.itm.getWorldCoordinates().getX(), (int) ti.itm.getWorldCoordinates().getY() );
        Vector flooredDest = new Vector( (int) ti.finalLocation.getX(), (int) ti.finalLocation.getY() );

        if(  !flooredWC.equals(flooredDest) ){
            ti.itm.setWorldCoordinates(ti.itm.getWorldCoordinates().add(ti.step));
            return false;
        }

        return true;
    }

    private float currentTrackPosition;

    // pause the game
    public void pause(Input input) {
        if (input.isKeyPressed(Input.KEY_P)) {
            paused = !paused;
            if( currentTrack.playing() ){
            	currentTrackPosition = currentTrack.getPosition();
            	currentTrack.stop();
            }else{
            	currentTrack.play(1, 0.3f);
            	currentTrack.setPosition(currentTrackPosition);
            }
            
            
        }
        
        
    }


    /*
    get the key being pressed, returns a string
     */
    public String getKeystroke(Input input, Main dc) {
        String ks = "";
        if (input.isKeyDown(Input.KEY_W)) {
            ks = "w";
        }
        else if (input.isKeyDown(Input.KEY_S)) {
            ks = "s";
        }
        else if (input.isKeyDown(Input.KEY_A)) {
            ks = "a";
        }
        else if (input.isKeyDown(Input.KEY_D)) {
            ks = "d";
        }
        // cheat speed up
        else if (input.isKeyPressed(Input.KEY_4)) {
            ks = "4";
        }
        // cheat slow down
        else if (input.isKeyPressed(Input.KEY_5)) {
            ks = "5";
        }
        try{
            Msg message = new Msg(dc.serverId,dc.hero.getType(),dc.hero.getWorldCoordinates().getX(),
                    dc.hero.getWorldCoordinates().getY(),dc.hero.getHitPoints());
            message.ks = ks;
            outStream.writeObject(message);
            //System.out.println("wrote message of "+message.getClass().getSimpleName());
            outStream.flush();
            outStream.reset();
        }catch(IOException e){
            e.printStackTrace();
        }
        return ks;
    }


    /**
     * sends information about the enemies to the server
     * @param dc
     */
    public void sendEnemyStatusToServer(Main dc) {
//        System.out.println("sendEnemyStatusToServer()");
        Msg msg;
        float wx;
        float wy;
        for (Character ai : dc.enemies) {
            wx = ai.getWorldCoordinates().getX();
            wy = ai.getWorldCoordinates().getY();
            msg = new Msg(ai.getCharacterID(), ai.getType(), wx, wy, ai.getHitPoints());
            try {
                outStream.writeObject(msg);
                outStream.flush();
                outStream.reset();
//                System.out.println("writing " + msg.toString());
            }catch(IOException e){
                e.printStackTrace();
            }
        }
//        // System.out.println();
    }

    /*
read the information about the AI from the server
 */
    private void readEnemyStatusFromServer(Main dc) {
//        System.out.println("readEnemyStatusFromServer()");
        for (Character ai : dc.enemies) {
            try {
                Msg msg = (Msg) inStream.readObject();
//                System.out.println("reading " + msg.toString());
                if (ai.canMove) {
                    ai.setWorldCoordinates(msg.wx, msg.wy);
                }
                ai.setHitPoints(msg.hp);
                ai.next = msg.nextDirection;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        // System.out.println();
    }

    /*
    Read the weights from dijkstra from the server
     */
    private void readWeightsFromServer(Main dc) {
        try {
            Msg msg = (Msg) inStream.readObject();
            dc.hero.weights = msg.dijkstraWeights;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
      * Update the players position on the server.
      */
//    public void positionToServer(Main dc){
//        float wx = dc.hero.getWorldCoordinates().getX();
//        float wy = dc.hero.getWorldCoordinates().getY();
//        Msg toServer = new Msg(dc.serverId,dc.hero.getType(),wx,wy,dc.hero.getHitPoints());
//        getEffectsForMsg(dc, toServer);
//        try {
//            outStream.writeObject(toServer);
//            outStream.flush();
//            outStream.reset();
////            System.out.println("writing "+ toServer.toString());
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }
//    public void positionToServer(Main dc){
//        float wx = dc.hero.getWorldCoordinates().getX();
//        float wy = dc.hero.getWorldCoordinates().getY();
//        Msg toServer = new Msg(dc.serverId,dc.hero.getType(),wx,wy,dc.hero.getHitPoints());
//        getEffectsForMsg(dc, toServer);
//        try {
//            outStream.writeObject(toServer);
//            outStream.flush();
//            outStream.reset();
////            System.out.println("writing "+ toServer.toString());
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }
//    public void positionToServer(Main dc){
//        float wx = dc.hero.getWorldCoordinates().getX();
//        float wy = dc.hero.getWorldCoordinates().getY();
//        Msg toServer = new Msg(dc.serverId,dc.hero.getType(),wx,wy,dc.hero.getHitPoints());
//        getEffectsForMsg(dc, toServer);
//        try {
//            outStream.writeObject(toServer);
//            outStream.flush();
//            outStream.reset();
////            System.out.println("writing "+ toServer.toString());
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }
//    public void positionToServer(Main dc){
//        float wx = dc.hero.getWorldCoordinates().getX();
//        float wy = dc.hero.getWorldCoordinates().getY();
//        Msg toServer = new Msg(dc.serverId,dc.hero.getType(),wx,wy,dc.hero.getHitPoints());
//        getEffectsForMsg(dc, toServer);
//        try {
//            outStream.writeObject(toServer);
//            outStream.flush();
//            outStream.reset();
////            System.out.println("writing "+ toServer.toString());
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }


    /**
     * enable the effects for the server message
     * @param dc
     * @param msg to be sent to the server
     */
    private void getEffectsForMsg(Main dc, Msg msg) {
        if (dc.hero.isInvisible()) {
            msg.invisible = true;
        }
        if (dc.hero.isStinky()) {
            msg.stinky = true;
        }
        if (dc.hero.isThorny()) {
            msg.thorny = true;
        }
        if (dc.hero.isFrightening()) {
            msg.frightening = true;
        }
        if (dc.hero.isReflecting()) {
            msg.reflecting = true;
        }
        if (dc.hero.isMighty()) {
            msg.mighty = true;
        }
    }


    /*
    Renders the map on screen, only drawing the necessary tiles in view
     */
    public void displayMap(Main dc, Graphics g) {
        for (BaseMap b : dc.maptiles) {
            b.render(g);
        }
    }

    public void updateOtherPlayers(Main dc){
        try {
            Msg read = (Msg) inStream.readObject(); // message from server
//            System.out.println("reading " + read.toString());
//            System.out.println("("+dc.serverId+"): " + read);

            if(read.type.equals("Exit")) {
                dc.characters.removeIf(c -> c.getPid() == read.id);
                return;
            }
            for(Iterator<Character> i = dc.characters.iterator();i.hasNext();){
                Character c = i.next();
                if(c.getPid() == read.id) {
                    if (c.getPid() == dc.serverId) {
                        return;
                    }
                    c.move(read.ks);
                    c.setHitPoints(read.hp);
                    return;
                }

            }
            dc.characters.add(new Character(dc,read.wx,read.wy,read.type,read.id,this,false));
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
    /**
     * Check if the Character is in the Hero's screen +- one tile wide and high
     * @param dc the Main class
     * @param ch the Character instance to check if it is in region
     * @return true if the ch is in screen, else false
     */
    public boolean characterInRegion(Main dc, Character ch){
        float maxX = (dc.tilesize * dc.tilesWide) + dc.hero.pixelX + dc.tilesize;
        float maxY = (dc.tilesize * dc.tilesWide) + dc.hero.pixelY + dc.tilesize;
        float minX = dc.hero.pixelX - dc.tilesize;
        float minY = dc.hero.pixelY - dc.tilesize;
        Vector wc = ch.getWorldCoordinates();
        return minX <= wc.getX() && wc.getX() <= maxX && minY <= wc.getY() && wc.getY() <= maxY;
    }


    /**
     * Check if the object is in the Hero's screen +- one tile wide and high
     * @param dc the Main class
     * @param wc The world coordinate vector of the object
     * @return true if the ch is in screen, else false
     */
    public boolean objectInRegion(Main dc, Vector wc){
        float maxX = (dc.tilesize * dc.tilesWide) + dc.hero.pixelX + dc.tilesize;
        float maxY = (dc.tilesize * dc.tilesWide) + dc.hero.pixelY + dc.tilesize;
        float minX = dc.hero.pixelX - dc.tilesize;
        float minY = dc.hero.pixelY - dc.tilesize;
        return minX <= wc.getX() && wc.getX() <= maxX && minY <= wc.getY() && wc.getY() <= maxY;
    }


    /**
     * Convert the Characters world coordinates to screen coordinates
     * @param dc Main class
     * @param ai an AI character
     */
    public Vector world2screenCoordinates(Main dc, Character ai) {
        // screen coords = AI world coords - Hero's Screen Origin
        float sx = ai.getWorldCoordinates().getX() - dc.hero.pixelX;
        float sy = ai.getWorldCoordinates().getY() - dc.hero.pixelY;
        return new Vector(sx, sy);
    }

    /**
     * Convert the passed in world coordinates to screen coordinates
     * @param dc Main class
     * @param wc worldCoordinates vector of the object to render
     */
    public Vector world2screenCoordinates(Main dc, Vector wc) {
        // screen coords = AI world coords - Hero's Screen Origin
        float sx = wc.getX() - dc.hero.pixelX;
        float sy = wc.getY() - dc.hero.pixelY;
        return new Vector(sx, sy);
    }


}
