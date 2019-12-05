package server;

import client.Character;
import client.Main;

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
            System.out.println("Wrote map` "+ Server.map.getClass().getSimpleName());
            outStream.flush();
            outStream.writeInt(id);
            System.out.println("Wrote id "+ id);
            outStream.flush();
            sendEnemyList();
            while(true) {
                try {
                    // Receive coordinate message from the client
                    Msg message = (Msg) inStream.readObject();
                    System.out.println("reading 'message' type: " + message.getClass().getSimpleName());
                    toServer(message);
                    writeSuccess = writeToClient();
                    if (!writeSuccess || message.type.equals("Exit"))
                        break;

                    // TODO update AI positions
                    AI.updatePosition();
                    sendAIStatusToClient();
                    readAIStatusFromClient();


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
        Msg msg;
        float wx;
        float wy;
//        try {
////            System.out.printf("Sent count %s to server\n", Server.enemies.size());
//            if (Server.aiList.size() <= 0) {
//                outStream.writeInt(0);
//            }
//            else {
//                outStream.writeInt(Server.aiList.size());
//            }
//            outStream.flush();
//            System.out.println("Wrote count: " + Server.aiList.size());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        for (Msg ai : Server.aiList) {
            try {
                outStream.writeObject(ai);
                outStream.flush();
                System.out.println("Sending ai: " + ai.toString());
//                System.out.println("sent AI update type: " + ai.getClass().getSimpleName());
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        System.out.println();
    }


    /*
    read the information about the AI from the server
     */
    private void readAIStatusFromClient() {
//        int count = 0;
//        try {
//            count = inStream.readInt();
//            System.out.printf("Read count %s from client\n", count);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.printf("FAILED Read count %s from client\n", count);
//        }


        for (Msg ai : Server.aiList) {
            try {
                Msg msg = (Msg) inStream.readObject();
                System.out.println("Reading ai: " + msg.toString());
                ai.hp = msg.hp;
//                System.out.println("read msg from client type: "+ msg.getClass().getSimpleName());
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
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
            //System.out.println("To Server: "+ id + " "+m);
            Server.serverQueue.put(m);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void sendEnemyList(){
        try {
//            System.out.printf("Sent count %s to server\n", Server.enemies.size());
            if (Server.aiList.size() <= 0) {
                outStream.writeInt(0);
            }
            else {
                outStream.writeInt(Server.aiList.size());
            }
            outStream.flush();
            System.out.println("Wrote count: " + Server.aiList.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            for (Msg ai : Server.aiList) {
                //System.out.println("Sending Enemy info "+ s)
                outStream.writeObject(ai);
                outStream.flush();
                System.out.println("Wrote ai: " + ai.toString());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    /**
     * This method takes from what the server gives to the client
     * and writes to the client.
     */
    private boolean writeToClient() {
        try {
            Msg toClient = threadQueue.take();
            //System.out.println("Writing to client "+id+": "+toClient);
            outStream.writeObject(toClient);
            outStream.flush();
            System.out.println("Wrote MSG `toClient` "+ toClient.getClass().getSimpleName());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public int getClientId(){
        return id;
    }
}
