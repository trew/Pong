package trew;

import java.util.Random;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

/**
 * A classic game of Pong.
 * 
 * @author Samuel Andersson
 *
 */
public class Pong extends BasicGame {

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
	
	/** The player's paddle */
	private Rectangle paddlePlayer;
	
	/** The computer's paddle */
	private Rectangle paddleCPU;
	
	/** The ball */
	private Circle ball;
	
	/** The velocity of the ball */
	private Vector2f ballVelocity;
	
	/** The player's score */
	private int scorePlayer;
	
	/** The computer's score */
	private int scoreCPU;
	
	/** Who is going to receive the serve?
	 * 0 = None
	 * 1 = Player
	 * 2 = Computer
	 */
	private int serveReceiver;
	private static final int NONE = 0;
	private static final int PLAYER = 1;
	private static final int COMPUTER = 2;
	
	/** The move speed of the paddles */ 
	private float moveSpeed = 6.0f;
	
	/** The max angle for the ball when bouncing of a paddle */
	private static final float MAXBOUNCEANGLE = 75.0f;
	
	/** The max speed of the ball */
	private static final float BALLSPEED = 10.0f;
	
	/**
	 * The time for cooldown between ball collisions 
	 * A very short cooldown to fix a bug where the ball
	 * would be stuck between the wall and the paddle 
	 */
	private long collisionCooldown;
	
	/** The collision cooldown time in milliseconds */
	private static final int COLLISIONCOOLDOWNTIME = 50;

	/** Indicate whether the game is paused or not */
	private boolean paused = false;

	/** Whether we're waiting for a key press */
	private boolean waitingForKeyPress = true;

	/** A message to display */
	private String message = "Press space to start";
	
	/** A randomizer */
	private Random randomizer;
	
	/**
	 * Construct a new game of Pong
	 */
	public Pong() {
		super(title);
	}

	private void startGame() {
		paddlePlayer.setLocation(5, height / 2 - 40);
		paddleCPU.setLocation(width - 15, height / 2 - 40);
		ball.setLocation(width / 2, height / 2);
		ballVelocity = new Vector2f(0, 0);
		serveReceiver = PLAYER; //player
		scoreCPU = 0;
		scorePlayer = 0;
	}

	/**
	 * Initialize the game.
	 */
	@Override
	public void init(GameContainer container) throws SlickException {
		container.getInput().enableKeyRepeat();
		paddlePlayer = new RoundedRectangle(5, height / 2 - 40, 10, 80, 3);
		paddleCPU = new RoundedRectangle(width - 15, height / 2 - 40, 10, 80, 3);
		ball = new Circle(width / 2, height / 2, 6);
		ballVelocity = new Vector2f(-3, 1);
		serveReceiver = PLAYER; //player
		randomizer = new Random();
		startGame();
	}

