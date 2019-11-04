/*
 * Server hands the socket to a new ClientHandler thread.
 * This is from the Multithreaded Server example from GeeksforGeeks.org
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 */

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler extends Thread {
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;

    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos){
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }
    @Override
    public void run(){
        String received;
        String toreturn;
        while(true){
            try{
                // Ask user what they want
                dos.writeUTF("What do you want?[Date | Time]..\n" +
                        "Type Exit to terminate connection.");
                received = dis.readUTF();

                if(received.equals("Exit")){
                    System.out.println("Client "+ this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }
                // creating Date object
                Date date = new Date();

                // write on output stream based on answer from client
                switch(received) {
                    case "Date":
                        toreturn = fordate.format(date);
                        dos.writeUTF(toreturn);
                        break;

                    case "Time":
                        toreturn = fortime.format(date);
                        dos.writeUTF(toreturn);
                        break;

                    default:
                        dos.writeUTF("Invalid input");
                        break;
                }
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
