package com.javamessenger.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens plain JDBC connections to MySQL using the settings in
 * {@code src/main/resources/db.properties}. Each DAO method opens its own
 * short-lived connection in a try-with-resources block rather than sharing
 * one connection across threads, which keeps the server's per-client
 * threads from stepping on each other.
 */
public class DBConnection {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("db.properties not found on the classpath.");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties: " + e.getMessage(), e);
        }
    }

    /** Caller is responsible for closing the returned connection (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password")
        );
    }
}
