import jig.Vector;
import java.io.*;
import java.net.*;

/*
 * Server implementation of the level, created by Tyler Higgins
 *
 */
public class LevelServer extends Thread{
    private Integer [][] map;           // Holds the 2d map file
    private Socket socket;             // holds the socket
    private ObjectInputStream dis;     // holds the input stream
    private ObjectOutputStream dos;    // output stream
    private int clientId;              // clientid is based on port number

    /**
     * creates a new map and character
     */
    public LevelServer(Socket socket, ObjectInputStream dis, ObjectOutputStream dos, int id, Integer [][]map){

        this.map = map;
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        clientId = id;

    }

    /**
     * This is what is called when the main server function invokes start().
     */
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

    public int getClientId(){
        return clientId;
    }
}
