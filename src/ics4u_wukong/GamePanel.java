/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ics4u_wukong;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.Scanner;

/**
 * GamePanel
 *
 * Author: wilso
 */
public class GamePanel extends JPanel {

    private static final int GAME_WIDTH = 960;
    private static final int GAME_HEIGHT = 540;

    private static final int GROUND_Y = 450;
    private static final double GRAVITY = 0.7;
    private static final double JUMP_POWER = -12.0;
    private static final int SPEED = 4;

    // world objects
    private final Rectangle platform = new Rectangle(300, 350, 120, 20);
    // additional platform for chapter 2
    private Rectangle platform2 = new Rectangle(520, 300, 120, 20);
    private final Rectangle goal = new Rectangle(GAME_WIDTH - 120, GROUND_Y - 60, 60, 60);

    // player and physics
    private Player player;
    // verticalVelocity - how many pixels the player moves vertically
    private double verticalVelocity = 0;

    // input state booleans
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    // true when the player is standing on the ground or a platform
    private boolean isOnGround = false;

        // overlay shown before the level starts and after the player wins
        private boolean overlayVisible = true;
        private String overlayMessage = "Chapter 1 - Stone Monkey on Flower-Fruit Mountain\n"
            + "Wukong is born from a stone and becomes leader of the monkeys.\n"
            + "He decides he wants true power and will leave the mountain.\n"
            + "Objective: Collect 3 peaches to prove leadership and reach the cave exit.\n"
            + "Press Enter to start\nLeft/Right to move, Space to jump";
        //current chapter
        private int chapter = 1;
    // show overlay when a chapter was just completed
    private boolean chapterCompleteOverlay = false;
    // Chapter 2 enemy / hazard and health
    // an enemy that patrols in chapter 2
    private java.awt.Rectangle enemy = null;
    private int enemyDir = 1; // 1 = right, -1 = left
    private int enemyMinX = 300;
    private int enemyMaxX = 700;
    private int enemySpeed = 2;

    // player health for chapter 2
    private int health = 3;
    private final int MAX_HEALTH = 3;

    // Chapter 1 peaches (collect 3)
    private Rectangle[] peaches;
    private int peachesCollected = 0;

    // Chapter 2 scroll
    private Rectangle scroll = null;
    private boolean scrollCollected = false;
    private Image scrollImage = null;
    private Image guardImage = null;
    private Image handImage = null;
    private Image gateImage = null;
    // 2D tile map
    private int tileSize = 40;
    private int[][] tileMap = new int[][]{
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
    };
    // whether the current overlay should just resume the game when Enter is pressed
    private boolean overlayResumeOnly = false;

    // Chapter 3 (bossfight) (Buddha's slamming hand
    private Rectangle hand = null;
    private int handUpY = -140;
    private int handDownY = 300;
    private boolean handSlamming = false;
    private int handTimer = 0; // ticks until next slam
    private int slamIntervalTicks = 90; // about ~1.5 seconds
    private int handHoldTimer = 0;
    private int handHoldTicks = 20; // how long the hand stays down
    private int handSpeed = 14; // pixels per tick when moving

    public GamePanel() {
        setBackground(new Color(220, 235, 245));
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);

            // try loading images
            try {
                scrollImage = ImageIO.read(new java.io.File("src/ics4u_wukong/scroll.jpg"));
            } catch (Exception e) {
                scrollImage = null;
            }
            try {
                guardImage = ImageIO.read(new java.io.File("src/ics4u_wukong/guard.jpg"));
            } catch (Exception e) {
                guardImage = null;
            }
            try {
                handImage = ImageIO.read(new java.io.File("src/ics4u_wukong/hand.jpg"));
            } catch (Exception e) {
                handImage = null;
            }
            try {
                gateImage = ImageIO.read(new java.io.File("src/ics4u_wukong/gate.jpg"));
            } catch (Exception e) {
                gateImage = null;
            }

        

        // starting position
        player = new Player(100, GROUND_Y - 60);

        // keyboard handling (simple)
        /**
         * key pressed code taken from learncodefromgaming
         * @author: learncodefromgaming
         * https://learncodebygaming.com/blog/how-to-make-a-video-game-in-java-2d-basics
         */
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int kc = e.getKeyCode();
                // set pressed-state flags so updateGame() can move the player
                if (kc == KeyEvent.VK_LEFT || kc == KeyEvent.VK_A) leftPressed = true;
                if (kc == KeyEvent.VK_RIGHT || kc == KeyEvent.VK_D) rightPressed = true;
                if (kc == KeyEvent.VK_SPACE) {
                    // only jump when on the ground
                    if (isOnGround) {
                        verticalVelocity = JUMP_POWER;
                        isOnGround = false;
                    }
                }

