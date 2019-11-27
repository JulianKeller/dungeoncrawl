package server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class ClientHandler extends Thread{
    private Socket socket;   // Socket of client and server
    private ObjectOutputStream os;  // the output stream
    private ObjectInputStream is;  // the input stream
    private int id;    /// the thread id (based on port number in socket)
    private BlockingQueue<String> threadQueue;
    public ClientHandler(Socket s, ObjectInputStream is, ObjectOutputStream os,
                         BlockingQueue<String> queue){
        socket = s;
        this.is = is;
        this.os = os;
        id = s.getPort();
        threadQueue = queue;

    }

    @Override
    public void run(){
        try{
            // Write the map onto the client for rendering
            os.writeObject(Server.map);
            os.flush();
            os.writeUTF(Integer.toString(id));
            os.flush();
            while(true) {
                // Receive coordinate message from the client
                String message = is.readUTF();
                if(message.split(" ")[0].equals("Exit")){
                    toServer(message);
                    break;
                }
                toServer(message);
                writeToClient();
            }
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
    private void toServer(String m){
        try {
            //System.out.println("To Server: "+ id + " "+m);
            Server.serverQueue.put(id +" "+  m);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * This method takes from what the server gives to the client
     * and writes to the client.
     */
    private void writeToClient() {
        try {
            String toClient = threadQueue.take();
            os.writeUTF(toClient);
            os.flush();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
    public int getClientId(){
        return id;
    }
}
