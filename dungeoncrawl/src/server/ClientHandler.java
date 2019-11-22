package server;

import java.io.*;
import java.net.*;
import java.util.Iterator;

public class ClientHandler extends Thread{
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private int clientId;  // clientid is based on port number
    private PlayerPosition position;

    /**
     * creates a new map and character
     */
    public ClientHandler(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, int id){
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
        clientId = id;
        position = null;

    }
    /**
     * This is what is called when the main server function invokes start().
     */
    @Override
    public void run(){


    }
}
