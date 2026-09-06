package com.javamessenger.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Java Messenger Server entry point.
 * <p>
 * Listens on {@link #PORT}, and spins up a dedicated {@link ClientHandler}
 * thread for every incoming connection so clients never block one another.
 * Run with: {@code mvn exec:java} (see pom.xml), or run this class's
 * {@code main} method directly from an IDE - it needs no JavaFX runtime.
 */
public class Server {

    public static final int PORT = 5555;

    public static void main(String[] args) {
        System.out.println("Java Messenger Server starting on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening for connections.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from " + clientSocket.getInetAddress());
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Server failed to start on port " + PORT + ": " + e.getMessage());
        }
    }
}
