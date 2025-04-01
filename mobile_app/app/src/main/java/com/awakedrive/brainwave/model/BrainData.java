package com.awakedrive.brainwave.model;

public class BrainData {
    private long timestamp;
    private int delta;
    private int highAlpha;
    private int highBeta;
    private int lowAlpha;
    private int lowBeta;
    private int lowGamma;
    private int middleGamma;
    private int theta;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public int getHighAlpha() {
        return highAlpha;
    }

    public void setHighAlpha(int highAlpha) {
        this.highAlpha = highAlpha;
    }

    public int getHighBeta() {
        return highBeta;
    }

    public void setHighBeta(int highBeta) {
        this.highBeta = highBeta;
    }

    public int getLowAlpha() {
        return lowAlpha;
    }

    public void setLowAlpha(int lowAlpha) {
        this.lowAlpha = lowAlpha;
    }

    public int getLowBeta() {
        return lowBeta;
    }

    public void setLowBeta(int lowBeta) {
        this.lowBeta = lowBeta;
    }

    public int getLowGamma() {
        return lowGamma;
    }

    public void setLowGamma(int lowGamma) {
        this.lowGamma = lowGamma;
    }

    public int getMiddleGamma() {
        return middleGamma;
    }

    public void setMiddleGamma(int middleGamma) {
        this.middleGamma = middleGamma;
    }

    public int getTheta() {
        return theta;
    }

    public void setTheta(int theta) {
        this.theta = theta;
    }
}
