package com.javamessenger.controller;

import com.google.gson.JsonObject;
import com.javamessenger.client.ClientContext;
import com.javamessenger.client.MainApp;
import com.javamessenger.model.Packet;
import com.javamessenger.model.PacketType;
import com.javamessenger.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

/**
 * Phase-1 shell for the messenger home screen: shows who's logged in and
 * lets them log out. The conversation list, search box, and chat panel
 * described in the spec are added in the messaging phase, once the wire
 * protocol's SEARCH_USERS / CONVERSATION_LIST / SEND_MESSAGE packets are
 * wired up on the server.
 */
public class MessengerController {

    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Label placeholderLabel;

    @FXML
    private void initialize() {
        User user = ClientContext.getInstance().getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            statusLabel.setText("Online");
        }
        placeholderLabel.setText(
                "You're logged in. Conversations, search, and real-time messaging land in the next build phase."
        );
    }

    @FXML
    private void handleLogout() {
        var client = ClientContext.getInstance().getNetworkClient();
        if (client != null) {
            client.send(new Packet(PacketType.LOGOUT, new JsonObject()));
            client.disconnect();
        }
        ClientContext.getInstance().setCurrentUser(null);
        ClientContext.getInstance().setNetworkClient(null);
        try {
            MainApp.showLogin();
        } catch (IOException ignored) {
            // Nothing sensible to show the user if the login screen itself fails to load.
        }
    }
}
