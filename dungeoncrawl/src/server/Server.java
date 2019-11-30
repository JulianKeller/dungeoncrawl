package server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Stream;

//import client.RenderMap;

public class Server extends Thread{
    // Static Objects for each thread.
    public static BlockingQueue<String> serverQueue = new LinkedBlockingQueue<>();
    public static int [][] map;
    private BlockingQueue<String> threadQs;


    // grabs a random map and returns it as a 2d array
    public static int[][] getRandomMap() throws IOException {
        File f;
        if( System.getProperty("os.name").toLowerCase().contains("windows")){
            f = new File("src/maps");
        }else{
            f = new File("dungeoncrawl/src/maps");
        }
        Random r = new Random();
        int rand = r.nextInt(100);
        String filepath = f.getAbsolutePath() + "/map" + rand + ".txt";
        System.out.println("Loading Map: " + "map" + rand + ".txt");
        return loadMapFromFile(Paths.get(filepath));
    }

    /*
    Based on Streams from: https://stackoverflow.com/questions/22185683/read-txt-file-into-2d-array
    Uses Java's Stream API to convert a text file to a 2d int array
     */
    static public int[][] loadMapFromFile(Path path) throws IOException {
        return Files.lines(path)                        // Read all lines from the filepath
                .map(line -> line.split("\\s"))   // for each line, get an array chars split by spaces
                .map((sa) -> Stream.of(sa)              // convert char array to a sequential ordered stream
                        .mapToInt(Integer::parseInt)            // map the char array to an int stream
                        .toArray())                             // convert the int stream to an array
                .toArray(int[][]::new);                 // add the array to a 2d array
    }


    public Server(BlockingQueue<String> queue){
        threadQs = queue;
    }
    @Override
    public void run() {
        while(true){
            sendToClients();

        }
    }

    public void sendToClients(){
            try {
                String playerInfo = serverQueue.take();
                threadQs.put(playerInfo);

                } catch(InterruptedException e){
                    e.printStackTrace();
                }
    }

    public static void main(String [] args){
        try {
            // Create a new Socket for the server
            ServerSocket ss = new ServerSocket(5000);
            // Generate the map
            map = getRandomMap();
            // Create a blocking queue for the threads
            BlockingQueue<String> threadQ = new LinkedBlockingQueue<>();
            // Start a Server thread that will handle distributing to the client and servers.
            Server server = new Server(threadQ);
            server.start();
            // This listens for new connections.
            while (true) {
                Socket s = ss.accept();
                System.out.println("A new client has connected " + s);
                ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(s.getInputStream());
                // This is the client handler thread.
                System.out.println("Creating new thread for this client...");
                ClientHandler t = new ClientHandler(s, is, os, threadQ);
                t.start();

            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }



}
