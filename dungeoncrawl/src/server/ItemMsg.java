package server;

import java.io.Serializable;

public class ItemMsg implements Serializable {
    public int id;
    public int oid;
    public String type;
    public float wx;
    public float wy;
    public String effect;
    public String material;
    public String []requiredClasses;
    public boolean cursed;
    public boolean identified;
    public int weight;
    public int count;
    public int requiredLevel;

    public ItemMsg(){
        this.id = 0;
        this.oid = 0;
        this.type = "";
        this.wx = 0;
        this.wy = 0;
        this.effect = "";
        this.material = "";
        this.requiredClasses = new String[4];
        this.cursed = false;
        this.identified = false;
        this.weight = 0;
        this.count = 1;
        this.requiredLevel = 0;
    }

    public ItemMsg(int id, int oid, float wx, float wy, String type, String effect, String material,
                   String [] requiredClasses,boolean cursed,boolean identified, int weight, int count,
                   int requiredLevel){
        this.id = id;
        this.oid = oid;
        this.type = type;
        this.wx = wx;
        this.wy = wy;
        this.effect = effect;
        this.material = material;
        this.requiredClasses = requiredClasses;
        this.cursed = cursed;
        this.identified = identified;
        this.weight = weight;
        this.count = count;
        this.requiredLevel=requiredLevel;
    }

    @Override
    public String toString(){
        return "id= "+id + " oid= " + oid + " wx= "+wx+ " wy= "+wy+" type= "+type+" effect= "+effect+" material= "+
                material;
    }
}
