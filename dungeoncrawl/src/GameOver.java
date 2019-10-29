import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class GameOver extends BasicGameState {
    private int tilesize;

    @Override
    public int getID() {
        return Main.GAMEOVER;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        Main dtc = (Main) game;
    }


    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Main dtc = (Main) game;
//        g.drawImage(ResourceManager.getImage(Game.GAMEOVER_IMG), 0, 0);
    }


    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Main dtc = (Main) game;
        Cheats.enableCheats(dtc, input);
    }


}
