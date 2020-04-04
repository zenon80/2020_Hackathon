// File: SkiGame.java
// Author: Denis Roman
// Date: April 4, 2020

package skigame;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;

public class SkiGame extends JFrame implements KeyListener {
	
	private static final int SCREEN_WIDTH = 640;
	private static final int SCREEN_HEIGHT = 480;
	private static final int MAX_FRAMES = 60;
	
	// game data
	Player player;
	private static final int PLAYER_STARTING_HEALTH = 100;
	private static final int PLAYER_STARTING_X = SCREEN_WIDTH / 2;
	private static final int PLAYER_STARTING_Y = SCREEN_HEIGHT - 40;
	private static final float PLAYER_STARTING_SPEED = 0.5f;

	private long currentTime;
	private long lastTime;
	private long lastObstacleTime;
	private long startTime;
	
	private boolean userWantsToQuit;
	private boolean hasLost;
	
	ArrayList<Obstacle> obstacles;
	private long obstacleTime;
	private float obstacleSpeed;
	
	private static final long MILESTONE_INTERVAL = 100;
	private static final float OBSTACLE_STARTING_SPEED = 0.3f;
	private static final float OBSTACLE_SPEED_INCREASE = 0.05f; // linear increase in obstacle speed
	private static final float OBSTACLE_SPEED_CAP = 1.0f;
	private static final long OBSTACLE_STARTING_TIME = 900;
	private static final float OBSTACLE_TIME_FACTOR = 0.8f; // exponential increase in obstacles
	private long lastScoreMilestone;
	
	private static final long STARTING_DURATION = 3000; // how long the instruction will be displayed
	
	// "controls"
	private boolean moveLeft;
	private boolean moveRight;
	private boolean moveUp;
	private boolean moveDown;
	
	
	
	private BufferedImage playerSprite;
	private BufferedImage obstacleSprite;
	private BufferedImage obstacleHitSprite;
	
	private Canvas canvas;
	// double-buffered rendering
	private BufferStrategy bufferStrat;
	private Graphics bufferStratSurface;
	
	private BufferedImage drawBuffer;
	private Graphics drawSurface;
	
	
	public SkiGame() {
		super("Mt. Shasta Ski Game");
		
		if (loadMedia()) {
			// set up window and rendering
			setIgnoreRepaint(true);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			canvas = new Canvas();
			canvas.setIgnoreRepaint(true);
			canvas.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
			
			add(canvas);
			pack();
			canvas.requestFocus();
			setVisible(true);
			
			canvas.createBufferStrategy(2);
			bufferStrat = canvas.getBufferStrategy();
			
			drawBuffer = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			drawSurface = drawBuffer.createGraphics();
			
			
			// set up game
			player = new Player(playerSprite, PLAYER_STARTING_X, PLAYER_STARTING_Y, PLAYER_STARTING_SPEED, PLAYER_STARTING_HEALTH, 0);
			
			moveLeft = false;
			moveRight = false;
			moveUp = false;
			moveDown = false;
			
			canvas.addKeyListener(this);
			
			// start game
			lastScoreMilestone = 0;
			obstacleSpeed = OBSTACLE_STARTING_SPEED;
			obstacleTime = OBSTACLE_STARTING_TIME;
			userWantsToQuit = false;
			hasLost = false;
			obstacles = new ArrayList<Obstacle>();
			lastObstacleTime = System.currentTimeMillis();
			lastTime = lastObstacleTime;
			startTime = lastTime;
			mainLoop();
		} else {
			System.err.println("Terminating...");
			System.exit(0);
		}
	}
	