                if (kc == KeyEvent.VK_ENTER) {
                    // Enter starts or advances when overlay is visible
                    if (overlayVisible) {
                        if (chapterCompleteOverlay) {
                            // advance to next chapter (cycle 1 -> 2 -> 3 -> 1)
                            chapterCompleteOverlay = false;
                            if (chapter == 1) chapter = 2;
                            else if (chapter == 2) chapter = 3;
                            else chapter = 1;
                            // start the next chapter immediately (reset health/layout)
                            startChapter();
                        } else if (overlayResumeOnly) {
                            // simply resume the current chapter without resetting
                            overlayVisible = false;
                            overlayResumeOnly = false;
                        } else {
                            // starting/retrying the current chapter: reset layout
                            startChapter();
                        }
                    }
                }
                // quick save/load for testing
                if (kc == KeyEvent.VK_S) {
                    saveProgress();
                }
                if (kc == KeyEvent.VK_L) {
                    loadProgress();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int kc = e.getKeyCode();
                if (kc == KeyEvent.VK_LEFT || kc == KeyEvent.VK_A) leftPressed = false;
                if (kc == KeyEvent.VK_RIGHT || kc == KeyEvent.VK_D) rightPressed = false;
            }
        });

        // game loop so graphics refresh
        Timer timer = new Timer(16, e -> {
            updateGame();
            repaint();
        });
        timer.start();
    }

    // Overloaded constructor to start at chapter
    public GamePanel(int startChapter) {
        this();
        this.chapter = startChapter;
        startChapter();
    }

    // Put the player back to the start
    // Reset the player's position and reset motion/input state
    private void resetGame() {
        player.reset(100, GROUND_Y - player.height);
        verticalVelocity = 0;
        leftPressed = rightPressed = false;
        isOnGround = false;
    }

    // Start the current chapter: initialize health and chapter-specific layout
    private void startChapter() {
        // reset player and input
        resetGame();

        // reset health
        health = MAX_HEALTH;

        // default layout for chapter 1
        if (chapter == 1) {
            platform.x = 300;
            platform.y = 350;
            platform.width = 120;

            goal.x = GAME_WIDTH - 120;
            goal.y = GROUND_Y - 60;

            // no enemy in chapter 1
            enemy = null;
            // place 3 peaches to collect in chapter 1
            peaches = new Rectangle[3];
            peaches[0] = new Rectangle(180, GROUND_Y - 40, 18, 18);
            peaches[1] = new Rectangle(platform.x + 40, platform.y - 18, 18, 18);
            peaches[2] = new Rectangle(600, GROUND_Y - 40, 18, 18);
            peachesCollected = 0;
            scroll = null;
            scrollCollected = false;
            // hide second platform in chapter 1
            platform2.y = GAME_HEIGHT + 200;
        } else if (chapter == 2) {
            // slightly different platform position for chapter 2
            platform.x = 350;
            platform.y = 360;
            platform.width = 120;

            // goal stays on the right as the player must reach it
            goal.x = GAME_WIDTH - 120;
            goal.y = GROUND_Y - 60;

            // create a enemy for chapter 2
            int enemyW = 48;
            int enemyH = 56;
            enemy = new Rectangle(400, GROUND_Y - enemyH, enemyW, enemyH);
            enemyMinX = 300;
            enemyMaxX = 700;
            enemyDir = 1;
            // show a second platform for jumps in chapter 2
            platform2.x = 520;
            platform2.y = 300;
            platform2.width = 120;
            // place the scroll on top of the second platform
            int scrollW = 48;
            int scrollH = 48;
            scroll = new Rectangle(platform2.x + (platform2.width / 2) - (scrollW/2), platform2.y - scrollH, scrollW, scrollH);
            scrollCollected = false;
            // remove peaches in chapter 2
            peaches = null;
            peachesCollected = 0;
        } else if (chapter == 3) {
            // Chapter 3 layout
            // keep ground as main floor, hide second platform
            platform.x = 200;
            platform.y = GROUND_Y - 20;
            platform.width = 120;
            platform2.y = GAME_HEIGHT + 200;

            // no patrolling enemies in chapter 3
            enemy = null;

            // clear other collectibles
            peaches = null;
            peachesCollected = 0;
            scroll = null;
            scrollCollected = false;

            // create the slamming hand in the middle of the map
            int handX = GAME_WIDTH / 2 - 70;
            hand = new Rectangle(handX, handUpY, 140, 140);
            handTimer = 0;
            handSlamming = false;
            handHoldTimer = 0;

            // place the goal (end marker) at the right
            goal.x = GAME_WIDTH - 120;
            goal.y = GROUND_Y - 60;
        }

        // hide overlay so the game starts
        overlayResumeOnly = false;
        overlayVisible = false;
    }

    // Save current progress to a file (save.txt)
    /**
    * save & load file code snippet used from stackoverflow
    * @author: stackoverflow
    * https://stackoverflow.com/questions/72022350/why-is-my-saved-file-not-being-used-in-game-when-i-run-it
    */
    private void saveProgress() {
        File saveFile = new File("src", "save.txt");
        System.out.println("Saving to: " + saveFile.getAbsolutePath());
        try (PrintWriter pw = new PrintWriter(new FileWriter(saveFile))) {
            pw.println(chapter);
            pw.println(health);
            pw.println(peachesCollected);
            pw.println(scrollCollected ? 1 : 0);
            pw.println(player.posX);
            pw.println(player.posY);
            pw.flush();
            System.out.println("Save written (bytes): " + saveFile.length());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    // Load progress from the file
    private void loadProgress() {
        File saveFile = new File("src", "save.txt");
        System.out.println("Loading from: " + saveFile.getAbsolutePath());
        if (!saveFile.exists()) {
            System.out.println("No save file found.");
            return;
        }
        try (Scanner sc = new Scanner(saveFile)) {
            int loadedChapter = sc.nextInt();
            int loadedHealth = sc.nextInt();
            int loadedPeaches = sc.nextInt();
            int loadedScroll = sc.nextInt();
            int px = sc.nextInt();
            int py = sc.nextInt();
            chapter = loadedChapter;
            startChapter();
            health = loadedHealth;
            peachesCollected = loadedPeaches;
            scrollCollected = (loadedScroll == 1);
            if (scrollCollected) scroll = null;
            player.reset(px, py);
            System.out.println("Loaded progress: chapter=" + chapter + " health=" + health + " peaches=" + peachesCollected + " scroll=" + scrollCollected);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Load failed: " + e.getMessage());
        }
    }

    // Update physics, movement and collisions
    private void updateGame() {
        if (overlayVisible) return; // paused while overlay is visible

        // horizontal movement based on pressed keys
        if (leftPressed) player.posX -= SPEED;
        if (rightPressed) player.posX += SPEED;

        // apply gravity to vertical velocity and move the player
        verticalVelocity += GRAVITY;
        player.posY += (int) verticalVelocity; // casts verticalvelocity from double to int

        // ground collision: stop falling and cahnges on-ground state
        if (player.posY + player.height >= GROUND_Y) {
            player.posY = GROUND_Y - player.height;
            verticalVelocity = 0;
            isOnGround = true;
        } else {
            isOnGround = false;
        }

        // platform collision
        if (verticalVelocity >= 0) {
            Rectangle pRect = player.getRect();
            // check main platform
            if (pRect.intersects(platform)) {
                player.posY = platform.y - player.height;
                verticalVelocity = 0;
                isOnGround = true;
            } else if (chapter == 2 && pRect.intersects(platform2)) {
                // check the second platform present in chapter 2
                player.posY = platform2.y - player.height;
                verticalVelocity = 0;
                isOnGround = true;
            }
        }
        
        // Chap 1 check peach collection
        if (chapter == 1 && peaches != null) {
            for (int i = 0; i < peaches.length; i++) {
                Rectangle pc = peaches[i];
                if (pc != null && player.getRect().intersects(pc)) {
                    peaches[i] = null; // collected
                    peachesCollected++;
                }
            }
        }

        // enemy movement and collisions
        if (chapter == 2 && enemy != null) {
            // move enemy and bounce between min/max X
            enemy.x += enemyDir * enemySpeed;
            if (enemy.x < enemyMinX) {
                enemy.x = enemyMinX;
                enemyDir = 1;
            } else if (enemy.x + enemy.width > enemyMaxX) {
                enemy.x = enemyMaxX - enemy.width;
                enemyDir = -1;
            }

            // check collision with player
            if (player.getRect().intersects(enemy)) {
                // lose a health and respawn or end chapter if health reaches 0
                health--;
                if (health <= 0) {
                    overlayVisible = true;
                    chapterCompleteOverlay = false;
                    overlayMessage = "You were defeated in Heaven!\nPress Enter to retry Chapter " + chapter;
                    overlayResumeOnly = false;
                } else {
                    // respawn player near start without resetting health
                    resetGame();
                }
                return; // skip further updates this frame when hit
            }
        }
        
        // Chap 2 check scroll pickup
        if (chapter == 2 && scroll != null && player.getRect().intersects(scroll)) {
            // pick up the scroll
            scroll = null;
            scrollCollected = true;
            // show a short overlay reminding the player to reach the gate
            overlayVisible = true;
            chapterCompleteOverlay = false;
            overlayResumeOnly = true; // pressing Enter just resumes
            overlayMessage = "You stole the scroll! Now return to the Heavenly Gate to escape.\nPress Enter to continue";
            return;
        }

        // Chap 3 update slamming hand hazard
        if (chapter == 3 && hand != null) {
            handTimer++;
            // start slamming after the interval
            if (!handSlamming) {
                if (handTimer >= slamIntervalTicks) {
                    handSlamming = true;
                    handTimer = 0;
                }
            }

            if (handSlamming) {
                // move hand down quickly
                if (hand.y < handDownY) {
                    hand.y += handSpeed;
                    if (hand.y > handDownY) hand.y = handDownY;
                } else {
                    // hold down for a short time
                    handHoldTimer++;
                    if (handHoldTimer >= handHoldTicks) {
                        handSlamming = false;
                        handHoldTimer = 0;
                    }
                }
            } else {
                // move hand back up slowly
                if (hand.y > handUpY) {
                    hand.y -= Math.max(2, handSpeed / 2);
                    if (hand.y < handUpY) hand.y = handUpY;
                }
            }

            // check collision with player
            if (hand.intersects(player.getRect())) {
                health--;
                if (health <= 0) {
                    overlayVisible = true;
                    chapterCompleteOverlay = false;
                    overlayMessage = "A great power stopped you!\nPress Enter to retry Chapter " + chapter;
                    overlayResumeOnly = false;
                } else {
                    resetGame();
                }
                return; // skip further updates this frame when hit
            }
        }

        // goal check -> show overlay for win and present next story/dialogue
        if (player.getRect().intersects(goal)) {
            if (chapter == 1) {
                if (peachesCollected >= 3) {
                    overlayVisible = true;
                    chapterCompleteOverlay = true;
                    overlayResumeOnly = false;
                    overlayMessage = "Chapter 1 Complete!\nI'm not satisfied. I want true power. I'm going to leave the mountain.\n\nChapter 2 - Havoc in Heaven:\nWukong enters Heaven and is offered a small job and is treated like he is not important. He gets angry and causes trouble.\nObjective: Reach the Heavenly Gate and steal the scroll.\nPress Enter to continue";
                } else {
                    overlayVisible = true;
                    chapterCompleteOverlay = false;
                    int remain = 3 - peachesCollected;
                    overlayResumeOnly = true;
                    overlayMessage = "You need to collect " + remain + " more peaches to prove leadership.\nCollect them and return to the cave exit.";
                }
            } else if (chapter == 2) {
                // in chapter 2 the player must have stolen the scroll and then reach the gate
                if (scrollCollected) {
                    overlayVisible = true;
                    chapterCompleteOverlay = true;
                    overlayResumeOnly = false;
                    overlayMessage = "Chapter 2 Complete!\nThey thought they could control me. I proved them wrong... but now they're angry.\nPress Enter to continue";
                } else if (scroll != null) {
                    overlayVisible = true;
                    chapterCompleteOverlay = false;
                        overlayResumeOnly = true;
                        overlayMessage = "The scroll is guarded. Steal the scroll from the second platform to complete the chapter.";
                } else {
                    overlayVisible = true;
                    chapterCompleteOverlay = false;
                        overlayResumeOnly = true;
                        overlayMessage = "You must steal the scroll and return to the Gate to escape.";
                }
            } else if (chapter == 3) {
                overlayVisible = true;
                chapterCompleteOverlay = true;
                overlayResumeOnly = false;
                overlayMessage = "Chapter 3 Complete!\nI was powerful... but not wise. I'll learn control and use my strength properly.\nPress Enter to continue";
            } else {
                overlayVisible = true;
                chapterCompleteOverlay = true;
                overlayMessage = "Chapter Complete!\nPress Enter to continue";
            }
        }

        // falling off the bottom
        if (player.posY > GAME_HEIGHT) {
            overlayVisible = true;
            chapterCompleteOverlay = false;
            overlayResumeOnly = false;
            overlayMessage = "You fell off the world!\nPress Enter to retry Chapter " + chapter;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw tile map
        if (tileMap != null && tileMap.length > 0) {
            for (int r = 0; r < tileMap.length; r++) {
                for (int c = 0; c < tileMap[r].length; c++) {
                    if (tileMap[r][c] == 1) {
                        g.setColor(new Color(200, 200, 200));
                        g.fillRect(c * tileSize, 30 + r * tileSize, tileSize, tileSize);
                        g.setColor(Color.DARK_GRAY);
                        g.drawRect(c * tileSize, 30 + r * tileSize, tileSize, tileSize);
                    }
                }
            }
        }

        // draw ground
        g.setColor(new Color(70, 140, 90));
        g.fillRect(0, GROUND_Y, GAME_WIDTH, GAME_HEIGHT - GROUND_Y);

        // draw platform
        g.setColor(new Color(140, 90, 50));
        g.fillRect(platform.x, platform.y, platform.width, platform.height);

        // draw goal
        g.drawImage(gateImage, goal.x, goal.y, goal.width, goal.height, null);

        // draw player
        player.draw(g);

        // draw second platform for chapter 2
        if (chapter == 2) {
            g.setColor(new Color(140, 90, 50));
            g.fillRect(platform2.x, platform2.y, platform2.width, platform2.height);
        }

        // draw Chapter 1 peaches (no text label)
        if (chapter == 1 && peaches != null) {
            g.setColor(new Color(255, 165, 0)); // peach color
            for (Rectangle pc : peaches) {
                if (pc != null) {
                    g.fillOval(pc.x, pc.y, pc.width, pc.height);
                }
            }
        }

        // draw Chapter 2 scroll collectible
        if (chapter == 2 && scroll != null) {
            g.drawImage(scrollImage, scroll.x, scroll.y, scroll.width, scroll.height, null);
        }

        // draw chapter 2 enemy/hazard
        if (chapter == 2 && enemy != null) {
            g.drawImage(guardImage, enemy.x, enemy.y, enemy.width, enemy.height, null);
        }

        // draw Chapter 3 slamming hand
        if (chapter == 3 && hand != null) {
            g.drawImage(handImage, hand.x, hand.y, hand.width, hand.height, null);
        }

        // draw health
        g.setColor(Color.BLACK);
        g.drawString("Health:", 20, 30);
        for (int i = 0; i < MAX_HEALTH; i++) {
            if (i < health) g.setColor(Color.RED);
            else g.setColor(Color.GRAY);
            int hx = 90 + i * 22;
            int hy = 16;
            g.fillRect(hx, hy, 18, 12);
            g.setColor(Color.BLACK);
            g.drawRect(hx, hy, 18, 12);
        }

        // draw peaches counter
        if (chapter == 1) {
            g.setColor(Color.BLACK);
            g.drawString("Peaches: " + peachesCollected + "/3", 20, 50);
        }

        // draw controls box (top-right)
        {
            int boxWidth = 240;
            int boxHeight = 110;
            int boxX = GAME_WIDTH - boxWidth - 20;
            int boxY = 10;
            g.setColor(new Color(0, 0, 0, 140));
            g.fillRect(boxX, boxY, boxWidth, boxHeight);
            g.setColor(new Color(255, 255, 255, 220));
            g.drawRect(boxX, boxY, boxWidth, boxHeight);
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(12f));
            int tx = boxX + 10;
            int ty = boxY + 20;
            g.drawString("Controls:", tx, ty);
            ty += 18;
            g.drawString("Left/Right: Arrow keys or A/D", tx, ty);
            ty += 16;
            g.drawString("Space: Jump", tx, ty);
            ty += 16;
            g.drawString("Enter: Start / Continue", tx, ty);
            ty += 16;
            g.drawString("S: Save   L: Load", tx, ty);
        }

        // overlay (start / win)
        if (overlayVisible) {
            // draw a semi-transparent rectangle as the background
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(50, 80, GAME_WIDTH - 100, GAME_HEIGHT - 160);

            // use white text with a larger font
            g.setColor(java.awt.Color.WHITE);
            g.setFont(g.getFont().deriveFont(20f));

            // draw each line
            String[] lines = overlayMessage.split("\\n");
            int textX = 70;
            int textY = 140; 
            for (String line : lines) {
                g.drawString(line, textX, textY);
                textY += 30; // next line lower down
            }
        }
    }

}

