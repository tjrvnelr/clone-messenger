package com.javamessenger.model;

public class User {

    private int id;
    private String username;
    private String email;

    // "transient" tells Gson to leave this field out of any JSON it writes,
    // so a User can be sent straight to the client without ever leaking the
    // password hash over the network.
    private transient String passwordHash;

    private String profilePicture;
    private String status;     // "ONLINE" or "OFFLINE"
    private String lastSeen;   // formatted timestamp, nullable
    private String createdAt;  // formatted timestamp

    public User() {
    }

    public User(int id, String username, String email, String passwordHash,
                String profilePicture, String status, String lastSeen, String createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.profilePicture = profilePicture;
        this.status = status;
        this.lastSeen = lastSeen;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
