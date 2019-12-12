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
    private BlockingQueue<Msg> threadQueue;
    private float[][] weights;
    String clientStatus;
    public ClientHandler(Socket s, ObjectInputStream is, ObjectOutputStream os,
                         BlockingQueue<Msg> queue){
        socket = s;
        this.inStream = is;
        this.outStream = os;
        id = s.getPort();
        threadQueue = queue;
        writeSuccess = true;
        Server.clientQueues.add(threadQueue);


    }

    @Override
    public void run(){
        try{
            // Write the map onto the client for rendering
            outStream.writeObject(Server.map);
            System.out.println("Writing map "+ Server.map.getClass().getSimpleName());
            outStream.flush();
            outStream.writeInt(id);
            System.out.println("Writing id "+ id);
            outStream.flush();
            sendEnemyList();
            sendItemList();
            while(true) {
                try {
                    // Receive coordinate message from the client
                   //System.out.println("reading 'message' type: " + message.getClass().getSimpleName());
                    int size = (int) inStream.readObject();
//                    System.out.println("reading " + message.toString());
//                    System.out.println("Client Handler "+id+": "+message);
                    clientStatus = toServer(size);
                    writeSuccess = writeToClient();
                    if (!writeSuccess || clientStatus.equals("Exit"))
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
            Server.clientQueues.remove(threadQueue);
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
        //toServer(msg);
        writeToClient();
    }


    /**
     * update the client with the position of the ai
     */
    public void sendAIStatusToClient() {
//        System.out.println("sendAIStatusToClient()");
        for (Msg ai : Server.enemies) {
            //toServer(ai);
            writeToClient();
        }
        // System.out.println();
    }


    /*
    read the information about the AI from the server
     */
    private void readAIStatusFromClient() {
//        System.out.println("readAIStatusFromClient()");
        for (Msg ai : Server.enemies) {
            try {
                Msg msg = (Msg) inStream.readObject();
//                System.out.println("reading " + msg.toString());
                ai.wx = msg.wx;
                ai.wy = msg.wy;
                ai.hp = msg.hp;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
//        // System.out.println();
    }

    /**
     * This function places the string into the Server Queue.
     * @param s how many messages to place onto the server.
     */
    private String toServer(int s){
        try {
            if(s >= 10) {
                for (int i = 0; i < s; i++) {
                    Msg msg = (Msg) inStream.readObject();
                    Server.serverQueue.put(msg);
                    if (msg.type.equals("Exit"))
                        return "Exit";
                }
            }
//            System.out.println("Adding to queue: " + m.toString());
        } catch (IOException | ClassNotFoundException | InterruptedException e){
            e.printStackTrace();
        }
        return "";
    }

    private void sendEnemyList(){
        try {
            outStream.writeObject(Server.enemies);
            outStream.flush();
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
            int size = threadQueue.size();
            outStream.writeObject(size);
            outStream.flush();
            outStream.reset();
            for(int i = 0; i < size; i++) {
                Msg toClient = threadQueue.take();
                outStream.writeObject(toClient);
//            System.out.println("writing " + toClient.toString());
                outStream.flush();
                outStream.reset();
                outStream.flush();
            }
//            System.out.println("Sent to client "+id+": "+toClient);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void sendItemList(){
        try{
            outStream.writeObject(Server.worldItems);
            outStream.flush();
            outStream.reset();
            System.out.println("Wrote Server.worldItems, type: "+Server.worldItems.getClass().getSimpleName());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public int getClientId(){
        return id;
    }
}
