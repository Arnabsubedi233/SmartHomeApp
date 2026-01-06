package com.smarthome;

import java.util.ArrayList;
import java.util.List;

public final class SmartHomeSystem implements SnapshotProvider {
    private final List<Sensor> sensors = new ArrayList<Sensor>();
    private boolean armed;

    private ContactConfig contacts;
    private final AlarmEngine alarmEngine;
    private final NotificationService notifier;
    private final AuditLog log;
    private final EventSubject events;

    public SmartHomeSystem(ContactConfig contacts, AlarmEngine alarmEngine, NotificationService notifier, AuditLog log, EventSubject events) {
        this.contacts = contacts;
        this.alarmEngine = alarmEngine;
        this.notifier = notifier;
        this.log = log;
        this.events = events;
    }

    public static SmartHomeSystem fromSnapshot(SystemSnapshot snapshot, NotificationService notifier, AuditLog log, EventSubject events) {
        AlarmEngine engine = new AlarmEngine(snapshot.getAlarmConfig());
        SmartHomeSystem sys = new SmartHomeSystem(snapshot.getContacts(), engine, notifier, log, events);
        sys.armed = snapshot.isArmed();
        for (Sensor s : snapshot.getSensors()) sys.sensors.add(s);
        return sys;
    }

    public void addDefaultSensors() {
        addSensor("FrontDoor", SensorType.DOOR, 1);
        addSensor("KitchenWindow", SensorType.WINDOW, 1);
        addSensor("HallMotion", SensorType.MOTION, 2);
        addSensor("BackDoor", SensorType.DOOR, 2);
    }

    public boolean isArmed() { return armed; }
    public List<Sensor> getSensors() { return sensors; }
    public ContactConfig getContacts() { return contacts; }
    public AlarmEngine getAlarmEngine() { return alarmEngine; }
    public AuditLog getLog() { return log; }
    public EventSubject getEvents() { return events; }

    public void arm() {
        armed = true;
        events.publish(new SystemEvent(now(), EventType.SYSTEM_ARMED, "System armed", null));
    }

    public void disarm() {
        armed = false;
        alarmEngine.clearPending();
        events.publish(new SystemEvent(now(), EventType.SYSTEM_DISARMED, "System disarmed (pending cleared)", null));
    }

    public int addSensor(String name, SensorType type, int zone) {
        if (!Validator.isValidSensorName(name)) {
            events.publish(new SystemEvent(now(), EventType.SENSOR_ADDED, "Rejected invalid sensor name", null));
            return -1;
        }
        if (zone < 0 || zone > 50) {
            events.publish(new SystemEvent(now(), EventType.SENSOR_ADDED, "Rejected invalid zone", null));
            return -1;
        }

        int id = sensors.size();
        sensors.add(new Sensor(id, name.trim(), type, zone, true));
        events.publish(new SystemEvent(now(), EventType.SENSOR_ADDED,
                "Sensor added id=" + id + " name=" + name.trim() + " type=" + type + " zone=" + zone, id));
        return id;
    }

    public boolean toggleSensor(int id) {
        if (id < 0 || id >= sensors.size()) return false;
        Sensor s = sensors.get(id);
        Sensor updated = s.withEnabled(!s.isEnabled());
        sensors.set(id, updated);
        events.publish(new SystemEvent(now(), EventType.SENSOR_TOGGLED,
                "Sensor toggled id=" + id + " enabled=" + updated.isEnabled(), id));
        return updated.isEnabled();
    }

    public boolean updateContacts(ContactConfig cfg) {
        if (!Validator.isValidPhone(cfg.getHomeownerPhone())) return false;
        if (!Validator.isValidEmail(cfg.getHomeownerEmail())) return false;
        if (!Validator.isValidPhone(cfg.getAuthorityPhone())) return false;

        this.contacts = cfg;
        events.publish(new SystemEvent(now(), EventType.CONTACTS_UPDATED, "Contacts updated", null));
        return true;
    }

