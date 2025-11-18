package ceg4188.grp2.main;

import javax.swing.SwingUtilities;

public class Launcher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuScreen());
    }
}
