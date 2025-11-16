package backend;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandling> clients = new ArrayList<>();
    private static GameState gameState = new GameState();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Game server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New player connected!");
            ClientHandling handler = new ClientHandling(clientSocket, gameState);
            clients.add(handler);
            new Thread(handler).start();
        }
    }
}
