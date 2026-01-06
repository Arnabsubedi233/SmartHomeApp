package com.smarthome;

public class AlarmDecision {
    private final AlarmStatus status;
    private final String message;

    public AlarmDecision(AlarmStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public AlarmStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
