package com.smarthome;

public class AlarmConfig {
    private final int confirmationWindowSeconds;
    private final int minDistinctSensorsToConfirm;
    private final int pendingExpirySeconds;

    public AlarmConfig(int confirmationWindowSeconds, int minDistinctSensorsToConfirm, int pendingExpirySeconds) {
        this.confirmationWindowSeconds = confirmationWindowSeconds;
        this.minDistinctSensorsToConfirm = minDistinctSensorsToConfirm;
        this.pendingExpirySeconds = pendingExpirySeconds;
    }

    public int getConfirmationWindowSeconds() { return confirmationWindowSeconds; }
    public int getMinDistinctSensorsToConfirm() { return minDistinctSensorsToConfirm; }
    public int getPendingExpirySeconds() { return pendingExpirySeconds; }

    public AlarmConfig withConfirmationWindowSeconds(int v) { return new AlarmConfig(v, minDistinctSensorsToConfirm, pendingExpirySeconds); }
    public AlarmConfig withMinDistinctSensorsToConfirm(int v) { return new AlarmConfig(confirmationWindowSeconds, v, pendingExpirySeconds); }
    public AlarmConfig withPendingExpirySeconds(int v) { return new AlarmConfig(confirmationWindowSeconds, minDistinctSensorsToConfirm, v); }

    public String toString() {
        return "AlarmConfig{windowSec=" + confirmationWindowSeconds
                + ", minDistinct=" + minDistinctSensorsToConfirm
                + ", pendingExpirySec=" + pendingExpirySeconds + "}";
    }
}
