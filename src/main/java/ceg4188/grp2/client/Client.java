package ceg4188.grp2.client;

import ceg4188.grp2.shared.Protocol;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * Client launcher - connects, opens Lobby then Game UI.
 */
public class Client {
    public static void start(String username, String host, int port) {
        SwingUtilities.invokeLater(() -> {
            // nothing - UI will be created after handshake
        });

        new Thread(() -> {
            try {
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // create protocol handler which drives UI
                ClientProtocolHandler handler = new ClientProtocolHandler(username, in, out, socket);
                handler.start();

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Unable to connect: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }
}
