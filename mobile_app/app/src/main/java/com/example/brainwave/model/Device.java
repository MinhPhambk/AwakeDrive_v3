package com.example.brainwave.model;

public class Device {
    private final String name;
    private final String address;
    private final boolean connected;

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isConnected() {
        return connected;
    }

    public Device(String name, String address, boolean connected) {
        this.name = name;
        this.address = address;
        this.connected = connected;
    }
}
