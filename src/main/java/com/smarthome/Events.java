package com.smarthome;

import java.util.ArrayList;
import java.util.List;

enum EventType {
    SYSTEM_ARMED,
    SYSTEM_DISARMED,
    SENSOR_ADDED,
    SENSOR_TOGGLED,
    CONTACTS_UPDATED,
    ALARM_CONFIG_UPDATED,
    SENSOR_EVENT_RECORDED,
    ALARM_PENDING_CREATED,
    ALARM_CONFIRMED,
    PENDING_CLEARED,
    NOTIFICATION_ATTEMPTED,
    SNAPSHOT_SAVED,
    SNAPSHOT_LOADED,
    SNAPSHOT_LOAD_FAILED,
    SNAPSHOT_SAVE_FAILED
}

final class SystemEvent {
    private final long timestampMillis;
    private final EventType type;
    private final String message;
    private final Integer sensorId;

    SystemEvent(long timestampMillis, EventType type, String message, Integer sensorId) {
        this.timestampMillis = timestampMillis;
        this.type = type;
        this.message = message;
        this.sensorId = sensorId;
    }

    long getTimestampMillis() { return timestampMillis; }
    EventType getType() { return type; }
    String getMessage() { return message; }
    Integer getSensorId() { return sensorId; }
}

interface Observer {
    void update(SystemEvent event);
}

final class EventSubject {
    private final List<Observer> observers = new ArrayList<Observer>();

    synchronized void register(Observer o) {
        if (o != null && !observers.contains(o)) observers.add(o);
    }

    synchronized void unregister(Observer o) {
        observers.remove(o);
    }

    void publish(SystemEvent e) {
        List<Observer> copy;
        synchronized (this) {
            copy = new ArrayList<Observer>(observers);
        }
        for (Observer o : copy) {
            try {
                o.update(e);
            } catch (Exception ignored) {
            }
        }
    }
}

final class AuditLogObserver implements Observer {
    private final AuditLog log;

    AuditLogObserver(AuditLog log) { this.log = log; }

    public void update(SystemEvent event) {
        if (event == null) return;
        String msg = event.getType() + (event.getMessage() == null ? "" : (": " + event.getMessage()));
        if (event.getType() == EventType.ALARM_CONFIRMED || event.getType() == EventType.ALARM_PENDING_CREATED) {
            log.warn(msg);
        } else if (event.getType() == EventType.SNAPSHOT_LOAD_FAILED || event.getType() == EventType.SNAPSHOT_SAVE_FAILED) {
            log.error(msg);
        } else {
            log.info(msg);
        }
    }
}

interface SnapshotProvider {
    SystemSnapshot snapshot();
}

final class AutoSaveObserver implements Observer {
    private final SnapshotProvider provider;
    private final SnapshotStore store;
    private final AuditLog log;

    AutoSaveObserver(SnapshotProvider provider, SnapshotStore store, AuditLog log) {
        this.provider = provider;
        this.store = store;
        this.log = log;
    }

    public void update(SystemEvent event) {
        if (event == null) return;
        if (event.getType() == EventType.ALARM_CONFIRMED || event.getType() == EventType.ALARM_PENDING_CREATED) {
            try {
                store.save(provider.snapshot());
                log.info("Snapshot auto-saved after event: " + event.getType());
            } catch (Exception e) {
                log.error("Auto-save failed: " + e.getMessage());
            }
        }
    }
}
