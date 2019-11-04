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


        } catch()
    }
    public static void Main(String [] args){

    }
}
