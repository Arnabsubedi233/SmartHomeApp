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
        if (cfg.getMinDistinctSensorsToConfirm() > Math.max(1, enabled)) return false;

        alarmEngine.updateConfig(cfg);
        events.publish(new SystemEvent(now(), EventType.ALARM_CONFIG_UPDATED, "Alarm config updated: " + cfg.toString(), null));
        return true;
    }

    public SensorEventResult triggerSensor(int sensorId) {
        if (sensorId < 0 || sensorId >= sensors.size()) {
            events.publish(new SystemEvent(now(), EventType.SENSOR_EVENT_RECORDED, "Rejected invalid sensor id=" + sensorId, sensorId));
            return new SensorEventResult("Sensor not found.", null);
        }

        Sensor sensor = sensors.get(sensorId);
        if (!sensor.isEnabled()) {
            events.publish(new SystemEvent(now(), EventType.SENSOR_EVENT_RECORDED, "Ignored disabled sensor id=" + sensorId, sensorId));
            return new SensorEventResult("Event ignored: sensor is disabled.", null);
        }

        SensorEvent event = new SensorEvent(now(), sensorId);
        events.publish(new SystemEvent(event.getTimestampMillis(), EventType.SENSOR_EVENT_RECORDED,
                "Event recorded id=" + sensorId + " name=" + sensor.getName() + " type=" + sensor.getType(), sensorId));

        if (sensor.getType() == SensorType.SMOKE) {
            NotificationSummary summary = notifier.notifyAll(contacts, "Safety event: smoke detected",
                    "Smoke sensor triggered: " + sensor.getName());
            events.publish(new SystemEvent(now(), EventType.NOTIFICATION_ATTEMPTED, "Smoke notification attempted", sensorId));
            return new SensorEventResult("Safety event detected; notifications issued.", summary.toDisplayString());
        }

        if (!armed) {
            return new SensorEventResult("System disarmed; event logged.", null);
        }

        AlarmDecision decision = alarmEngine.recordEvent(event);
        if (decision.getStatus() == AlarmStatus.CONFIRMED) {
            events.publish(new SystemEvent(now(), EventType.ALARM_CONFIRMED, decision.getMessage(), sensorId));
            NotificationSummary summary = notifier.notifyAll(contacts, "Alarm confirmed", decision.getMessage());
            events.publish(new SystemEvent(now(), EventType.NOTIFICATION_ATTEMPTED, "Alarm notification attempted", sensorId));
            return new SensorEventResult("Alarm confirmed; notifications issued.", summary.toDisplayString());
        }

        if (decision.getStatus() == AlarmStatus.PENDING) {
            if (alarmEngine.getPendingAlarm() != null && alarmEngine.getPendingAlarm().getSensorId() == sensorId) {
                events.publish(new SystemEvent(now(), EventType.ALARM_PENDING_CREATED, decision.getMessage(), sensorId));
            }
            return new SensorEventResult("Alarm pending review. " + decision.getMessage(), null);
        }

        return new SensorEventResult("Event recorded; no alarm action required.", null);
    }

    public PendingActionResult handlePending(PendingDecision decision) {
        alarmEngine.expirePendingIfNeeded();
        if (!alarmEngine.hasPending()) {
            return new PendingActionResult("No pending alarm.", null);
        }

        PendingAlarm p = alarmEngine.getPendingAlarm();
        if (decision == PendingDecision.DEFER) {
            return new PendingActionResult("No action taken.", null);
        }

        if (decision == PendingDecision.SUPPRESS) {
            alarmEngine.clearPending();
            events.publish(new SystemEvent(now(), EventType.PENDING_CLEARED, "Pending alarm suppressed by user", p.getSensorId()));
            return new PendingActionResult("Pending alarm suppressed as false alarm.", null);
        }

        // CONFIRM
        alarmEngine.clearPending();
        events.publish(new SystemEvent(now(), EventType.PENDING_CLEARED, "Pending alarm confirmed by user", p.getSensorId()));
        Sensor sensor = sensors.get(p.getSensorId());
        NotificationSummary summary = notifier.notifyAll(contacts, "Alarm confirmed (user)",
                "User confirmed alarm. Sensor: " + sensor.getName() + " (" + sensor.getType() + ")");
        events.publish(new SystemEvent(now(), EventType.NOTIFICATION_ATTEMPTED, "User-confirmed alarm notification attempted", p.getSensorId()));
        return new PendingActionResult("Pending alarm confirmed; notifications issued.", summary.toDisplayString());
    }

    public SystemSnapshot snapshot() {
        return new SystemSnapshot(armed, contacts, alarmEngine.getConfig(), sensors);
    }

    private static long now() { return System.currentTimeMillis(); }
}
