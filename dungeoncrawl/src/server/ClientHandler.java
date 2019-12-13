package server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;


// TODO investigate why reading and writing different position values is not working
//  it seems the client is not getting the updated value, but the server is sending it
public class ClientHandler extends Thread {
    private Socket socket;   // Socket of client and server
    private ObjectOutputStream outStream;  // the output stream
    private ObjectInputStream inStream;  // the input stream
    private int id;    /// the thread id (based on port number in socket)
    private boolean writeSuccess;
    public BlockingQueue<Msg> threadQueue;
    private float[][] weights;
    public Msg hero;
    public ClientHandler(Socket s, ObjectInputStream is, ObjectOutputStream os,
                         BlockingQueue<Msg> queue){
        socket = s;
        this.inStream = is;
        this.outStream = os;
        id = s.getPort();
        threadQueue = queue;
        writeSuccess = true;
    }

    @Override
    public void run(){
        boolean init = true;
        try{
            // Write the map onto the client for rendering
            outStream.writeObject(Server.map);
            System.out.println("send map "+ Server.map.getClass().getSimpleName());
            outStream.flush();
            outStream.writeInt(id);
            System.out.println("send id "+ id);
            outStream.flush();
            sendEnemyList();
            sendItemList();
            while(true) {
                try {
                    // Receive coordinate message from the client about the Hero
                    Msg message = (Msg) inStream.readObject();
                    System.out.printf("read %s\n", message);
                    if (init) {
                        Server.characters.add(message);
                        init = false;
                    }
                    readCharactersFromClient();
                    sendCharactersToClient();
//                    System.out.println("reading " + message.toString());
//                    System.out.println("Client Handler "+id+": "+message);
                    //toServer(message);
                    //writeSuccess = writeToClient();
                    if (!writeSuccess || message.type.equals("Exit"))
                        break;

                    // Update the AI Positions
//                    readAIStatusFromClient();
//                    weights = AI.updatePosition(message);      // takes the hero's x, y coordinates
//                    message.dijkstraWeights = weights;
//                    sendAIStatusToClient();
//                    sendWeightsToClient(message);

                } catch(SocketException | ClassNotFoundException e){
                    System.out.println("Client "+id+" closed unexpectedly.\nClosing connections " +
                            "and terminating thread.");
                    break;
                }
            }
//            Server.clientQueues.remove(threadQueue);
            Server.clients.remove(this);
            outStream.close();
            inStream.close();
            socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    /*
    Send weights from dijkstra's to the client
     */
    private void sendWeightsToClient(Msg msg) {
        toServer(msg);
        writeToClient();
    }


    /**
     * update the client with the position of the ai
     */
    public void sendAIStatusToClient() {
//        System.out.println("sendAIStatusToClient()");
        synchronized (Server.enemies) {
            for (Msg ai : Server.enemies) {
                toServer(ai);
                writeToClient();
            }
        }
        // System.out.println();
    }


    /*
    read the information about the AI from the server
     */
    private void readAIStatusFromClient() {
//        System.out.println("readAIStatusFromClient()");
        synchronized (Server.enemies) {
        for (Msg ai : Server.enemies) {
            try {
                Msg msg = (Msg) inStream.readObject();
                System.out.printf("send " + msg);
                ai.wx = msg.wx;
                ai.wy = msg.wy;
                ai.hp = msg.hp;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        }
         System.out.println();
    }

    /**
     * This function places the string into the Server Queue.
     * @param m message to send
     */
    private void toServer(Msg m){
        try {
            Server.serverQueue.put(m);
//            System.out.println("Adding to queue: " + m.toString());
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void sendEnemyList(){
        try {
            synchronized (Server.enemies) {
                outStream.writeObject(Server.enemies);
                System.out.printf("send Server.enemies\n");
            }
            outStream.reset();
//            System.out.println("Wrote ArrayList Server.enemies");
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * This method takes from what the server gives to the client
     * and writes to the client.
     */
    // TODO why are we putting and taking from different queues?
    private boolean writeToClient() {
        try {
            Msg toClient = threadQueue.take();
            outStream.writeObject(toClient);
            System.out.printf("send %s\n", toClient);
            outStream.reset();
//            System.out.println("Sent to client "+id+": "+toClient);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public void readCharactersFromClient(){
        int count = 0;
        try {
            count = (int) inStream.readObject();
            synchronized(Server.characters) {
                for (int i = 0; i < count; i++) {
                    Msg character = Server.characters.get(i);
                    System.out.printf("character %s before: wx= %s, wy= %s, ks= %s\n",i,character.wx,
                            character.wy,character.ks);
                    Msg msg = (Msg)inStream.readObject();
                    character.wx = msg.wx;
                    character.wy = msg.wy;
                    character.ks = msg.ks;
                    System.out.printf("character %s after: wx= %s, wy= %s, ks= %s\n\n",i,character.wx,
                            character.wy,character.ks);
                }
            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void sendCharactersToClient(){
        synchronized (Server.characters){
            int count = Server.characters.size();
            try {
                outStream.writeObject(count);
                outStream.reset();
                for(int i = 0; i < count;i++){
                    Msg character = Server.characters.get(i);
                    outStream.writeObject(character);
                    outStream.reset();
                    System.out.printf("send %s\n",character);
                }
                System.out.printf("\n");
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public void sendItemList(){
        try{
            int count = Server.worldItems.size();
            outStream.writeInt(count);
//            System.out.println("writing " + toClient.toString());
            outStream.reset();

            synchronized (Server.worldItems) {
                for (int i = 0; i < count; i++) {
                    ItemMsg item = Server.worldItems.get(i);
                    outStream.writeObject(item);
                    System.out.print("send Server.enemies\n");
                    outStream.reset();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public int getClientId(){
        return id;
    }

}
