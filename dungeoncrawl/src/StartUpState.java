import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class StartUpState extends BasicGameState {
    @Override
    public int getID() {
        return Main.STARTUPSTATE;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game){
        Main dc = (Main) game;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
//        g.drawImage(ResourceManager.getImage(Game.STARTUP_BANNER), 0, 0);

    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Main dc = (Main) game;
        dc.enterState(Main.LEVEL1);
        if (input.isKeyPressed(Input.KEY_SPACE)) {
            dc.enterState(Main.LEVEL1);
        }
        Cheats.enableCheats(dc, input);
    }
}
