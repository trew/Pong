package trew;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class Pong extends StateBasedGame {

	/** The width of the game window */
	private static int width = 800;

	/** The height of the game window */
	private static int height = 600;

	/** Whether we are playing in fullscreen or not */
	private static boolean fullscreen = false;

	/** The game title */
	private static String title = "Pong";

	/** The target frame rate */
	private static int fpsLimit = 60;


	public Pong() {
		super(title);
	}

	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		this.addState(new MenuState());
		this.addState(new GameplayState(width, height));
	}

	/**
	 * The main entry point of the game. Sets up the application and gets the
	 * game loop going.
	 *
	 * @param args
	 *            Any arguments passed
	 */
	public static void main(String[] args) {

		try {
			AppGameContainer app = new AppGameContainer(new Pong());

			app.setDisplayMode(width, height, fullscreen);
			app.setTargetFrameRate(fpsLimit);
			app.setShowFPS(false);
			app.start();
		} catch (SlickException se) {
			se.printStackTrace();
		}
	}

}
