package com.javamessenger.controller;

import com.google.gson.JsonObject;
import com.javamessenger.client.ClientContext;
import com.javamessenger.client.MainApp;
import com.javamessenger.model.Packet;
import com.javamessenger.model.PacketType;
import com.javamessenger.network.NetworkClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Please fill in every field.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
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
        data.addProperty("username", username);
        data.addProperty("email", email);
        data.addProperty("password", password);
        client.send(new Packet(PacketType.REGISTER, data));
    }

    @FXML
    private void goToLogin() throws IOException {
        MainApp.showLogin();
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
                case REGISTER_SUCCESS -> onRegisterSuccess();
                case REGISTER_FAILURE, ERROR -> errorLabel.setText(packet.getData().get("message").getAsString());
                default -> {
                    // Other packet types aren't expected on this screen yet.
                }
            }
        });
    }

    private void onRegisterSuccess() {
        try {
            MainApp.showLogin();
        } catch (IOException e) {
            errorLabel.setText("Account created - please return to the login screen manually.");
        }
    }
}
