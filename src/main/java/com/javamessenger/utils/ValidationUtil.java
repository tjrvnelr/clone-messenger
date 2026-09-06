package com.javamessenger.utils;

import java.util.regex.Pattern;

/**
 * Server-side validation. The client also checks these fields before sending
 * a request (so users get instant feedback), but the server re-checks
 * everything itself since a client can never be trusted.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    /** @return a user-friendly error message, or null if every field is valid. */
    public static String validateRegistration(String username, String email, String password) {
        if (username == null || username.isBlank()) {
            return "Username is required.";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "Username must be 3-20 characters (letters, numbers, underscore only).";
        }
        if (email == null || email.isBlank()) {
            return "Email is required.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Enter a valid email address.";
        }
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters.";
        }
        return null;
    }
}
