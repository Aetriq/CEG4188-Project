package ceg4188.grp2.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ceg4188.grp2.shared.Protocol;

/**
 * Receives messages from server and updates Lobby and Game screens.
 */
public class ClientProtocolHandler extends Thread {
    private final String username;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Socket socket;

    private LobbyScreen lobby;
    private GameScreen game;

    public ClientProtocolHandler(String username, BufferedReader in, PrintWriter out, Socket socket) {
        this.username = username; this.in = in; this.out = out; this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // send JOIN
            out.println(Protocol.JOIN + " " + username);

            // create lobby UI (client-side)
            SwingUtilities.invokeLater(() -> {
                lobby = new LobbyScreen(username, out);
            });

            String line;
            while ((line = in.readLine()) != null) {
                final String s = line;
                SwingUtilities.invokeLater(() -> process(s));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Disconnected: " + e.getMessage()));
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void process(String line) {
        if (line == null || line.isBlank()) return;
        String[] parts = line.split(" ", 2);
        String cmd = parts[0];
        String rest = parts.length>1?parts[1]:"";

        switch (cmd) {
            case Protocol.JOIN_OK -> lobby.appendLog("Joined lobby.");
            case Protocol.JOIN_FAIL -> { JOptionPane.showMessageDialog(null, "Join failed: " + rest); }
            case Protocol.LOBBY_UPDATE -> lobby.updatePlayers(rest);
            case Protocol.LOBBY_SETTINGS -> lobby.applySettings(rest);
            case Protocol.START_GAME -> {
                // startEpoch provided (not used here). Close lobby & open game
                if (lobby != null) lobby.dispose();
                game = new GameScreen();
                game.setUsername(username);
                game.getGamePanel().setProtocolOut(out);
            }
            case Protocol.GAME_ALREADY_STARTED -> {
                JOptionPane.showMessageDialog(null, "Game already started, cannot join.");
                // disconnect
                try { socket.close(); } catch (IOException ignored) {}
            }
            case Protocol.COOKIE_SPAWN -> {
                String[] f = rest.split(" ");
                int id = Integer.parseInt(f[0]), x=Integer.parseInt(f[1]), y=Integer.parseInt(f[2]), score=Integer.parseInt(f[3]);
                if (game!=null) game.getGamePanel().spawnCookie(id,x,y,score);
            }
            case Protocol.COOKIE_MOVE -> {
                String[] f = rest.split(" ");
                int id = Integer.parseInt(f[0]), x=Integer.parseInt(f[1]), y=Integer.parseInt(f[2]);
                long ts = Long.parseLong(f[3]);
                if (game!=null) game.getGamePanel().moveCookie(id,x,y,ts);
            }
            case Protocol.COOKIE_DESPAWN -> {
                int id = Integer.parseInt(rest.trim());
                if (game!=null) game.getGamePanel().despawnCookie(id);
            }
            case Protocol.COOKIE_STATE -> {
                String[] f = rest.split(" ");
                int id=Integer.parseInt(f[0]), x=Integer.parseInt(f[1]), y=Integer.parseInt(f[2]);
                boolean locked = f[3].equals("1");
                String lockedBy = f[4];
                int score = Integer.parseInt(f[5]);
                if (game!=null) game.getGamePanel().setCookieState(id,x,y,locked,lockedBy,score);
            }
            case Protocol.LOCK_GRANTED -> {
                String[] f = rest.split(" ");
                int id = Integer.parseInt(f[0]); String who = f[1];
                if (who.equals(username)) out.println(Protocol.CLICK + " " + id);
            }
            case Protocol.LOCK_DENIED -> { /* show message */ }
            case Protocol.CLICKED -> {
                String[] f = rest.split(" ");
                int id=Integer.parseInt(f[0]); String who=f[1]; int score=Integer.parseInt(f[2]); int total=Integer.parseInt(f[3]);
                if (game!=null) { game.appendMessage(who + " clicked cookie " + id + " +" + score); game.updateTotal(total); game.getGamePanel().releaseCookieVisual(id); }
            }
            case Protocol.COOKIE_COUNT -> {
                if (game!=null) game.updateTotal(Integer.parseInt(rest.trim()));
            }
            case Protocol.LEADERBOARD -> {
                if (game!=null) game.showLeaderboard(rest);
            }
            case Protocol.MSG -> {
                if (lobby!=null) lobby.appendLog(rest);
                if (game!=null) game.appendMessage(rest);
            }
            default -> {
                System.out.println("Unhandled: " + line);
            }
        }
    }
}
