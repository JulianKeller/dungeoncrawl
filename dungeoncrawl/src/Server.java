/*
 * Multithreaded Server Example from GeeksforGeeks.org
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 *
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;


public class Server {
    // setting up server class to be starting up in Main.java
    static ArrayList<ClientHandler> clients;
    private int clientsConnected;

    public Server() throws IOException{
        // server is listening on port 5000
        ServerSocket ss = new ServerSocket(5000);
        clients = new ArrayList<ClientHandler>(4);
        clientsConnected = 0;
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
                ClientHandler t = new ClientHandler(s,clientsConnected,dis,dos);
                clients.add(t);
                clientsConnected++;

                // Invoking the start() method
                t.start();
                clientsConnected++;
            } catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }

    public class ClientHandler extends Thread {
        final DataInputStream dis;
        final DataOutputStream dos;
        final Socket s;
        private String message;
        private String read;

        public ClientHandler(Socket s, int clientNo, DataInputStream dis, DataOutputStream dos){
            this.s = s;
            this.dis = dis;
            this.dos = dos;
        }
        @Override
        public void run(){
            while(true){
                try{
                    read = dis.readUTF();
                    System.out.println(read);
                    switch(read){
                        case "Exit":
                            System.out.println("Client "+ this.s + " sends exit...");
                            System.out.println("Closing this connection.");
                            this.s.close();
                            System.out.println("Connection closed");
                            break;
                        case "W":
                            message = "You pressed W.";
                            break;
                        case "S":
                            message = "You pressed S.";
                            break;
                        case "A":
                            message = "You pressed A.";
                            break;
                        case "D":
                            message = "You pressed D.";
                            break;
                        default:
                            message = "Unrecognized input.";
                            break;
                    }
                    if(read.equals("Exit"))
                        break;
                    dos.writeUTF(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                // closing resources
                this.dis.close();
                this.dos.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String [] args) throws IOException {
        new Server();
    }
}