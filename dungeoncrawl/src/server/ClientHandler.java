package server;

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
    private boolean debug = true;
    private boolean exit = false;
    public Boolean send = false;
    int tilesize = 32;
    int offset = tilesize/2;
    int doubleOffset = offset/2;
    public final PauseObject pauseObject;


    public ClientHandler(Socket s, ObjectInputStream is, ObjectOutputStream os, BlockingQueue<Msg> queue, int id) {
        socket = s;
        this.inStream = is;
        this.outStream = os;
        this.id = id;
        threadQueue = queue;
        writeSuccess = true;
        pauseObject = new PauseObject();
    }

    @Override
    public void run() {
        try {
            // Write the map onto the client for rendering
            outStream.writeObject(Server.map);
            if (debug) System.out.println("send map " + Server.map.getClass().getSimpleName());
            outStream.reset();

            // read in type of hero from client
            String type = inStream.readUTF();
            if (debug) System.out.println("read: " + type);
            // spawn the hero
            // TODO update spawning to be dynamic
            float wx = (tilesize * 20) - offset;
            float wy = (tilesize * 18) - tilesize - doubleOffset;
            Msg hero = new Msg(id, type, wx, wy, 100, false);
            sendHeroToClient(hero);
            Server.characters.add(hero);

            sendEnemyList();
            sendItemList();
            sendCharactersToClient();
            while (true) {
                try {
                    readHeroFromClient();
                    synchronized (pauseObject) {
                        pauseObject.wait();
                    }
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
        if (debug) System.out.println("sendHeroToClient() " + this.getId());
        outStream.writeObject(hero);
        if (debug) System.out.printf("send: %s\n\n", hero);
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
        if (debug) System.out.println("readAIStatusFromClient() " + this.getId());
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
        if (debug) System.out.println("sendEnemyList() " + this.getId());
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
        if (debug) System.out.println("writeToClient() " + this.getId());
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

    public void readHeroFromClient() {
        if (debug) System.out.println("readCharactersFromClient() " + this.getId());
        try {
            synchronized (Server.characters) {
                Msg hero = Server.characters.get(id);
                Msg msg = (Msg) inStream.readObject();
                if (msg.type.equals("Exit")) {
                    exit = true;
                }
                if (debug) System.out.printf("read: %s\n", msg);
                hero.wx = msg.wx;
                hero.wy = msg.wy;
                hero.ks = msg.ks;
                hero.hp = msg.hp;
            }
        } catch (IOException | ClassNotFoundException e) {
            if (debug) System.out.println("Exiting Game: failed to read character: " + e);
            exit = true;
        }
        if (debug) System.out.println();
    }


    public void sendCharactersToClient() {
        if (debug) System.out.println("sendCharactersToClient() " + this.getId());
            int count = Server.characters.size();
            try {
                outStream.writeInt(count);
                outStream.reset();
                if (debug) System.out.printf("send %s items\n", count);
                for (int i = 0; i < count; i++) {
//                    Msg character = Server.characters.get(i);
//                    toServer(character);
//                    writeToClient();
//                    outStream.writeObject(character);
//                    outStream.reset();
//                    if (debug) System.out.printf("send %s\n", character);
                    Msg toClient = null;
                    try {
                        toClient = threadQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outStream.writeObject(toClient);
                    if (debug) System.out.printf("%s :send %s\n", this.getId(), toClient);
                    outStream.reset();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (debug) System.out.println();
    }

    
    public void sendItemList() {
        if (debug) System.out.println("sendItemList() " + this.getId());
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


    public class PauseObject {
    }

}
