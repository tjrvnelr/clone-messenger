package com.javamessenger.model;

import com.google.gson.JsonObject;

/**
 * The single envelope type sent in both directions over the socket.
 * <p>
 * Packets are serialized to one line of compact JSON with Gson and sent
 * terminated by a newline, so the reading side can simply call
 * {@code BufferedReader.readLine()} in a loop. {@code data} carries a
 * loose JSON object whose fields depend on {@code type} (e.g. a LOGIN
 * packet's data has "identifier" and "password"; a LOGIN_SUCCESS
 * packet's data has "user").
 */
public class Packet {

    private PacketType type;
    private JsonObject data;

    /** No-arg constructor required by Gson when deserializing. */
    public Packet() {
    }

    public Packet(PacketType type, JsonObject data) {
        this.type = type;
        this.data = data;
    }

    public PacketType getType() {
        return type;
    }

    public JsonObject getData() {
        return data;
    }
}
