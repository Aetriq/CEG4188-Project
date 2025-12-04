/* CEG4188 - Final Project
 * CrunchLAN Multiplayer Game
 * ServerClientHandler.java 
 * Server side handler. Per-client handler; parses protocol and updates GameState.
 * 12-03-25
 * Authors: Escalante, A., Gordon, A. 
 */
package ceg4188.grp2.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import ceg4188.grp2.shared.Cookie;
import ceg4188.grp2.shared.GameSettings;
import ceg4188.grp2.shared.GameState;
import ceg4188.grp2.shared.Protocol;

/**
 * Per-client handler; parses protocol and updates GameState.
 */
public class ServerClientHandler extends Thread {
    private final Socket socket;
    private final GameState state;
    private final List<ServerClientHandler> clients;
    private final PrintWriter audit;
    private final int maxPlayers;

    private BufferedReader in;
    private PrintWriter out;
    private String username = "unknown";

    public ServerClientHandler(Socket socket, GameState state, List<ServerClientHandler> clients, PrintWriter audit, int maxPlayers) {
        this.socket = socket; this.state = state; this.clients = clients; this.audit = audit; this.maxPlayers = maxPlayers;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            send(Protocol.MSG + " Welcome! Please JOIN <username>");
            sendLobbyState();

            String line;
            while ((line = in.readLine()) != null) {
                audit.println(System.currentTimeMillis() + " " + username + " -> " + line);
                audit.flush();
                handle(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + username);
        } finally {
            cleanup();
        }
    }

