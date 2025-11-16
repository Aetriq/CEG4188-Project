package backend;

import java.io.*;
import java.net.*;

public class ClientHandling implements Runnable {
    private Socket socket;
    private GameState gameState;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandling(Socket socket, GameState gameState) {
        this.socket = socket;
        this.gameState = gameState;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String input;
            while ((input = in.readLine()) != null) {
                System.out.println("Received: " + input);
                // e.g., "LOCK 3 4 color=red"
                gameState.handleMessage(input, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