    public boolean updateAlarmConfig(AlarmConfig cfg) {
        if (cfg.getConfirmationWindowSeconds() < 1 || cfg.getConfirmationWindowSeconds() > 3600) return false;
        if (cfg.getPendingExpirySeconds() < 5 || cfg.getPendingExpirySeconds() > 86400) return false;
        if (cfg.getMinDistinctSensorsToConfirm() < 1 || cfg.getMinDistinctSensorsToConfirm() > 10) return false;

        // feasibility: don't allow minDistinct > number of enabled security sensors
        int enabled = 0;
        for (Sensor s : sensors) {
            if (s.isEnabled() && s.getType() != SensorType.SMOKE) enabled++;
        }
        if (cfg.getMinDistinctSensorsToConfirm() > enabled) return false;

        alarmEngine.updateConfig(cfg);
        events.publish(new SystemEvent(now(), EventType.ALARM_CONFIG_UPDATED, "Alarm config updated", null));
        return true;
    }

    public SensorEventResult recordSensorEvent(int sensorId) {
        if (!armed) return new SensorEventResult("System is not armed.", null);
        if (sensorId < 0 || sensorId >= sensors.size()) return new SensorEventResult("Invalid sensor ID.", null);
        Sensor s = sensors.get(sensorId);
        if (!s.isEnabled()) return new SensorEventResult("Sensor is disabled.", null);

        SensorEvent event = new SensorEvent(System.currentTimeMillis(), sensorId);
        AlarmDecision decision = alarmEngine.recordEvent(event);
        events.publish(new SystemEvent(event.getTimestampMillis(), EventType.SENSOR_EVENT_RECORDED,
                "Sensor event recorded id=" + sensorId + " status=" + decision.getStatus(), sensorId));

        if (decision.getStatus() == AlarmStatus.CONFIRMED) {
            NotificationSummary summary = notifier.notifyAll(contacts, "Alarm confirmed!", "Sensor: " + s.getName());
            events.publish(new SystemEvent(event.getTimestampMillis(), EventType.ALARM_CONFIRMED, "Alarm confirmed", sensorId));
            return new SensorEventResult("Alarm confirmed!", summary.toString());
        } else if (decision.getStatus() == AlarmStatus.PENDING) {
            events.publish(new SystemEvent(event.getTimestampMillis(), EventType.ALARM_PENDING_CREATED, "Alarm pending", sensorId));
            return new SensorEventResult("Alarm pending.", null);
        } else {
            return new SensorEventResult("No alarm.", null);
        }
    }

    public PendingActionResult handlePendingAlarm(PendingDecision decision) {
        if (!alarmEngine.hasPending()) return new PendingActionResult("No pending alarm.", null);
        PendingAlarm pending = alarmEngine.getPendingAlarm();
        String msg = "Pending alarm for sensor " + pending.getSensorId() + ": " + pending.getReason();
        if (decision == PendingDecision.CONFIRM) {
            alarmEngine.clearPending();
            NotificationSummary summary = notifier.notifyAll(contacts, "Alarm confirmed by user!", msg);
            events.publish(new SystemEvent(System.currentTimeMillis(), EventType.ALARM_CONFIRMED, "Alarm confirmed by user", pending.getSensorId()));
            return new PendingActionResult("Alarm confirmed by user.", summary.toString());
        } else if (decision == PendingDecision.SUPPRESS) {
            alarmEngine.clearPending();
            events.publish(new SystemEvent(System.currentTimeMillis(), EventType.PENDING_CLEARED, "Pending alarm suppressed by user", pending.getSensorId()));
            return new PendingActionResult("Pending alarm suppressed.", null);
        } else {
            return new PendingActionResult("No action taken.", null);
        }
    }

    public SystemSnapshot snapshot() {
        return new SystemSnapshot(armed, contacts, alarmEngine.getConfig(), sensors);
    }

    private static long now() { return System.currentTimeMillis(); }
}
