import jig.Vector;
import java.io.*;
import java.net.*;

/*
 * Server implementation of the level, created by Tyler Higgins
 *
 */
public class LevelServer extends Thread{
    // map holds the map, characterCoord holds the characters coordinates.
    private Integer [][] map;
    private Vector characterCoord;
    private Socket socket;
    private ObjectInputStream dis;
    private ObjectOutputStream dos;
    private int clientId;

    /**
     * creates a new map and character
     */
    public LevelServer(Socket socket, ObjectInputStream dis, ObjectOutputStream dos, int id, Integer [][]map){

        this.map = map;
        characterCoord = new Vector(0,0);
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        clientId = id;

    }
    @Override
    public void run(){
        String inputCode;
        sendMap();
        try {
            while (true) {
                try {
                    inputCode = dis.readUTF();
                    if (inputCode.equals("Exit")) {
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            socket.close();
            dos.close();
            dis.close();
        }catch(IOException e){
            e.printStackTrace();
        }

    }
    public Integer [][]getMap(){
        return map;
    }

    public void sendMap(){
        try {
            dos.writeObject(map);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void setCharacterCoord(Vector c){
        characterCoord = c;

    }
    public float characterCoordx(){
        return characterCoord.getX();
    }
    public float characterCoordy(){
        return characterCoord.getY();
    }
    public Vector getCharacterCoord(){
        return characterCoord;
    }
}
