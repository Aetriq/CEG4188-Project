package ceg4188.grp2.server;

import ceg4188.grp2.shared.GameSettings;
import ceg4188.grp2.shared.GameState;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Server launcher (server-only).
 */
public class Server {
    private static final int MAX_PLAYERS = 4;

    public static void start(int port) {
        GameState state = new GameState();
        List<ServerClientHandler> clients = new ArrayList<>();
        CookieManager manager = new CookieManager(state, clients);
        new Thread(manager).start();

        System.out.println("Server starting on port " + port);
        try (PrintWriter audit = new PrintWriter(new FileWriter("server_audit.log", true));
             ServerSocket ss = new ServerSocket(port)) {

            while (true) {
                Socket s = ss.accept();
                ServerClientHandler handler = new ServerClientHandler(s, state, clients, audit, MAX_PLAYERS);
                synchronized (clients) { clients.add(handler); }
                handler.start();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
