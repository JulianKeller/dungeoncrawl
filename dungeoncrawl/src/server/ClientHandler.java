package server;

import client.Character;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;


// TODO investigate why reading and writing different position values is not working
//  it seems the client is not getting the updated value, but the server is sending it
public class ClientHandler extends Thread {
    private Socket socket;   // Socket of client and server
    private ObjectOutputStream outStream;  // the output stream
    private ObjectInputStream inStream;  // the input stream
    private int id;    /// the thread id (based on port number in socket)
    private boolean writeSuccess;
    public BlockingQueue<Msg> threadQueue;
    //    private float[][] weights;
    private boolean debug = false;
    private boolean exit = false;
    int tilesize = 32;
    int offset = tilesize/2;
    int doubleOffset = offset/2;


    public ClientHandler(Socket s, ObjectInputStream is, ObjectOutputStream os, BlockingQueue<Msg> queue) {
        socket = s;
        this.inStream = is;
        this.outStream = os;
        id = s.getPort();
        threadQueue = queue;
        writeSuccess = true;
    }

    @Override
    public void run() {
        boolean init = true;

        try {
            // Write the map onto the client for rendering
            outStream.writeObject(Server.map);
            if (debug) System.out.println("send map " + Server.map.getClass().getSimpleName());
            outStream.reset();

            // read in type of hero from client
            String type = inStream.readUTF();
            System.out.println("read: " + type);
            // spawn the hero
            // TODO update spawning to be dynamic
            float wx = (tilesize * 20) - offset;
            float wy = (tilesize * 18) - tilesize - doubleOffset;
            Msg hero = new Msg(id, type, wx, wy, 100, false);
            sendHeroToClient(hero);

            Server.characters.add(hero);
//            outStream.writeInt(id);
//            if (debug) System.out.println("send id " + id);
//            outStream.reset();

            sendEnemyList();
            sendItemList();
            sendCharactersToClient();
            while (true) {
                try {
                    readCharactersFromClient();
                    sendCharactersToClient();
                    if (exit) {
                        break;
                    }
                    // TODO handle exit
                } catch (Exception e) {
                    if (debug) System.out.println("Client " + id + " closed unexpectedly.\nClosing connections " +
                            "and terminating thread.");
                    break;
                }
            }
            Server.clients.remove(this);
            outStream.close();
            inStream.close();
            socket.close();
            this.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * send the hero to the client on enter
     * @param hero
     */
    private void sendHeroToClient(Msg hero) throws IOException {
        if (debug) System.out.println("sendHeroToClient()");
        outStream.writeObject(hero);
        System.out.printf("send: %s\n\n", hero);
    }


    /*
    Send weights from dijkstra's to the client
     */
    private void sendWeightsToClient(Msg msg) {
        toServer(msg);
        writeToClient();
    }


    /**
     * update the client with the position of the ai
     */
    public void sendAIStatusToClient() {
//        if (debug) System.out.println("sendAIStatusToClient()");
        synchronized (Server.enemies) {
            for (Msg ai : Server.enemies) {
                toServer(ai);
                writeToClient();
            }
        }
//         if (debug) System.out.println();
    }


    /*
    read the information about the AI from the server
     */
    private void readAIStatusFromClient() {
        if (debug) System.out.println("readAIStatusFromClient()");
        synchronized (Server.enemies) {
            for (Msg ai : Server.enemies) {
                try {
                    Msg msg = (Msg) inStream.readObject();
                    if (debug) System.out.printf("send " + msg);
                    ai.wx = msg.wx;
                    ai.wy = msg.wy;
                    ai.hp = msg.hp;
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (debug) System.out.println();
    }

    /**
     * This function places the string into the Server Queue.
     *
     * @param m message to send
     */
    private void toServer(Msg m) {
        try {
            Server.serverQueue.put(m);
//            if (debug) System.out.println("Adding to queue: " + m.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send the list of enemies tot he client
     */
    private void sendEnemyList() {
        if (debug) System.out.println("sendEnemyList()");
        try {
            synchronized (Server.enemies) {
                outStream.writeObject(Server.enemies);
                if (debug) System.out.printf("send Server.enemies\n");
            }
            outStream.reset();
//            if (debug) System.out.println("Wrote ArrayList Server.enemies");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (debug) System.out.println();
    }


    /**
     * This method takes from what the server gives to the client
     * and writes to the client.
     */
    // TODO why are we putting and taking from different queues?
    private boolean writeToClient() {
        if (debug) System.out.println("writeToClient()");
        try {
            Msg toClient = threadQueue.take();
            outStream.writeObject(toClient);
            if (debug) System.out.printf("send %s\n", toClient);
            outStream.reset();
//            if (debug) System.out.println("Sent to client "+id+": "+toClient);
            if (debug) System.out.println();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            exit = true;
            return false;
        }
        return true;
    }

    public void readCharactersFromClient() {
        if (debug) System.out.println("readCharactersFromClient()");
        int count = 0;
        try {
            count = inStream.readInt();
            if (debug) System.out.printf("Read: %s\n", count);
            synchronized (Server.characters) {
                for (int i = 0; i < count; i++) {
                    Msg character = Server.characters.get(i);
                    System.out.printf("reading %s: wx= %s, wy= %s, ks= %s\n", i, character.wx,
                            character.wy, character.ks);
                    Msg msg = (Msg) inStream.readObject();

                    if (msg.type.equals("Exit")) {
                        exit = true;
                    }

                    if (debug) System.out.printf("read: %s\n", msg);
                    character.wx = msg.wx;
                    character.wy = msg.wy;
                    character.ks = msg.ks;
                    character.hp = msg.hp;
/*                    System.out.printf("character %s after: wx= %s, wy= %s, ks= %s\n\n", i, character.wx,
                            character.wy, character.ks);*/
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exiting Game: failed to read character: " + e);
            exit = true;
        }
        if (debug) System.out.println();
    }

    public void sendCharactersToClient() {
        if (debug) System.out.println("sendCharactersToClient()");
        synchronized (Server.characters) {
            int count = Server.characters.size();
            try {
                outStream.writeInt(count);
                outStream.reset();
                if (debug) System.out.printf("Send %s items\n", count);
                for (int i = 0; i < count; i++) {
                    Msg character = Server.characters.get(i);
                    toServer(character);
                    writeToClient();
//                    System.out.printf("sending %s: wx= %s, wy= %s, ks= %s\n", i, character.wx,
//                            character.wy, character.ks);
//                    outStream.writeObject(character);
//                    outStream.reset();
//                    if (debug) System.out.printf("send %s\n", character);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (debug) System.out.println();
    }

    
    public void sendItemList() {
        if (debug) System.out.println("sendItemList()");
        try {
            int count = Server.worldItems.size();
            outStream.writeInt(count);
            if (debug) System.out.println("send " + count);
            outStream.reset();
            synchronized (Server.worldItems) {
                for (int i = 0; i < count; i++) {
                    ItemMsg item = Server.worldItems.get(i);
                    outStream.writeObject(item);
                    if (debug) System.out.print("send item\n");
                    outStream.reset();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (debug) System.out.println();
    }

    public int getClientId() {
        return id;
    }

}
