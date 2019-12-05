package server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class ClientHandler extends Thread{
    private Socket socket;   // Socket of client and server
    private ObjectOutputStream os;  // the output stream
    private ObjectInputStream is;  // the input stream
    private int id;    /// the thread id (based on port number in socket)
    private boolean writeSuccess;
    private BlockingQueue<Msg> threadQueue;
    public ClientHandler(Socket s, ObjectInputStream is, ObjectOutputStream os,
                         BlockingQueue<Msg> queue){
        socket = s;
        this.is = is;
        this.os = os;
        id = s.getPort();
        threadQueue = queue;
        writeSuccess = true;
        Server.clientQueues.add(threadQueue);

    }

    @Override
    public void run(){
        try{
            // Write the map onto the client for rendering
            os.writeObject(Server.map);
            //System.out.println("Wrote map` "+ Server.map.getClass().getSimpleName());
            os.flush();
            os.write(id);
            //System.out.println("Wrote id "+ id);
            os.flush();
            sendItemList();
            sendEnemyList();
            while(true) {
                try {
                    // Receive coordinate message from the client
                    Msg message = (Msg) is.readObject();
                   System.out.println("Client Handler "+id+": "+message);
                    toServer(message);
                    writeSuccess = writeToClient();
                    if (!writeSuccess || message.type.equals("Exit"))
                        break;

                }catch(SocketException | ClassNotFoundException e){
                    System.out.println("Client "+id+" closed unexpectedly.\nClosing connections " +
                            "and terminating thread.");
                    break;
                }
            }
            Server.clientQueues.remove(threadQueue);
            os.close();
            is.close();
            socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }

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
          //System.out.println("Sending Enemy info "+ s)
            os.writeObject(Server.enemies);
            os.flush();
            System.out.println("Wrote Server.enemies, type:  "+ Server.enemies.getClass().getSimpleName());
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
            os.writeObject(toClient);
            os.flush();
            System.out.println("Sent to client "+id+": "+toClient);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void sendItemList(){
        try{
            os.writeObject(Server.worldItems);
            os.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public int getClientId(){
        return id;
    }
}
