package backend;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private final ConcurrentHashMap<String, String> squares = new ConcurrentHashMap<>();

    public synchronized void handleMessage(String msg, PrintWriter out) {
        // Simple message format: "LOCK x y color"
        String[] parts = msg.split(" ");
        if (parts[0].equals("LOCK")) {
            String key = parts[1] + "," + parts[2];
            if (!squares.containsKey(key)) {
                squares.put(key, parts[3]);
                out.println("LOCK_OK " + key);
            } else {
                out.println("LOCK_DENIED " + key);
            }
        }
    }
}
