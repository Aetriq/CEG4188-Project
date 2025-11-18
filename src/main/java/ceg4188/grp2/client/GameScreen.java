package ceg4188.grp2.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class GameScreen extends JFrame {
    private final GamePanel panel;
    private final JLabel totalLabel = new JLabel("Score: 0");
    private final JTextArea log = new JTextArea();
    private String username = "Player";

    public GameScreen() {
        setTitle("Cookie Clicker - Game");
        setSize(1280,720);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        panel = new GamePanel(this);
        add(panel, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setPreferredSize(new Dimension(300,720));
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        right.add(totalLabel, BorderLayout.NORTH);

        log.setEditable(false);
        right.add(new JScrollPane(log), BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        setVisible(true);
    }

    public GamePanel getGamePanel(){ return panel; }
    public void appendMessage(String s){ log.append(s + "\n"); }
    public void updateTotal(int t){ totalLabel.setText("Score: " + t); }
    public void setUsername(String u){ this.username = u; panel.setUsername(u); }
    public String getUsername(){ return username; }

    public void showLeaderboard(String csv){
        SwingUtilities.invokeLater(() -> {
            String[] parts = csv.split(",");
            StringBuilder sb = new StringBuilder();
            int i=1;
            for (String p: parts) { sb.append(i++).append(". ").append(p).append("\n"); }
            JOptionPane.showMessageDialog(this, sb.toString(), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
