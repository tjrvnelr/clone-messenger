package com.javamessenger.client;

import com.javamessenger.model.User;
import com.javamessenger.network.NetworkClient;

/**
 * Holds the small amount of state every screen needs: the active socket
 * connection and the currently logged-in user. FXML controllers are
 * instantiated fresh by FXMLLoader each time a screen loads, so they can't
 * hold this state themselves - it has to live somewhere that survives
 * screen switches.
 */
public class ClientContext {

    private static final ClientContext INSTANCE = new ClientContext();

    private NetworkClient networkClient;
    private User currentUser;

    private ClientContext() {
    }

    public static ClientContext getInstance() {
        return INSTANCE;
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    public void setNetworkClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
