/* Client
 *
 */

import org.newdawn.slick.*;

import java.io.*;
import java.net.*;



public class Client extends BasicGame {
    private final int ScreenWidth;
    private final int ScreenHeight;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String message;
    private Socket socket;
    private boolean keyPressed;
    private String keystroke;

    public Client(String title, int width, int height, DataInputStream dis, DataOutputStream dos){
        super(title);
        ScreenWidth = width;
        ScreenHeight = height;
        this.dis = dis;
        this.dos = dos;

    }

    @Override
    public void init(GameContainer container) throws SlickException {

    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.drawString("Server's Response: "+message,ScreenWidth/2f-100, ScreenHeight/2f);
        g.drawString("keyPressed: " + keyPressed, ScreenWidth/2f-100,ScreenHeight/2f+30);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        Input input = container.getInput();
        try{
            if(input.isKeyPressed(Input.KEY_W)){
                dos.writeUTF("W");
                keyPressed = true;
            }
            if(input.isKeyPressed(Input.KEY_S)){
                dos.writeUTF("S");
                keyPressed = true;
            }
            if(input.isKeyPressed(Input.KEY_A)){
                dos.writeUTF("A");
                keyPressed = true;
            }
            if(input.isKeyPressed(Input.KEY_D)){
                dos.writeUTF(("D"));
                keyPressed = true;
            }
            if(input.isKeyPressed(Input.KEY_X)){
                dos.writeUTF("Exit");
                System.out.println("Closing this connection.");
                socket.close();
                dis.close();
                dos.close();
                System.out.println("Connection closed.");
                System.exit(0);
            }
            if(keyPressed){
                message = dis.readUTF();
                keyPressed = false;
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }
    public static void main(String [] args){
        Socket socket;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            byte [] ipAddr = new byte[] {127,0,0,1};

            // getting localhost ip
            InetAddress ip = InetAddress.getByAddress(ipAddr);

            // establish the connection with server port 5000
            socket = new Socket(ip, 5000);

            // obtaining input and out streams
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

        } catch(Exception e){
            e.printStackTrace();
        }
        AppGameContainer app;
        try {
            app = new AppGameContainer(new Client("Client/Server Demo", 1280,768,
            dis,dos));
            app.setDisplayMode(1280, 768, false);
            app.setVSync(true);
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }

    }

}
