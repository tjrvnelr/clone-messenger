package com.javamessenger.model;

/**
 * Every kind of message the Java Messenger client and server exchange.
 * <p>
 * Phase 1 (this build) only uses the REGISTER/LOGIN/LOGOUT/ERROR types.
 * The remaining types are declared up front so the wire protocol doesn't
 * need to change shape again when messaging, search, and presence are
 * added in later phases.
 */
public enum PacketType {

    // --- Authentication (phase 1) ---
    REGISTER,
    REGISTER_SUCCESS,
    REGISTER_FAILURE,
    LOGIN,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,

    // --- Reserved for later phases ---
    SEARCH_USERS,
    SEARCH_RESULTS,
    CONVERSATION_LIST,
    OPEN_CONVERSATION,
    MESSAGE_HISTORY,
    SEND_MESSAGE,
    INCOMING_MESSAGE,
    MARK_READ,
    STATUS_UPDATE,

    ERROR
}
