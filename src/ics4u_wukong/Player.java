/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ics4u_wukong;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.imageio.ImageIO;

/**
 * Player used for position, drawing and collision
 *
 * Author: wilso
 */
public class Player {
    // position (top-left) and size in pixels
    // use clear names: posX/posY for position, width/height for size
    public int posX, posY;
    public int width = 48;
    public int height = 60;

    // optional sprite image
    private Image sprite;

    /**
     * Create a player at the given starting coordinates (top-left in pixels).
     */
    public Player(int startX, int startY) {
        posX = startX;
        posY = startY;
        // try loading sprite from classpath resources; fallback to null on failure
        try {
            sprite = ImageIO.read(new java.io.File("src/ics4u_wukong/image.png"));
        } catch (Exception e) {
            sprite = null;
        }
    }

    /**
     * Draw the player
     */
    public void draw(Graphics g) {
        g.drawImage(sprite, posX, posY, width, height, null);
    }

    /**
     * Return the player's rectangle (like its hitbox)
     */
    public Rectangle getRect() {
        return new Rectangle(posX, posY, width, height);
    }

    /**
     * Reset the player's position to the bottom left (top-left in pixels).
     */
    public void reset(int startX, int startY) {
        posX = startX;
        posY = startY;
    }
}
