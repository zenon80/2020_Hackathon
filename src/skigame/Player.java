package skigame;

import java.awt.image.BufferedImage;

public class Player extends Entity {
	
	private int health;
	private float score;
	
	public Player(BufferedImage sprite, int x, int y, float speed, int health, float score) {
		super(sprite, x, y, speed);
		this.health = health;
		this.score = score;
	}
	
	public int getHealth() {
		return health;
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
	
	public void damage(int damage) {
		health -= damage;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public void addScore(float points) {
		score += points;
	}
}