    private void handle(String line) {
        if (line.isBlank()) return;
        String[] parts = line.split(" ", 2);
        String cmd = parts[0];

        try {
            switch (cmd) {
                case Protocol.JOIN -> {
                    if (parts.length < 2) { send(Protocol.JOIN_FAIL + " Missing username"); break; }
                    String u = parts[1].trim();
                    synchronized (state) {
                        if (state.isGameRunning()) {
                            send(Protocol.GAME_ALREADY_STARTED);
                            break;
                        }
                        boolean ok = state.addPlayer(u, maxPlayers);
                        if (!ok) { send(Protocol.JOIN_FAIL + " Server full"); break; }
                        username = u;
                    }
                    send(Protocol.JOIN_OK);
                    broadcastLobby();
                }
                case Protocol.LOBBY_SETTINGS -> {
                    // only host can set (first player)
                    List<String> players = state.getPlayers();
                    if (players.isEmpty() || !players.get(0).equals(username)) { send(Protocol.MSG + " Only host may change settings"); break; }
                    // parse values
                    String[] ts = parts[1].split(" ");
                    GameSettings gs = new GameSettings();
                    gs.durationSeconds = Integer.parseInt(ts[0]);
                    gs.minCookies = Integer.parseInt(ts[1]);
                    gs.maxCookies = Integer.parseInt(ts[2]);
                    gs.minScore = Integer.parseInt(ts[3]);
                    gs.maxScore = Integer.parseInt(ts[4]);
                    gs.spawnMinMs = Integer.parseInt(ts[5]);
                    gs.spawnMaxMs = Integer.parseInt(ts[6]);
                    gs.maxCookiesAllowed = Integer.parseInt(ts[7]);
                    state.applySettings(gs);
                    broadcast(Protocol.LOBBY_SETTINGS + " " + parts[1]);
                }
                case Protocol.START_GAME -> {
                    List<String> players = state.getPlayers();
                    if (players.isEmpty() || !players.get(0).equals(username)) { send(Protocol.MSG + " Only host may start"); break; }
                    state.startGame();
                    broadcast(Protocol.START_GAME + " " + state.getEndEpoch());
                    // spawn initial cookies between min and max
                    int want = (state.getSettings().minCookies + state.getSettings().maxCookies)/2;
                    for (int i=0;i<want;i++){
                        Cookie c = state.spawnRandomCookie();
                        if (c != null) broadcast(Protocol.COOKIE_SPAWN + " " + c.getId() + " " + c.getX() + " " + c.getY() + " " + c.getScore());
                    }
                }
                case Protocol.LOCK_REQUEST -> {                    
                    String[] p = parts[1].split(" ");
                    int id = Integer.parseInt(p[0]);

                     // DEBUG
                    System.out.println("DEBUG: User " + username + " requested lock on cookie " + id);

                    // A better lock
                    Cookie cookie = state.getCookie(id);
                    if (cookie == null){
                        send(Protocol.LOCK_DENIED + " " + id + " Cookie does not exist");
                        break;
                    }

                    boolean ok = state.lockCookie(id, username);
                    if (ok) {
                        send(Protocol.LOCK_GRANTED + " " + id + " " + username);
                        // Update all clients with the locked state.
                        broadcast(Protocol.COOKIE_STATE + " " + id + " " + cookie.getX()
                        + " " + cookie.getY() + " 1 " + username + " " + cookie.getScore());
                    } else {
                        String currentLocker = cookie.getLockedBy();
                        send(Protocol.LOCK_DENIED + " " + id + " Alredy locked by " + currentLocker);
                    }
                }

                case Protocol.CLICK -> {
                // expected: "CLICK <id>"

                if (parts.length < 2) {
                    send(Protocol.MSG + " Missing cookie id for CLICK");
                    break;
                }
                try {
                    int id = Integer.parseInt(parts[1].trim());

                    // DEBUG
                    System.out.println("DEBUG: User " + username + " clicked cookie " + id);

                    Cookie cookie = state.getCookie(id);
                    String locker = (cookie == null) ? null : cookie.getLockedBy();
                    if (locker == null || !locker.equals(username)) {
                        send(Protocol.MSG + " You are not the locker of cookie " + id);
                        break;
                    }
                    int gained = state.clickCookie(id, username); // This returns 1 per click.

                    // Get the updated cookie after a click
                    Cookie updatedCookie = state.getCookie(id);
                    if (updatedCookie != null){
                        // This means the score > 0
                        broadcast(Protocol.COOKIE_STATE + " " + id + " " + 
                        updatedCookie.getX() + " " + updatedCookie.getY() + " 1 " + username + 
                        " " + updatedCookie.getScore());
                        
                        broadcast(Protocol.CLICKED + " " + id + " " + username + " " + 0 + " " + state.getTotalScore());
                    }else{
                        // The cookie is destroyed
                        send(Protocol.MSG + " Cookie" + id + " destroyed! " + username + 
                        " earned " + gained + " point(s)");

                        broadcast(Protocol.CLICKED + " " + id + " " + username + " " + gained + " " + state.getTotalScore());
                        broadcast(Protocol.COOKIE_DESPAWN + " " + id);
                    }
                    
                    broadcast(Protocol.COOKIE_COUNT + " " + state.getTotalScore());
                    
                } catch (NumberFormatException ex) {
                    send(Protocol.ERROR + " Invalid cookie id for CLICK");
                }
            }

                case Protocol.MSG -> {
                    broadcast(Protocol.MSG + " " + parts[1]);
                }
                default -> send(Protocol.MSG + " Unknown command: " + cmd);
            }
        } catch (Exception ex) {
            send(Protocol.ERROR + " " + ex.getMessage());
        }
    }

    private void broadcastLobby() {
        List<String> players = state.getPlayers();
        String csv = String.join(",", players);
        synchronized (clients) { for (ServerClientHandler c : clients) c.send(Protocol.LOBBY_UPDATE + " " + csv); }
    }

    private void sendLobbyState() {
        GameSettings s = state.getSettings();
        send(Protocol.LOBBY_SETTINGS + " " + s.durationSeconds + " " + s.minCookies + " " + s.maxCookies + " " +
                s.minScore + " " + s.maxScore + " " + s.spawnMinMs + " " + s.spawnMaxMs + " " + s.maxCookiesAllowed);
        String csv = String.join(",", state.getPlayers());
        send(Protocol.LOBBY_UPDATE + " " + csv);
    }

    public void send(String msg){ if (out!=null) out.println(msg); }

    private void broadcast(String msg){
        synchronized (clients){
            for (ServerClientHandler c: clients) c.send(msg);
        }
    }

    private void cleanup(){
        try { socket.close(); } catch (IOException ignored) {}
        synchronized (clients){ clients.remove(this); }
        state.removePlayer(username);
        broadcast(Protocol.LOBBY_UPDATE + " " + String.join(",", state.getPlayers()));
    }
}
