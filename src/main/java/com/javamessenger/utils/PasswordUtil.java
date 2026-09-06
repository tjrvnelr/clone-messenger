package com.javamessenger.utils;

import org.mindrot.jbcrypt.BCrypt;

/** Wraps bcrypt hashing so no other class ever touches a plain-text password directly. */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean verify(String plainPassword, String storedHash) {
        return BCrypt.checkpw(plainPassword, storedHash);
    }
}
