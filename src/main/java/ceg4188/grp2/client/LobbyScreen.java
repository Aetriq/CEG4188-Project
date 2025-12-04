/* CEG4188 - Final Project
 * CrunchLAN Multiplayer Game
 * LobbyScreen.java 
 * Primary class to handle the UI for the lobby screen.
 * 12-03-25
 * Authors: Escalante, A., Gordon, A. 
 */
package ceg4188.grp2.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.PrintWriter;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ceg4188.grp2.shared.Protocol;

/**
 * Simple lobby UI (1280x720). Host toggles settings and starts game.
 * Host is first in player list.
 */
public class LobbyScreen extends JFrame {
    

    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final JTextArea log = new JTextArea();

    private final JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard", "Extreme"});
    private final JTextField duration = new JTextField("60");
    private final JTextField minCookies = new JTextField("2");
    private final JTextField maxCookies = new JTextField("6");
    private final JTextField minScore = new JTextField("1");
    private final JTextField maxScore = new JTextField("5");
    private final JTextField spawnMin = new JTextField("2000");
    private final JTextField spawnMax = new JTextField("5000");
    private final JTextField maxAllowed = new JTextField("12");
     

    private final JButton startBtn = new JButton("Start Game");
    private final PrintWriter out;
    private final String username;

    public LobbyScreen(String username, PrintWriter out) {
        this.username = username; this.out = out;
        setTitle("CrunchLAN Lobby - " + username);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(300,720));
        left.add(new JLabel("Players"), BorderLayout.NORTH);
        left.add(new JScrollPane(list), BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        // Right panel only shows the difficulty and start button.
        JPanel right = new JPanel(new GridLayout(3,1,6,6));
        right.setPreferredSize(new Dimension(320,720));

        // Add difficulty
        right.add(new JLabel("Difficulty: "));
        right.add(difficultyBox);
        difficultyBox.addActionListener(e -> updateSettingsForDifficulty());  // Listener to auto-set parameters
        difficultyBox.setSelectedIndex(1);  // Default to Medium

        // Internal fields
        JPanel hiddenPanel = new JPanel();
        hiddenPanel.add(duration); hiddenPanel.add(minCookies);
        hiddenPanel.add(maxCookies); hiddenPanel.add(minScore);
        hiddenPanel.setVisible(false);  // Hide from the UI
        right.add(hiddenPanel);

        right.add(startBtn);
        add(right, BorderLayout.EAST);

        JPanel center = new JPanel();
        center.setBackground(Color.darkGray);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        log.setEditable(false);
        bottom.add(new JScrollPane(log), BorderLayout.CENTER);
        bottom.setPreferredSize(new Dimension(1280,120));
        add(bottom, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> onStart());

        setVisible(true);
    }

    // New method to set the parameters based on difficulty.
    private void updateSettingsForDifficulty(){
        String selected = (String) difficultyBox.getSelectedItem();
        switch (selected) {
            case "Easy":
                duration.setText("60"); minCookies.setText("1"); maxCookies.setText("4"); minScore.setText("1"); 
                maxScore.setText("2"); spawnMin.setText("2000"); spawnMax.setText("4200"); maxAllowed.setText("10");
                break;
            case "Medium":
                duration.setText("50"); minCookies.setText("2"); maxCookies.setText("6"); minScore.setText("1"); 
                maxScore.setText("5"); spawnMin.setText("1200"); spawnMax.setText("3200"); maxAllowed.setText("12");
                break;
            case "Hard":
                // Reduced the time, allowded more cookies on screen ,and increase cookie spawn rate. 
                duration.setText("40"); minCookies.setText("5"); maxCookies.setText("12"); minScore.setText("3"); 
                maxScore.setText("5"); spawnMin.setText("900"); spawnMax.setText("2200"); maxAllowed.setText("18");
                break;
            case "Extreme":
                // Reduced the time even more, allowed even more cookies on screen ,and more cookie spawn rate. 
                duration.setText("25"); minCookies.setText("8"); maxCookies.setText("15"); minScore.setText("3"); 
                maxScore.setText("5"); spawnMin.setText("800"); spawnMax.setText("1900"); maxAllowed.setText("25");
                break;
        }
    }

    public void appendLog(String s){ log.append(s + "\n"); }
    public void updatePlayers(String csv){
        SwingUtilities.invokeLater(() -> {
            model.clear();
            if (csv==null || csv.isBlank()) return;
            String[] arr = csv.split(",");
            for (String a: arr) if (!a.isBlank()) model.addElement(a);
        });
    }
    public void applySettings(String line){
        SwingUtilities.invokeLater(() -> {
            String[] f = line.split(" ");
            if (f.length < 8) return;
            duration.setText(f[0]); minCookies.setText(f[1]); maxCookies.setText(f[2]);
            minScore.setText(f[3]); maxScore.setText(f[4]); spawnMin.setText(f[5]); spawnMax.setText(f[6]); maxAllowed.setText(f[7]);
        });
    }

    private void onStart(){
        // only host should see meaningful success; we still send settings
        try {
            String s = duration.getText().trim() + " " + minCookies.getText().trim() + " " + maxCookies.getText().trim() + " " +
                    minScore.getText().trim() + " " + maxScore.getText().trim() + " " + spawnMin.getText().trim() + " " + spawnMax.getText().trim() + " " + maxAllowed.getText().trim();
            out.println(Protocol.LOBBY_SETTINGS + " " + s);
            out.println(Protocol.START_GAME);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid settings");
        }
    }
}
