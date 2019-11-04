import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Server {
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    public Server(int port) {
        // starts server and waits for connection
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
            System.out.println("Server Started");
            System.out.println("Waiting for a client...");
            socket = server.accept();
            System.out.println("Client accepted");

            // takes input from the client socket
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String line = "";

            // reads message from client until "Over" is sent
            while (!line.equals("Over")) {
                try {
                    line = in.readUTF();
                    System.out.println((line));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Closing connection");
            // close connection
            socket.close();
            in.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    }
    public static void main(String [] args){
        Server server = new Server(5000);
    }
}