	/**
	 * Serve the ball in the direction of the serveReceiver.
	 * The ball will be launched from somewhere between 1/4
	 * to 3/4 of the window height. It will have a random
	 * speed between 0.5*BALLSPEED and 1*BALLSPEED.  
	 */
	private void serve() {
		ball.setCenterX(width / 2);
		// randomize a location from 1/4 to 3/4 of the total height
		ball.setCenterY(height - height / 4 - randomizer.nextInt(height / 2));
		// random between 1 and 2
		float divider = (float)randomizer.nextDouble() + 1.0f;
		ballVelocity.x = serveReceiver == PLAYER ? -BALLSPEED / divider : BALLSPEED / divider;
		ballVelocity.y = 1;
		serveReceiver = NONE;
	}
	
	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {
		
		if (container.getInput().isKeyPressed(Input.KEY_P)) {
			paused = !paused;
		}
		
		if (waitingForKeyPress) {
			if (container.getInput().isKeyPressed(Input.KEY_SPACE)) {
				waitingForKeyPress = false;
				startGame();
			} else {
				return;
			}
		}

		if (paused) {
			return;
		}
		
		// serve?
		if (serveReceiver != NONE) {
			serve();
		}
		
		// get input and move player paddle
		if (container.getInput().isKeyDown(Input.KEY_UP)) {
			if (paddlePlayer.getMinY() > moveSpeed)
				paddlePlayer.setY(paddlePlayer.getY() - moveSpeed);
			else if (paddlePlayer.getMinY() > 0) {
				paddlePlayer.setY(0);
			}
		} else if (container.getInput().isKeyDown(Input.KEY_DOWN)) {
			if (paddlePlayer.getMaxY() < height - moveSpeed)
				paddlePlayer.setY(paddlePlayer.getY() + moveSpeed);
			else if (paddlePlayer.getMaxY() < height) {
				paddlePlayer.setY(height - paddlePlayer.getHeight());
			}
		}
		
		// move computer paddle
		int distanceToBallY = (int)Math.abs(paddleCPU.getCenterY() - ball.getCenterY());
		if (distanceToBallY > moveSpeed) {
			if (paddleCPU.getCenterY() < ball.getCenterY()) {
				paddleCPU.setY(paddleCPU.getY() + moveSpeed); 
			} else {
				paddleCPU.setY(paddleCPU.getY() - moveSpeed); 
			}
		} else {
			paddleCPU.setCenterY(ball.getCenterY());
		}
			
		// move ball
		ball.setLocation(ball.getX() + ballVelocity.getX(), ball.getY() + ballVelocity.getY());
		
		// detect score
		if (ball.getMinX() <= 0) {
			scoreCPU++;
			serveReceiver = COMPUTER;
			if (scoreCPU >= 10) {
				notifyLoss();
			}
		} else if (ball.getMaxX() >= width) {
			scorePlayer++;
			serveReceiver = PLAYER;
			if (scorePlayer >= 10) {
				notifyWin();
			}
		}
		
		// bounce against top and bottom walls
		if (ball.getMinY() < 0 && ballVelocity.y < 0) {
			ballVelocity.y = -ballVelocity.y;
		} else if (ball.getMaxY() > height && ballVelocity.y > 0) {
			ballVelocity.y = -ballVelocity.y;
		}
		
		// bounce against paddles
		if (collisionCooldown > 0) {
			collisionCooldown -= delta;
		} else {
			if (ball.intersects(paddlePlayer)) {
				bounceOnPaddle(paddlePlayer);
				collisionCooldown = COLLISIONCOOLDOWNTIME;
			} else if (ball.intersects(paddleCPU)){
				bounceOnPaddle(paddleCPU);
				collisionCooldown = COLLISIONCOOLDOWNTIME;
			}
		}
	}

	/**
	 * Calculate the direction of the ball when bouncing on
	 * a paddle
	 * 
	 * @param paddle The paddle hitting the ball
	 */
	private void bounceOnPaddle(Shape paddle) {
		// get the distance from the center of the paddle to the center of the ball
		// this ranges from (-PaddleHeight / 2) to (PaddleHeight / 2).
		float distanceToPaddleCenterY = (paddle.getCenterY() - ball.getCenterY());
		
		// get a normalized value, ranges from -1 to 1, making Paddle Height 
		// trivial
		float normalizedY = distanceToPaddleCenterY / (paddle.getHeight() / 2);
		if (normalizedY > 1) normalizedY = 1;
		if (normalizedY < -1) normalizedY = -1;
		
		// get the angle which the ball will fly
		// ranges from -MAXBOUNCEANGLE to MAXBOUNCEANGLE
		double bounceAngle = Math.toRadians(normalizedY * MAXBOUNCEANGLE);
		
		// calculate the horizontal speed based on where on the paddle
		// we hit. Ranges from 0.7 to 1.0.
		float ballVx = Math.abs((float)Math.sin(bounceAngle));
		if (ballVx > 0 && ballVx < 0.7f) {
			ballVx = 0.7f;
		}
		
		// Go left if we're hitting the paddle on the right hand side
		if (ballVelocity.getX() > 0) {
			ballVx = -ballVx; 
		}
		// Calculate the vertical speed based on where on the paddle we hit.
		// Since bounce angle is -75 to 75, it will range from -0.9659~ to 0.9659
		float ballVy = (float)-Math.sin(bounceAngle);

		// multiply with the ball speed
		ballVelocity.x = ballVx * BALLSPEED;
		ballVelocity.y = ballVy * BALLSPEED;
	}

	private void notifyLoss()
	{
		waitingForKeyPress = true;
		message = "You lost. Press space to play again";
	}

	private void notifyWin()
	{
		waitingForKeyPress = true;
		message = "You win! Press space to play again";
	}
		
	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		g.fill(ball);
		g.fill(paddleCPU);
		g.fill(paddlePlayer);
		
		// center the score on top
		String scoreString = scorePlayer + " - " + scoreCPU;
		int scoreStringWidth = container.getDefaultFont().getWidth(scoreString);
		g.drawString(scoreString, width / 2- scoreStringWidth / 2, 5);

		if (waitingForKeyPress) {
			int messageWidth = container.getDefaultFont().getWidth(message);
			g.drawString(message, width / 2 - messageWidth / 2, height - height / 3);
		}
	}

	/**
	 * The main entry point of the game. Sets up the
	 * application and gets the game loop going.
	 * 
	 * @param args Any arguments passed
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
