import jig.Vector;
import java.io.*;
import java.net.*;

public class LevelServer extends Thread{
    private Integer [][] map;           // Holds the 2d map file
    private Socket socket;             // holds the socket
    private ObjectInputStream dis;     // holds the input stream
    private ObjectOutputStream dos;    // output stream
    private int clientId;              // clientid is based on port number
    private Vector playerCoord;        // stores the current coordinates of the player.

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
                    // Switch based on client's input.
                    switch(inputCode){  // for each direction, first check for a collision, if so don't update
                        case "w": //w     // otherwise update.
                            if(collision(inputCode)){
                                dos.writeUTF("");
                            } else{
                                playerCoord = new Vector(playerCoord.getX(),playerCoord.getY()-Server.tilesize);
                                toSend = (playerCoord.getX())+" "+(playerCoord.getY());
                                dos.writeUTF(toSend);
                            }
                            break;
                        case "s":
                            if(collision(inputCode)){
                                dos.writeUTF("");
                            } else{
                                playerCoord = new Vector(playerCoord.getX(),playerCoord.getY()+Server.tilesize);
                                toSend = (playerCoord.getX())+" "+(playerCoord.getY());
                                dos.writeUTF(toSend);
                            }
                            break;
                        case "a":
                            if(collision(inputCode)){
                                dos.writeUTF("");
                            } else{
                                playerCoord = new Vector(playerCoord.getX()-Server.tilesize,playerCoord.getY());
                                toSend = (playerCoord.getX())+" "+(playerCoord.getY());
                                dos.writeUTF(toSend);
                            }
                            break;
                        case "d":
                            if(collision(inputCode)){
                                dos.writeUTF("");
                            } else{
                                playerCoord = new Vector(playerCoord.getX()+Server.tilesize,playerCoord.getY());
                                toSend = (playerCoord.getX())+" "+(playerCoord.getY());
                                dos.writeUTF(toSend);
                            }
                            break;
                        default:
                            dos.writeUTF("");
                            break;
                    }
                    dos.flush();
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
        }catch(IOException e){
            e.printStackTrace();
        }
        x = coords.split(" ")[0];
        y = coords.split(" ")[1];
        playerCoord = new Vector(Float.parseFloat(x),Float.parseFloat(y));
        System.out.println("Client "+ clientId+ "x: "+playerCoord.getX()+" y: "+playerCoord.getY());
    }
    public int getClientId(){
        return clientId;
    }

    /**
      * check if there is a collision at the next x, y with the wall
      * returns true if there is a collision, false otherwise
     */
    // TODO this method needs to be adjusted for the screen coordinates
    // NOTE: This method was modified from Character.collision().
    public boolean collision(String direction) {
        int x = (int) playerCoord.getX();
        int y = (int) playerCoord.getY();
//        System.out.printf("Position x, y:  %s, %s --> %s, %s\n", x, y, x/dc.tilesize, y/dc.tilesize);
        if (direction.equals("w")) {
            y -= Server.tilesize;
        }
        else if (direction.equals("s")) {
            y += Server.tilesize;
        }
        else if (direction.equals("a")) {
            x -= Server.tilesize;
        }
        else if (direction.equals("d")) {
            x += Server.tilesize;
        }
//        System.out.printf("this x, this y:  %s, %s\n", this.getX(), this.getY());
//        System.out.printf("Collision? x, y:  %s, %s", x, y);
        x = x/Server.tilesize;
        y = y/Server.tilesize;
//        System.out.printf(" -->  %s, %s\n\n", x, y);
        return (Server.map[y][x] != 0);
    }
}
