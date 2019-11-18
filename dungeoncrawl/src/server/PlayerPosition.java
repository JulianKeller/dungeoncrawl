package server;

public class PlayerPosition {
    private float [] position;
    private int clientId;

    public PlayerPosition(int id){
        position = new float[]{0,0};
        clientId = id;
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
        return position[0] + " " + position[1];
    }

    public int getClientId(){
        return clientId;
    }
    public void setClientId(int id){
        clientId = id;
    }
}
