package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import client.RenderMap;

public class Server extends Thread{
    // Static Objects for each thread.
    private static BlockingQueue<PlayerPosition> serverQueue = new LinkedBlockingQueue<>(10);
    private static int [][] map;

    private static void getMap() {
        try {
            map = RenderMap.getRandomMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Objects that are only server
    private Socket socket;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private BlockingQueue<String> threadQueue;

    public Server(Socket socket) throws IOException{
        this.socket = socket;
        is = new ObjectInputStream(socket.getInputStream());
        os = new ObjectOutputStream(socket.getOutputStream());
        threadQueue = new LinkedBlockingQueue<>(10);
    }

    @Override
    public void run() {
        String message = "";
        try {
            os.writeObject(map);
            message = is.readUTF();
            while(true){

            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }
    private void toQueue(String m){
        try {
            serverQueue.put(new PlayerPosition(socket.getPort(), m.split(" ")[0],
                    Float.parseFloat(m.split(" ")[1]),
                    Float.parseFloat(m.split(" ")[2])));
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void main(String [] args) throws IOException{
        ServerSocket ss = new ServerSocket(5000);
        getMap();

    }


}
