/* CEG4188 - Final Project
 * CrunchLAN Multiplayer Game
 * GameScreen.java 
 * Primary class to handle the UI for the game screen.
 * 12-03-25
 * Authors: Escalante, A., Gordon, A. 
 */
package ceg4188.grp2.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

// Added timer
import javax.swing.Timer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

// Added awt.events
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GameScreen extends JFrame {
    private final GamePanel panel;
    private final JLabel totalLabel = new JLabel("Score: 0");

    // Reference the parent for restarting the game
    private final Runnable onRestart; 

    private final JLabel timerLabel = new JLabel("Time: 60s"); // New timer display.

    private final JTextArea log = new JTextArea();
    private String username = "Player";
    private Timer gameTimer; // Game timer.
    private int timeRemaining = 60; // Has a 60 seconds game time.


    public GameScreen(Runnable onRestart) {
        this.onRestart = onRestart;
        setTitle("Cookie Clicker - Game");
        setSize(1280,720);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        panel = new GamePanel(this);
        add(panel, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setPreferredSize(new Dimension(300,720));

        //Create a new panel for top information.
        // Adds 2 new ros for a score and a timer
        JPanel topInfo = new JPanel(new GridLayout(2, 1)); 

        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));


        // New timer label
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setForeground(Color.BLUE);

        topInfo.add(totalLabel);
        topInfo.add(timerLabel); // Add a timer to layout.
        right.add(topInfo, BorderLayout.NORTH);

        log.setEditable(false);
        right.add(new JScrollPane(log), BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        setVisible(true);

        // New game timer
        startGameTimer();
    }


    // New method for the game timer countdown.
    private void startGameTimer(){
        gameTimer = new Timer(1000, new ActionListener() { // Update every second
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                timerLabel.setText("Time: " + timeRemaining + "s");
                
                // Change color when time is running out
                if (timeRemaining <= 10) {
                    timerLabel.setForeground(Color.RED);
                } else if (timeRemaining <= 30) {
                    timerLabel.setForeground(Color.ORANGE);
                }
                
                if (timeRemaining <= 0) {
                    gameTimer.stop();
                    timerLabel.setText("TIME UP!");
                    // You can add game over logic here
                    appendMessage("Game Over! Time's up!");
                }
            }
        });
        gameTimer.start();
    }

    // New method to get the timer
    public Timer getGameTimer(){
        return gameTimer;
    }

    public GamePanel getGamePanel(){ return panel; }
    public void appendMessage(String s){ log.append(s + "\n"); }
    public void updateTotal(int t){ totalLabel.setText("Score: " + t); }
    public void setUsername(String u){ this.username = u; panel.setUsername(u); }
    public String getUsername(){ return username; }

    public void showLeaderboard(String csv){
        SwingUtilities.invokeLater(() -> {
            // Parse the leaderboard data
            String[] parts = csv.split(",");

            // New leaderboard
            JDialog leaderboardDialog = new JDialog(this, "Game Over - Leaderboard", true);

            // --- Create a new window (JFrame) ---
            leaderboardDialog.setSize(450, 500);
            leaderboardDialog.setLocationRelativeTo(this); // Center on the game window
            leaderboardDialog.setLayout(new BorderLayout(10, 10));
            leaderboardDialog.getContentPane().setBackground(new Color(240, 248, 255));

            // TItle for the game over
             JLabel titleLabel = new JLabel("Game Over!", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
            titleLabel.setForeground(new Color(139, 69, 19));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            leaderboardDialog.add(titleLabel, BorderLayout.NORTH);

            // Add a text area to show the scores
            JPanel scoresPanel = new JPanel();
            scoresPanel.setLayout(new BoxLayout(scoresPanel, BoxLayout.Y_AXIS));
            scoresPanel.setBackground(Color.WHITE);
            scoresPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

             JLabel scoresTitle = new JLabel("Final Scores:", SwingConstants.CENTER);
            scoresTitle.setFont(new Font("Arial", Font.BOLD, 20));
            scoresTitle.setAlignmentX(CENTER_ALIGNMENT);
            scoresPanel.add(scoresTitle);
            scoresPanel.add(Box.createRigidArea(new Dimension(0, 15)));

            // Better formating
            for (int i = 0; i < parts.length; i++){
                String[] playerScore = parts[i].split(":");
                if (playerScore.length == 2) {
                    String playerName = playerScore[0].trim();
                    String score = playerScore[1].trim();
                    
                    JPanel playerPanel = new JPanel(new BorderLayout());
                    playerPanel.setBackground(Color.WHITE);
                    playerPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
                    playerPanel.setMaximumSize(new Dimension(400, 40));
                    
                    JLabel rankLabel = new JLabel((i + 1) + ". ");
                    rankLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    rankLabel.setForeground(new Color(139, 69, 19));
                    
                    JLabel nameLabel = new JLabel(playerName);
                    nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    
                    JLabel scoreLabel = new JLabel(score + " points");
                    scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    scoreLabel.setForeground(new Color(0, 100, 0));
                    
                    playerPanel.add(rankLabel, BorderLayout.WEST);
                    playerPanel.add(nameLabel, BorderLayout.CENTER);
                    playerPanel.add(scoreLabel, BorderLayout.EAST);
                    
                    scoresPanel.add(playerPanel);
                    
                    // Add separators bettweeen players, execpt the last.
                    if (i < parts.length - 1) {
                        scoresPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
                    }
                }
            }

            JScrollPane scrollPane = new JScrollPane(scoresPanel);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            leaderboardDialog.add(scrollPane, BorderLayout.CENTER);

            // For the play again buton
            JButton playAgainButton = new JButton("Play Again");
            playAgainButton.setBackground(new Color(34, 139, 34));
            playAgainButton.setForeground(Color.WHITE);
            playAgainButton.setFont(new Font("Arial", Font.BOLD, 14));
            playAgainButton.addActionListener(e -> {
                leaderboardDialog.dispose();  // Close dialog
                this.dispose();  // Close game screen
                onRestart.run();  // Restart to go back to the lobby
            });

            // For the close button
            JButton closeButton = new JButton("Close");
            closeButton.setBackground(new Color(139, 69, 19));
            closeButton.setForeground(Color.WHITE);
            closeButton.setFont(new Font("Arial", Font.BOLD, 14));
            closeButton.setFocusPainted(false);
            closeButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
            closeButton.addActionListener(e -> leaderboardDialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(240, 248, 255));
            buttonPanel.add(closeButton);
            // Add button to the button panel
            buttonPanel.add(playAgainButton);

            leaderboardDialog.add(buttonPanel, BorderLayout.SOUTH);
            
            leaderboardDialog.setVisible(true);

            // Disable the main game panel now that the game is over
            panel.setEnabled(false);

        });

     
    }
}
