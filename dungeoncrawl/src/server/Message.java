package server;

public class Message {
    public int id;
    public String type;
    public float wx;
    public float wy;
    public float hp;

    public Message(int id, String type, float wx, float wy, float hp){
        this.id = id;
        this.type = type;
        this.wx = wx;
        this.wy = wy;
        this.hp = hp;

    }
}
