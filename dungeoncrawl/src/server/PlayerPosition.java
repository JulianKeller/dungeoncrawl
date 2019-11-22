package server;

public class PlayerPosition {
    private float [] position;
    private int clientId;
    private String type;

    public PlayerPosition(int id, String type, float x, float y){
        position = new float[]{x,y};
        clientId = id;
        this.type = type;
    }

    public void setX(float x){
        position[0] = x;
    }

    public void setY(float y){
        position[1] = y;
    }

    public void setPosition(float x,float y){
        position[0] = x;
        position[1] = y;
    }

    public float getX(){
        return position[0];
    }

    public float getY(){
        return position[1];
    }

    public float [] getPosition(){
        return position;
    }

    public String stringify(){
        return type + " "+ clientId + " " + position[0] + " " + position[1];
    }

    public int getClientId(){
        return clientId;
    }
    public void setClientId(int id){
        clientId = id;
    }
}
