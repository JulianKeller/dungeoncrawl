package server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;


// TODO investigate why reading and writing different position values is not working
//  it seems the client is not getting the updated value, but the server is sending it
public class ClientHandler extends Thread{
    private Socket socket;   // Socket of client and server
    private ObjectOutputStream outStream;  // the output stream
    private ObjectInputStream inStream;  // the input stream
    private int id;    /// the thread id (based on port number in socket)
    private boolean writeSuccess;
    private BlockingQueue<Msg> threadQueue;
    float hp = 1;
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
                    Msg message = (Msg) inStream.readObject();
//                    System.out.println("reading " + message.toString());
//                    System.out.println("Client Handler "+id+": "+message);
                    toServer(message);
                    writeSuccess = writeToClient();
                    if (!writeSuccess || message.type.equals("Exit"))
                        break;

                    // TODO update AI positions
                    readAIStatusFromClient();
                    AI.updatePosition();
                    sendAIStatusToClient();



                }catch(SocketException | ClassNotFoundException e){
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


    /**
     * update the client with the position of the ai
     */
    public void sendAIStatusToClient() {
//        System.out.println("sendAIStatusToClient()");
        for (Msg ai : Server.enemies) {
            toServer(ai);
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
                ai.hp = msg.hp;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
//        // System.out.println();
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
            Msg toClient = threadQueue.take();
            outStream.writeObject(toClient);
//            System.out.println("writing " + toClient.toString());
            outStream.flush();
            outStream.reset();
            outStream.flush();
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
