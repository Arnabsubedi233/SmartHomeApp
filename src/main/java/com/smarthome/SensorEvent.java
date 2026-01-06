package com.smarthome;

public class SensorEvent {
    private final long timestampMillis;
    private final int sensorId;

    public SensorEvent(long timestampMillis, int sensorId) {
        this.timestampMillis = timestampMillis;
        this.sensorId = sensorId;
    }

    public long getTimestampMillis() { return timestampMillis; }
    public int getSensorId() { return sensorId; }
}
