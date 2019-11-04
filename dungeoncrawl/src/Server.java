/*
 * Multithreaded Server Example from GeeksforGeeks.org
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 *
 */

import java.io.*;
import java.net.*;


public class Server {
    // setting up server class to be starting up in Main.java
//    public Server(){
//
//    }

    public static void main(String [] args) throws IOException {
        // server is listening on port 5000
        ServerSocket ss = new ServerSocket(5000);
        // infinite loop for getting client request
        while(true){
            Socket s = null;
            try {
                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("A new client is connected: " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                // Create a new thread object
                Thread t = new ClientHandler(s,dis,dos);

                // Invoking the start() method
                t.start();
            } catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}