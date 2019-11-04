import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    // initialize socket and input/output streams
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out  = null;

    public Client(String address, int port){
        //establish a connection
        try{
            socket = new Socket(address,port);
            System.out.println("Connected");

            // takes input from terminal
            input = new DataInputStream(System.in);

            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());

            String line = "";
            while(!line.equals("Over")){
                try{
                    line = input.readLine();
                    out.writeUTF(line);
                } catch(IOException e){
                    System.out.println(e);
                }
            }

        } catch(IOException i){
            System.out.println(i);
        }
        // close the connection
        try{
            input.close();
            out.close();
            socket.close();
        } catch(IOException e){
            System.out.println(e);
        }
    }
    public static void main(String [] args){
        Client client = new Client("127.0.0.1", 5000);
    }
}
