package server;

import client.Character;
import client.Main;

import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.concurrent.*;

public class ClientHandler extends Thread {
    private Socket socket;      // Socket of client and server
    private ObjectOutputStream outStream;   // the output stream
    private ObjectInputStream inStream;     // the input stream
    private int id;             // the thread id (based on port number in socket)
    private boolean writeSuccess;
    public BlockingQueue<Msg> characterQueue;
    public BlockingQueue<Msg> enemyQueue;
    private boolean debug = false;
    private boolean exit = false;
    int tilesize = 32;
    int offset = tilesize/2;
    int doubleOffset = offset/2;
    public final PauseObject pauseObject;


    public ClientHandler(Socket s, ObjectInputStream is, ObjectOutputStream os, int id) {
        socket = s;
        this.inStream = is;
        this.outStream = os;
        this.id = id;
        characterQueue = new LinkedBlockingQueue<>();
        enemyQueue = new LinkedBlockingQueue<>();
        writeSuccess = true;
        pauseObject = new PauseObject();
    }

    @Override
    public void run() {
        try {
            // Write the map onto the client for rendering
            outStream.writeObject(Server.map);
            if (debug) System.out.println("send map " + Server.map.getClass().getSimpleName());
            outStream.flush();

            // read in type of hero from client
            String type = inStream.readUTF();
            if (debug) System.out.println("read: " + type);
            // spawn the hero
            Msg hero = spawnHero(type,Server.map);
            sendHeroToClient(hero);
            Server.characters.add(hero);

            sendEnemyList();
            sendItemList();
            sendCharactersToClient();
            while (true) {
                try {
                    if (exit) {
                        break;
                    }
                    readHeroFromClient();
                    sendCharactersToClient();

                    readEnemyStatusFromClient();
                    sendEnemiesToClient();

                    sendWeightsToClient();
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

    private Msg spawnHero(String type, int [][]map){
        int tilesize = 32;
        int offset = tilesize/2;
        int doubleOffset = offset/2;
        int maxcol =  20;
        int maxrow = 20;
        Random rand = new Random();
        int row = 0;
        int col = 0;
        do {
            col = rand.nextInt(maxcol);
            row = rand.nextInt(maxrow);
        }while(map[row][col] == 1);
        while(row < 2 || col < 2 || map[col][row] == 1){
            col = rand.nextInt(maxcol) - 1;
            row = rand.nextInt(maxrow) - 1;
        }
        float wx = (tilesize * row) - offset;
        float wy = (tilesize * col) - tilesize - doubleOffset;

        return new Msg(this.id,type,wx,wy,100,false,1);
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
    private void sendWeightsToClient() {
        Msg hero = Server.characters.get(id);
        try {
            outStream.writeObject(hero.dijkstraWeights);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /*
    read the information about the AI from the server
     */
    private void readEnemyStatusFromClient() {
        if (debug) System.out.println("readEnemyStatusFromClient() " + this.getId());
        synchronized (Server.enemies) {
            for (Msg ai : Server.enemies) {
                try {
                    Msg msg = (Msg) inStream.readObject();
                    if (debug) System.out.printf("send " + msg);
                    Msg.saveMsgToCharacter(ai, msg);
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (debug) System.out.println();
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


    /*
    Reads the hero details from the client
     */
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
                Msg.saveMsgToCharacter(hero, msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (debug) System.out.println("Exiting Game: failed to read character: " + e);
            exit = true;
        }
        if (debug) System.out.println();
    }


    /**
     * Take from the characterQueue and send to the client
     */
    public void sendCharactersToClient() {
        int count;
        if (debug) System.out.println("sendCharactersToClient() " + this.getId());
            count = Server.characters.size() - 1;
            try {
                outStream.writeInt(count);
                outStream.reset();
                if (debug) System.out.printf("send %s items\n", count);
                for (int i = 0; i < count; i++) {
                    Msg toClient = null;
                    try {
                        toClient = characterQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (toClient != null && toClient.id == id) {
                        continue;
                    }
                    outStream.writeObject(toClient);
                    if (debug) System.out.printf("%s :send %s\n", this.getId(), toClient);
                    outStream.reset();
                }
                // outStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (debug) System.out.println();
    }


    /**
     * Take from the enemyQueue and send to the client
     */
    public void sendEnemiesToClient() {
        int count;
        if (debug) System.out.println("sendEnemiesToClient() " + this.getId());
        count = Server.enemies.size();
        try {
            outStream.writeInt(count);
            if (debug) System.out.printf("send %s items\n", count);
            for (int i = 0; i < count; i++) {
                Msg ai = null;
                try {
                    ai = enemyQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                outStream.writeObject(ai);
                outStream.reset();
                if (debug) System.out.printf("%s :send %s\n", this.getId(), ai);
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
            outStream.flush();
            synchronized (Server.worldItems) {
                for (int i = 0; i < count; i++) {
                    ItemMsg item = Server.worldItems.get(i);
                    outStream.writeObject(item);
                    if (debug) System.out.print("send item\n");
                }
            }
             outStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (debug) System.out.println();
    }

    public int getClientId() {
        return id;
    }


    /*
    Object just used for waiting on the server thread, very important
     */
    public class PauseObject {
        /*
        !! Do not remove !!
         */
    }

}
