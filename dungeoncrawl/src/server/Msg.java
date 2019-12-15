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
    public int strength;

    public Msg(){
        this.id = 0;
        this.type = "";
        this.wx = 0;
        this.wy = 0;
        this.hp = 0;
        this.ks = "";
        this.strength = 1;
        this.ai = false;
    }
    public Msg(int id, String type, float wx, float wy, float hp, boolean ai, int strength){
        this.id = id;
        this.type = type;
        this.wx = wx;
        this.wy = wy;
        this.hp = hp;
        this.ks = "";
        this.ai = ai;
        this.strength = strength;

    }
    @Override
    public String toString(){
        return("Id = "+id+" type = "+type+" wx = "+wx+" wy = "+wy+" hp = "+hp+" strength= "+strength);
    }
}
