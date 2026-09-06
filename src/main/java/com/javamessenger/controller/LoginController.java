package com.javamessenger.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.javamessenger.client.ClientContext;
import com.javamessenger.client.MainApp;
import com.javamessenger.model.Packet;
import com.javamessenger.model.PacketType;
import com.javamessenger.model.User;
import com.javamessenger.network.NetworkClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private TextField identifierField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String identifier = identifierField.getText().trim();
        String password = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Enter your username/email and password.");
            return;
        }
        errorLabel.setText("");

        NetworkClient client;
        try {
            client = connectIfNeeded();
        } catch (IOException e) {
            errorLabel.setText("Can't reach the server. Is it running?");
            return;
        }

        client.setOnPacketReceived(this::handleServerPacket);

        JsonObject data = new JsonObject();
        data.addProperty("identifier", identifier);
        data.addProperty("password", password);
        client.send(new Packet(PacketType.LOGIN, data));
    }

    @FXML
    private void goToRegister() throws IOException {
        MainApp.showRegister();
    }

    private NetworkClient connectIfNeeded() throws IOException {
        NetworkClient existing = ClientContext.getInstance().getNetworkClient();
        if (existing != null && existing.isConnected()) {
            return existing;
        }
        NetworkClient client = new NetworkClient(MainApp.HOST, MainApp.PORT);
        client.connect();
        ClientContext.getInstance().setNetworkClient(client);
        return client;
    }

    private void handleServerPacket(Packet packet) {
        Platform.runLater(() -> {
            switch (packet.getType()) {
                case LOGIN_SUCCESS -> onLoginSuccess(packet);
                case LOGIN_FAILURE, ERROR -> errorLabel.setText(packet.getData().get("message").getAsString());
                default -> {
                    // Other packet types aren't expected on this screen yet.
                }
            }
        });
    }

    private void onLoginSuccess(Packet packet) {
        User user = new Gson().fromJson(packet.getData().get("user"), User.class);
        ClientContext.getInstance().setCurrentUser(user);
        try {
            MainApp.showMessenger();
        } catch (IOException e) {
            errorLabel.setText("Logged in, but the messenger screen failed to load.");
        }
    }
}
