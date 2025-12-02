/* CEG4188 - Final Project
 * CrunchLAN Multiplayer Game
 * Launcher.java 
 * Run this class.
 * 12-03-25
 * Authors: Escalante, A., Gordon, A. 
 */
package ceg4188.grp2.main;

import javax.swing.SwingUtilities;

public class Launcher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuScreen());
    }
}
