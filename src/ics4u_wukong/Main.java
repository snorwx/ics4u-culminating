/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ics4u_wukong;

import javax.swing.JFrame;
/**
 *
 * @author wilso
 */
public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Wukong");

        window.setSize(960, 540);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel panel = new GamePanel();
        window.add(panel);

        window.setVisible(true);

        // important: click the window once if keys don't work
        panel.requestFocusInWindow();
    }
}
