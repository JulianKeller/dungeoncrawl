/*
 * Multithreaded Client Example from GeeksforGeeks.org
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;



public class Client {
    public Client(){

    }
    public static void main(String [] args){
        try {
            Scanner scn = new Scanner(System.in);
            byte [] ipAddr = new byte[] {127,0,0,1};

            // getting localhost ip
            InetAddress ip = InetAddress.getByAddress(ipAddr);

            // establish the connection with server port 5000
            Socket s = new Socket(ip, 5000);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs an exchange of information
            // between the client and client handler
            while(true){
                System.out.println(dis.readUTF());
                String tosend = scn.nextLine();
                dos.writeUTF(tosend);

                // If client sends exit, close this connection and break
                // from while loop
                if(tosend.equals("Exit")){
                    System.out.println("Closing this connection");
                    s.close();
                    System.out.println("Connection closed");
                    break;
                }

                // printing date or time as requested by client
                String received = dis.readUTF();
                System.out.println(received);

            }
            // closing resources
            scn.close();
            dis.close();
            dos.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
