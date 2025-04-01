package com.awakedrive.brainwave.model;

public class Session {
    private String sessionId;
    private long startTime;
    private long endTime;

    public Session() {}

    public Session(String sessionId, long startTime, long endTime) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
