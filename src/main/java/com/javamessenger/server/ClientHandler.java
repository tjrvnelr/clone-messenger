package com.javamessenger.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.javamessenger.database.UserDAO;
import com.javamessenger.model.Packet;
import com.javamessenger.model.PacketType;
import com.javamessenger.model.User;
import com.javamessenger.utils.PasswordUtil;
import com.javamessenger.utils.ValidationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;

/**
 * One instance of this class runs on its own thread per connected socket
 * (see {@link Server}), so a slow or misbehaving client can never block
 * anyone else. It reads newline-delimited JSON packets, dispatches each to
 * the matching handler method, and writes the response back on the same
 * socket. Once a later phase adds real-time messaging, the server will
 * also call {@link #send(Packet)} on *other* clients' handlers (looked up
 * via {@link ServerRegistry}) to push messages to them immediately.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();

    private BufferedReader in;
    private PrintWriter out;
    private Integer authenticatedUserId = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String line;
            while ((line = in.readLine()) != null) {
                try {
                    Packet packet = gson.fromJson(line, Packet.class);
                    handlePacket(packet);
                } catch (Exception e) {
                    sendError("The server could not understand that request.");
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
        } finally {
            handleDisconnect();
        }
    }

    private void handlePacket(Packet packet) {
        switch (packet.getType()) {
            case REGISTER -> handleRegister(packet);
            case LOGIN -> handleLogin(packet);
            case LOGOUT -> handleDisconnect();
            default -> sendError("Unsupported request type: " + packet.getType());
        }
    }

    private void handleRegister(Packet packet) {
        JsonObject data = packet.getData();
        String username = data.get("username").getAsString().trim();
        String email = data.get("email").getAsString().trim().toLowerCase();
        String password = data.get("password").getAsString();

        String validationError = ValidationUtil.validateRegistration(username, email, password);
        if (validationError != null) {
            sendFailure(PacketType.REGISTER_FAILURE, validationError);
            return;
        }

        try {
            if (userDAO.usernameExists(username)) {
                sendFailure(PacketType.REGISTER_FAILURE, "That username is already taken.");
                return;
            }
            if (userDAO.emailExists(email)) {
                sendFailure(PacketType.REGISTER_FAILURE, "An account with that email already exists.");
                return;
            }
            String hash = PasswordUtil.hash(password);
            User created = userDAO.createUser(username, email, hash);
            System.out.println("Registered new user: " + created.getUsername());
            sendUser(PacketType.REGISTER_SUCCESS, created);
        } catch (SQLException e) {
            System.err.println("Registration DB error: " + e.getMessage());
            sendFailure(PacketType.REGISTER_FAILURE, "Server error: could not create account.");
        }
    }

    private void handleLogin(Packet packet) {
        JsonObject data = packet.getData();
        String identifier = data.get("identifier").getAsString().trim();
        String password = data.get("password").getAsString();

        try {
            User user = userDAO.findByUsernameOrEmail(identifier);
            if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
                sendFailure(PacketType.LOGIN_FAILURE, "Incorrect username/email or password.");
                return;
            }

            authenticatedUserId = user.getId();
            userDAO.updateStatus(user.getId(), "ONLINE");
            ServerRegistry.register(user.getId(), this);
            user.setStatus("ONLINE");

            System.out.println(user.getUsername() + " logged in.");
            sendUser(PacketType.LOGIN_SUCCESS, user);
        } catch (SQLException e) {
            System.err.println("Login DB error: " + e.getMessage());
            sendFailure(PacketType.LOGIN_FAILURE, "Server error: could not log in.");
        }
    }

    private void handleDisconnect() {
        if (authenticatedUserId != null) {
            try {
                userDAO.updateStatus(authenticatedUserId, "OFFLINE");
            } catch (SQLException e) {
                System.err.println("Failed to record offline status: " + e.getMessage());
            }
            ServerRegistry.unregister(authenticatedUserId);
            System.out.println("User " + authenticatedUserId + " disconnected.");
            authenticatedUserId = null;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            // Already closed or never fully opened - nothing more to do.
        }
    }

    private void sendFailure(PacketType type, String message) {
        JsonObject data = new JsonObject();
        data.addProperty("message", message);
        send(new Packet(type, data));
    }

    private void sendUser(PacketType type, User user) {
        JsonObject data = new JsonObject();
        data.add("user", gson.toJsonTree(user));
        send(new Packet(type, data));
    }

    private void sendError(String message) {
        sendFailure(PacketType.ERROR, message);
    }

    /** Called by this handler itself, and later by other handlers to push real-time events to this client. */
    public synchronized void send(Packet packet) {
        if (out != null) {
            out.println(gson.toJson(packet));
        }
    }
}
