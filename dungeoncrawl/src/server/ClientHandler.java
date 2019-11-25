package server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class ClientHandler extends Thread{
    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private int id;
    public BlockingQueue<String> threadQueue;
    public ClientHandler(Socket s, ObjectInputStream is, ObjectOutputStream os){
        socket = s;
        this.is = is;
        this.os = os;
        id = s.getPort();
        threadQueue = new ArrayBlockingQueue<>(50);

    }

    @Override
    public void run(){
        try{
            os.writeObject(Server.map);
            os.flush();
            while(true) {
                String message = is.readUTF();
                System.out.println("Read from client: "+message);
                toServer(message);
                if(message.split(" ")[0].equals("Exit")){
                    toServer(message);
                    break;
                }
                writeToClient();
            }
            os.close();
            is.close();
            socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    private void toServer(String m){
        try {
            Server.serverQueue.put(id +" "+  m);

        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
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