	private void mainLoop() {
		while (!userWantsToQuit) {
			currentTime = System.currentTimeMillis();
			long delta = currentTime - lastTime;
			
			if (hasLost) {
				
				// overlay on last rendered screen
				drawSurface.setColor(Color.black);
				drawSurface.drawString("GAME OVER", SCREEN_WIDTH / 2 - 40, SCREEN_HEIGHT / 2);
				drawSurface.drawString("FINAL SCORE: " + (long)player.getScore(), SCREEN_WIDTH / 2 - 40, (SCREEN_HEIGHT / 2) + 15);
				drawSurface.drawString("Press space to reset.", SCREEN_WIDTH / 2 - 40, (SCREEN_HEIGHT / 2) + 45);
				
			} else {
				// user input
				if (moveLeft && (player.getLeftBound() > 0)) {
					player.moveX(-(int)(delta * player.getSpeed()));
				}
				if (moveRight && (player.getRightBound() < SCREEN_WIDTH - 1)) {
					player.moveX((int)(delta * player.getSpeed()));
				}
				if (moveUp && (player.getTopBound() > 0)) {
					player.moveY(-(int)(delta * player.getSpeed()));
				}
				if (moveDown && (player.getBottomBound() < SCREEN_HEIGHT - 1)) {
					player.moveY((int)(delta * obstacleSpeed)); // can't move backwards faster than the obstacles
				}
				
				////// game logic
				// obstacle logic
				
				// difficulty calculations
				if (player.getScore() - lastScoreMilestone > MILESTONE_INTERVAL) {
					lastScoreMilestone = (long)player.getScore() - ((long)player.getScore() % MILESTONE_INTERVAL);
					
					// time to INCREASE the difficulty
					obstacleSpeed += OBSTACLE_SPEED_INCREASE;
					if (obstacleSpeed > OBSTACLE_SPEED_CAP) {
						obstacleSpeed = OBSTACLE_SPEED_CAP;
					}
					// update speeds of all obstacles
					for (int i = 0; i < obstacles.size(); i++) {
						obstacles.get(i).setSpeed(obstacleSpeed);
					}
					
					obstacleTime *= OBSTACLE_TIME_FACTOR;
				}
				
				// is it time to add another obstacle?
				if (currentTime - startTime > STARTING_DURATION && currentTime - lastObstacleTime > obstacleTime) {
					obstacles.add(new Obstacle(obstacleSprite, obstacleHitSprite,
							getIntRanged((obstacleSprite.getWidth() / 2), SCREEN_WIDTH - (obstacleSprite.getWidth() / 2)),
							-(obstacleSprite.getHeight() / 2),
							obstacleSpeed, 20, false));
					lastObstacleTime = currentTime;
				}
				
				// move obstacles down
				// reverse order so removing elements from array list doesn't affect future iterations
				for (int i = obstacles.size() - 1; i >= 0; i--) {
					Obstacle current = obstacles.get(i);
					current.moveY((int)(delta*current.getSpeed()));
					
					// cull out-of-view obstacles
					if (current.getY() - current.getHeight() > SCREEN_HEIGHT) {
						obstacles.remove(i);
					}
					
					// check for collision with player
					if (current.collidesWith(player)) {
						if (!current.isHit()) {
							
							// audio
							Clip hitClip;
							try {
								hitClip = AudioSystem.getClip();
								hitClip.open(AudioSystem.getAudioInputStream(new File("media/hit.wav")));
								hitClip.start();
							} catch (Exception e) {
								// just play no sound
							}
							
							player.damage(current.getDamage());
							current.setIsHit(true);
						}
					}
				}
				
				// scoring	
				player.addScore(((float)(delta)) / 100);
				
				// losing
				if (player.getHealth() <= 0) {
					hasLost = true;
				}
				
				/////// rendering
				render(drawSurface);
			}
			
			
			bufferStratSurface = bufferStrat.getDrawGraphics();
			bufferStratSurface.drawImage(drawBuffer, 0, 0, null);
			bufferStrat.show();
			
			if (bufferStratSurface != null) bufferStratSurface.dispose();
			
			// cap framerate
			if (1000 / ((float) MAX_FRAMES) > currentTime - lastTime) {
				try {
					Thread.sleep((long) ((1000 / ((float) MAX_FRAMES)) - (currentTime - lastTime)));
				} catch (InterruptedException e) {
					// then don't sleep
				}
			}
				
			lastTime = currentTime;
		}
	}
	
	private boolean loadMedia() { // returns true on success
		
		// note: audio loaded in real time
		// this method only checks to make sure the audio files exists
		
		// player
		try {
			playerSprite = ImageIO.read(new File("media/player.png"));
		} catch (IOException e) {
			System.err.println("Cannot load player sprite!");
			return false;
		}
		
		// obstacle
		try {
			obstacleSprite = ImageIO.read(new File("media/obstacle.png"));
		} catch (IOException e) {
			System.err.println("Cannot load obstacle sprite!");
			return false;
		}
		
		// obstacle_hit
		try {
			obstacleHitSprite = ImageIO.read(new File("media/obstacle_hit.png"));
		} catch (IOException e) {
			System.err.println("Cannot load obstacle_hit sprite!");
			return false;
		}
		
		File hitAudio = new File("media/hit.wav");
		if (!hitAudio.exists()) {
			System.err.println("Unable to locate hit.wav!");
			return false;
		}
		
		// successful at this point
		return true;
	}
	
	private void render(Graphics page)  {
		// clear page
		page.setColor(Color.white);
		page.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		
		// draw player
		player.draw(page);
		
		// draw obstacles
		for (int i = 0; i < obstacles.size(); i++) {
			obstacles.get(i).draw(page);
		}
		
		// draw HUD
		page.setColor(Color.black);
		page.drawString("Health: " + player.getHealth(), 5, 10);
		page.drawString("Score: " + (long)player.getScore(), 5, 25);
		
		if (currentTime - startTime <= STARTING_DURATION) {
			page.drawString("Mt. Shasta Ski Game", SCREEN_WIDTH / 2 - 50, SCREEN_HEIGHT / 2 - 30);
			page.drawString("Avoid the trees!", SCREEN_WIDTH / 2 - 50, SCREEN_HEIGHT / 2);
			page.drawString("Move with WASD or arrow keys.", SCREEN_WIDTH / 2 - 50, SCREEN_HEIGHT / 2 + 15);
		}
	}
	
	
	// keyboard input
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_A:
				moveLeft = true;
				break;
			
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_D:
				moveRight = true;
				break;
			
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W:
				moveUp = true;
				break;
			
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S:
				moveDown = true;
				break;
			
			case KeyEvent.VK_SPACE:
				if (hasLost) {
					// reset game
					player.setSpeed(PLAYER_STARTING_SPEED);
					player.setHealth(PLAYER_STARTING_HEALTH);
					player.setScore(0);
					player.setX(PLAYER_STARTING_X);
					player.setY(PLAYER_STARTING_Y);
					
					obstacles.clear();
					lastScoreMilestone = 0;
					obstacleSpeed = OBSTACLE_STARTING_SPEED;
					obstacleTime = OBSTACLE_STARTING_TIME;
					
					lastObstacleTime = System.currentTimeMillis();
					lastTime = lastObstacleTime;
					startTime = lastTime;
					
					hasLost = false;
				}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			moveLeft = false;
			break;
		
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			moveRight = false;
			break;
		
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			moveUp = false;
			break;
		
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			moveDown = false;
			break;
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	
	private int getIntRanged(int lower, int upper) {
		Random randomGen = new Random();
		return randomGen.nextInt((upper - lower) + 1) + lower;
	}
}
