import jig.Vector;
import java.io.*;
import java.net.*;
import java.util.Iterator;

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
                // User clicks "X"(Windows) or red button (macOS) it will break out of the loop
                // and close socket connections.
                try {
                    inputCode = dis.readUTF();
                    if (inputCode.equals("Exit")) {
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Close all connections
            dos.flush();
            socket.close();
            dos.close();
            dis.close();
            // remove the client from the client ArrayList.
            Server.clients.removeIf(levelServer -> levelServer.getClientId() == this.clientId);
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    /**
     *
     * @return 2d int array for the map.
     */
    public Integer [][]getMap(){
        return map;
    }

    /**
     * This method sends the map to to a client.
     */
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
    public int getClientId(){
        return clientId;
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
