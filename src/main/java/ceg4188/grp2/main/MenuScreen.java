package ceg4188.grp2.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

// nEw imports
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//New imports for better layout control.
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ceg4188.grp2.client.Client;
import ceg4188.grp2.server.Server;

public class MenuScreen extends JFrame {
    private final JTextField usernameField = new JTextField("Player1");
    private final JTextField hostField = new JTextField("localhost");
    private final JTextField portField = new JTextField("6000");

    private final JRadioButton hostBtn = new JRadioButton("Host server (server-only)");
    private final JRadioButton joinBtn = new JRadioButton("Join game (client)");

    private final JButton startBtn = new JButton("Start");

    public MenuScreen() {
        setTitle("Cookie Clicker - Menu");
        setSize(700, 650); // Bigger for better layout.
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create the updated main panel.
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));
        mainPanel.setBackground(new Color(245, 245, 255)); // A nice blue background/

        // A Title
        JLabel titleLabel = new JLabel("Cookie Clicker", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 36));
        titleLabel.setForeground(new Color(139, 69, 19)); // A nice brown color.
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create a central panel
        JPanel centralPanel = new JPanel(new GridBagLayout());
        centralPanel.setBackground(new Color(245, 248, 255)); // Nice blue
        centralPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2)
        , BorderFactory.createEmptyBorder(50, 40, 80, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10); // Padding between components.
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Font Label variables.
        Font labelFont = new Font("Times New Roman", Font.BOLD, 16);
        Font fieldFont = new Font("Times New Roman", Font.PLAIN, 16);
        Font radiFont = new Font("Times New Roman", Font.BOLD, 16);

        // For the Username label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel userJLabel = new JLabel("Username:");
        userJLabel.setFont(labelFont);
        centralPanel.add(userJLabel, gbc);

        // For the username field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        usernameField.setFont(fieldFont);
        usernameField.setPreferredSize(new Dimension(100, 35));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(139, 69, 19), 1),
        BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        centralPanel.add(usernameField, gbc);

        // For the host label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel hosJLabel = new JLabel("Server Host:");
        hosJLabel.setFont(labelFont);
        centralPanel.add(hosJLabel, gbc);

        // For the host field
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        hostField.setFont(fieldFont);
        hostField.setPreferredSize(new Dimension(100, 35));
        hostField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(139, 69, 19), 1),
        BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        centralPanel.add(hostField, gbc);

        // For the port label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(labelFont);
        centralPanel.add(portLabel, gbc);

        // For the port field
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        portField.setFont(fieldFont);
        portField.setPreferredSize(new Dimension(100, 35));
        portField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(139, 69, 19), 1),
        BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        centralPanel.add(portField, gbc);

        // For the connection label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Center the label vertically
        gbc.fill = GridBagConstraints.NONE; // Don't stretch the label
        JLabel connectionLabel = new JLabel("Connection Type:");
        connectionLabel.setFont(labelFont);
        centralPanel.add(connectionLabel, gbc);

        // Reset the gridheight for the radiobuttons.
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create a buttongroup for the radio buttons
        ButtonGroup connectionGroup = new ButtonGroup();
        connectionGroup.add(hostBtn);
        connectionGroup.add(joinBtn);

        // The first radiobutton is host button.
         gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        
        hostBtn.setFont(radiFont);
        hostBtn.setBackground(new Color(245, 248, 255));
        hostBtn.setFocusPainted(false);
        hostBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        centralPanel.add(hostBtn, gbc);

        // The second button is the join button.
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        
        joinBtn.setFont(radiFont);
        joinBtn.setBackground(new Color(245, 248, 255));
        joinBtn.setFocusPainted(false);
        joinBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        joinBtn.setSelected(true);
        centralPanel.add(joinBtn, gbc);

        
        mainPanel.add(centralPanel, BorderLayout.CENTER);

        // Modified start button
        startBtn.setBackground(new Color(34, 139, 34)); // Pretty green
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Times New Roman", Font.BOLD, 20));
        startBtn.setFocusPainted(false);
        startBtn.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 40));
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.setPreferredSize(new Dimension(200, 60)); // Larger button

        // Action listner to the start button
         startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });

        // A new hover effect
         startBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                startBtn.setBackground(new Color(46, 139, 87)); // Differen green
            }
            public void mouseExited(MouseEvent e) {
                startBtn.setBackground(new Color(34, 139, 34)); // Pretty green
            }
        });

         JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 248, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Add a top margain.

        buttonPanel.add(startBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        setVisible(true);
    }

    private void start() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) username = "Player";

        int port;
        try { port = Integer.parseInt(portField.getText().trim()); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Port must be a number"); return; }

        String host = hostField.getText().trim();
        if (host.isEmpty()) host = "localhost";

        final String u = username;
        final String h = host;
        final int p = port;

        if (hostBtn.isSelected()) {
            // Start server only
            new Thread(() -> Server.start(p)).start();
            JOptionPane.showMessageDialog(this, "Server started on port " + p + ". Close this window to terminate the server.");
            
            // Disable buttons to prevent re-starting
            startBtn.setEnabled(false);
            hostBtn.setEnabled(false);
            joinBtn.setEnabled(false);
        } else {
            // Start client only
            new Thread(() -> Client.start(u, h, p)).start();
            dispose(); // <-- MOVED! Only dispose when starting a client.
        }
        // dispose(); // <-- OLD (INCORRECT) LOCATION
    }
}
