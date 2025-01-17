package com.example.brainwave.model;

public class Device {
    private final String name;
    private final String address;
    private final String status;


    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Device(String name, String address, String status) {
        this.name = name;
        this.address = address;
        this.status = status;
    }
}
