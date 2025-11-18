package ceg4188.grp2.main;

import ceg4188.grp2.client.Client;
import ceg4188.grp2.server.Server;

import javax.swing.*;
import java.awt.*;

public class MenuScreen extends JFrame {
    private final JTextField usernameField = new JTextField("Player1");
    private final JTextField hostField = new JTextField("localhost");
    private final JTextField portField = new JTextField("5000");

    private final JRadioButton hostBtn = new JRadioButton("Host server (server-only)");
    private final JRadioButton joinBtn = new JRadioButton("Join game (client)");

    private final JButton startBtn = new JButton("Start");

    public MenuScreen() {
        setTitle("Cookie Clicker - Menu");
        setSize(420, 320);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(9, 1, 6, 6));

        ButtonGroup g = new ButtonGroup();
        g.add(hostBtn); g.add(joinBtn);
        joinBtn.setSelected(true);

        add(new JLabel("Username (client):"));
        add(usernameField);
        add(new JLabel("Host (client):"));
        add(hostField);
        add(new JLabel("Port:"));
        add(portField);

        JPanel p = new JPanel(new FlowLayout());
        p.add(hostBtn); p.add(joinBtn);
        add(p);

        add(startBtn);

        startBtn.addActionListener(e -> start());

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
            JOptionPane.showMessageDialog(this, "Server started on port " + p + ". Now run a client and join to play.");
        } else {
            // Start client only
            new Thread(() -> Client.start(u, h, p)).start();
        }
        dispose();
    }
}
