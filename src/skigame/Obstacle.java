// File: Obstacle.java
// Author: Denis Roman
// Date: April 4, 2020

package skigame;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Obstacle extends Entity {
	
	private int damage;
	private boolean isHit;
	private BufferedImage hitSprite;
	
	public Obstacle(BufferedImage sprite, BufferedImage hitSprite, int x, int y, float speed, int damage, boolean isHit) {
		super(sprite, x, y, speed);
		this.hitSprite = hitSprite;
		this.damage = damage;
		this.isHit = isHit;
	}
	
	public int getDamage() {
		return damage;
	}
	
	public void setDamage(int damage) {
		this.damage = damage;
	}
	
	public boolean isHit() {
		return isHit;
	}
	
	public void setIsHit(boolean isHit) {
		this.isHit = isHit;
	}
	
	// custom draw method, since we have two sprites to choose from
	@Override
	public void draw(Graphics page) {
		if (isHit) {
			drawExtSprite(page, hitSprite);
		} else {
			super.draw(page);
		}
	}
}
