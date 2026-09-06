package com.javamessenger.network;

import com.google.gson.Gson;
import com.javamessenger.model.Packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Owns the client's single socket connection to the Java Messenger server.
 * <p>
 * All reading happens on a dedicated daemon thread ({@link #listenLoop()})
 * so the JavaFX Application Thread is never blocked waiting on the network.
 * Controllers register a callback via {@link #setOnPacketReceived}; that
 * callback must hop back onto the JavaFX thread with
 * {@code Platform.runLater(...)} before touching any UI control, since it
 * runs on the listener thread.
 */
public class NetworkClient {

    private final String host;
    private final int port;
    private final Gson gson = new Gson();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = false;

    private Consumer<Packet> onPacketReceived;
    private Runnable onDisconnected;

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        running = true;

        Thread listener = new Thread(this::listenLoop, "server-listener");
        listener.setDaemon(true);
        listener.start();
    }

    private void listenLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                Packet packet = gson.fromJson(line, Packet.class);
                if (onPacketReceived != null) {
                    onPacketReceived.accept(packet);
                }
            }
        } catch (IOException e) {
            // Socket closed or connection dropped; fall through to cleanup below.
        } finally {
            running = false;
            if (onDisconnected != null) {
                onDisconnected.run();
            }
        }
    }

    public synchronized void send(Packet packet) {
        if (out != null) {
            out.println(gson.toJson(packet));
        }
    }

    public void setOnPacketReceived(Consumer<Packet> handler) {
        this.onPacketReceived = handler;
    }

    public void setOnDisconnected(Runnable handler) {
        this.onDisconnected = handler;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // Already closed - nothing more to do.
        }
    }
}
