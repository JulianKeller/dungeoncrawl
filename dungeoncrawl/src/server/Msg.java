package server;

import java.io.Serializable;

public class Msg implements Serializable {
    public int id;
    public String type;
    public float wx;
    public float wy;
    public int tilex;
    public int tiley;
    public float hp;
    public String ks;
    public boolean ai;
    public float[][] dijkstraWeights;
    public String nextDirection = "walk_down";      // AI attribute
    public boolean invisible = false;
    public boolean stinky = false;
    public boolean thorny = false;
    public boolean frightening = false;
    public boolean reflecting = false;
    public boolean mighty = false;
    public int[][][] path;
    public boolean canMove = true;
    public String action;

    public Msg(){
        this.id = 0;
        this.type = "";
        this.wx = 0;
        this.wy = 0;
        this.hp = 0;
        this.ks = "";
        this.ai = false;
    }
    
    public Msg(int id, String type, float wx, float wy, float hp, boolean ai){
        this.id = id;
        this.type = type;
        this.wx = wx;
        this.wy = wy;
        this.hp = hp;
        this.ks = "";
        this.ai = ai;

    }


    /**
     *
     * @param msg from the client
     * @param character character on server to save msg to
     */
    public static void saveMsgToCharacter(Msg character, Msg msg) {
        character.id = msg.id;
        character.type = msg.type;
        character.wx = msg.wx;
        character.wy = msg.wy;
        character.tilex = msg.tilex;
        character.tiley = msg.tiley;
        character.hp = msg.hp;
        character.ks = msg.ks;
        character.ai = msg.ai;
        character.dijkstraWeights = msg.dijkstraWeights;
        character.nextDirection = msg.nextDirection;
        character.invisible = msg.invisible;
        character.stinky = msg.stinky;
        character.thorny = msg.thorny;
        character.frightening = msg.frightening;
        character.reflecting = msg.reflecting;
        character.mighty = msg.mighty;
        character.path = msg.path;
        character.canMove = msg.canMove;
        character.action = msg.action;
    }
    
    @Override
    public String toString(){
        return("Id = "+id+" type = "+type+" wx = "+wx+" wy = "+wy+" hp = "+hp+" " +
                " ai = "+ai+" ks = "+ks);
    }
}
