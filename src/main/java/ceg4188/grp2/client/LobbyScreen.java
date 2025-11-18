package ceg4188.grp2.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.PrintWriter;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
        setTitle("Lobby - " + username);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(300,720));
        left.add(new JLabel("Players"), BorderLayout.NORTH);
        left.add(new JScrollPane(list), BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new GridLayout(10,1,6,6));
        right.setPreferredSize(new Dimension(320,720));
        right.add(new JLabel("Duration (s)")); right.add(duration);
        right.add(new JLabel("Min Cookies")); right.add(minCookies);
        right.add(new JLabel("Max Cookies")); right.add(maxCookies);
        right.add(new JLabel("Min Score")); right.add(minScore);
        right.add(new JLabel("Max Score")); right.add(maxScore);
        right.add(new JLabel("Spawn Min ms")); right.add(spawnMin);
        right.add(new JLabel("Spawn Max ms")); right.add(spawnMax);
        right.add(new JLabel("Max Allowed")); right.add(maxAllowed);
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
