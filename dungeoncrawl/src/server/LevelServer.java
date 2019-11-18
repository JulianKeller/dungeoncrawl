package server;

import jig.Vector;

import javax.swing.text.html.HTMLDocument;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

public class LevelServer extends Thread{
    private Integer [][] map;           // Holds the 2d map file
    private Socket socket;             // holds the socket
    private ObjectInputStream dis;     // holds the input stream
    private ObjectOutputStream dos;    // output stream
    private int clientId;              // clientid is based on port number
    public PlayerPosition position;    // Position of this player.

    /**
     * creates a new map and character
     */
    public LevelServer(Socket socket, ObjectInputStream dis, ObjectOutputStream dos, int id, Integer [][]map){
        this.map = map;
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        clientId = id;
        position = new PlayerPosition(clientId);
    }
    /**
     * This is what is called when the main server function invokes start().
     */
    @Override
    public void run(){
        String inputCode;
        String toSend = "";
        sendMap();
        getPlayerCoord();
        try {
            while (true) {
                // User clicks "X"(Windows) or red button (macOS) it will break out of the loop
                // and close socket connections.
                try {
                    inputCode = dis.readUTF();
                    //System.out.println("Client "+clientId+" sent "+inputCode);
                    if (inputCode.equals("Exit")) {
                        break;
                    }
                    getPlayerCoord();
                    //updateOtherPositions();
                }catch(EOFException e){
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // end infinite loop
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
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This method retrieves the player coordinates in screen coordinates to the
     * server to store. (This will probably be changed to convert to screen coordinates
     * and send back to the client later).
     */
    public void getPlayerCoord(){
        String coords = "";
        String x;
        String y;
        try{
            coords = dis.readUTF();
            //System.out.println("Position read: "+coords);
        }catch(IOException e){
            e.printStackTrace();
        }
        x = coords.split(" ")[0];
        y = coords.split(" ")[1];
        position.setPosition(Float.parseFloat(x),Float.parseFloat(y));
    }
    public int getClientId(){
        return clientId;
    }

    /**
     * updates other players' position on the screen.
     */
    public void updateOtherPositions(){
        for(Iterator<LevelServer> i = Server.clients.iterator();i.hasNext();){
            LevelServer s = i.next();
            try {
                this.dos.writeUTF(s.position.stringify());
                this.dos.flush();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
