// File: Entity.java
// Author: Denis Roman
// Date: April 4, 2020

package skigame;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Entity {
	
	private BufferedImage sprite;
	private int posX;
	private int posY;
	private float speed;
	
	public Entity(BufferedImage sprite, int x, int y, float speed) {
		this.sprite = sprite;
		posX = x;
		posY = y;
		this.speed = speed;
	}
	
	public int getX() {
		return posX;
	}
	
	public int getY() {
		return posY;
	}
	
	public void setX(int x) {
		posX = x;
	}
	
	public void setY(int y) {
		posY = y;
	}
	
	public void moveX(int x) {
		posX += x;
	}
	
	public void moveY(int y) {
		posY += y;
	}
	
	public int getHeight() {
		return sprite.getHeight();
	}
	
	public int getWidth() {
		return sprite.getWidth();
	}
	
	public int getLeftBound() {
		return posX - (sprite.getWidth() / 2);
	}
	
	public int getRightBound() {
		return posX + (sprite.getWidth() / 2);
	}
	
	public int getTopBound() {
		return posY - (sprite.getHeight() / 2);
	}
	
	public int getBottomBound() {
		return posY + (sprite.getHeight() / 2);
	}
	
	public void draw(Graphics page) {
		int xDrawPosition = getLeftBound();
		int yDrawPosition = getTopBound();
		page.drawImage(sprite, xDrawPosition, yDrawPosition, null);
	}
	
	// draw with external sprite
	public void drawExtSprite(Graphics page, BufferedImage sprite) {
		int xDrawPosition = getLeftBound();
		int yDrawPosition = getTopBound();
		page.drawImage(sprite, xDrawPosition, yDrawPosition, null);
	}
	
	// rectangular bounds collision
	public boolean collidesWith(Entity other) {
		return getRightBound() >= other.getLeftBound()
				&& getLeftBound() <= other.getRightBound()
				&& getBottomBound() >= other.getTopBound()
				&& getTopBound() <= other.getBottomBound();
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
}
