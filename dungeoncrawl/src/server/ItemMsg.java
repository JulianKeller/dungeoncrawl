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

    public ItemMsg(){
        this.id = 0;
        this.oid = 0;
        this.type = "";
        this.wx = 0;
        this.wy = 0;
        this.effect = "";
        this.material = "";
    }

    public ItemMsg(int id, int oid, float wx, float wy, String type, String effect, String material){
        this.id = id;
        this.oid = oid;
        this.type = type;
        this.wx = wx;
        this.wy = wy;
        this.effect = effect;
        this.material = material;
    }

    @Override
    public String toString(){
        return "id= "+id + " oid= " + oid + " wx= "+wx+ " wy= "+wy+" type= "+type+" effect= "+effect+" material= "+
                material;
    }
}
