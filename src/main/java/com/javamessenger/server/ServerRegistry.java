package com.javamessenger.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which {@link ClientHandler} is currently serving each logged-in
 * user, so the server can find the right socket to push a real-time event
 * (e.g. an incoming chat message, in a later phase) to a specific user.
 * A ConcurrentHashMap is used because every client thread reads and writes
 * this registry concurrently.
 */
public class ServerRegistry {

    private static final Map<Integer, ClientHandler> ONLINE_USERS = new ConcurrentHashMap<>();

    private ServerRegistry() {
    }

    public static void register(int userId, ClientHandler handler) {
        ONLINE_USERS.put(userId, handler);
    }

    public static void unregister(int userId) {
        ONLINE_USERS.remove(userId);
    }

    public static ClientHandler get(int userId) {
        return ONLINE_USERS.get(userId);
    }

    public static boolean isOnline(int userId) {
        return ONLINE_USERS.containsKey(userId);
    }
}
