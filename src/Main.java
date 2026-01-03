import java.io.File;

public class Main {
    public static void main(String[] args) {
        AuditLog log = new AuditLog(500);
        EventSubject events = new EventSubject();
        events.register(new AuditLogObserver(log));

        SnapshotStore store = new SnapshotStore("autosave.properties");
        NotificationService notifier = new ConsoleNotificationService(log);

        SmartHomeSystem system = null;

        File f = new File(store.getFilePath());
        if (f.exists() && f.isFile()) {
            try {
                SystemSnapshot snap = store.load();
                system = SmartHomeSystem.fromSnapshot(snap, notifier, log, events);
                events.publish(new SystemEvent(System.currentTimeMillis(), EventType.SNAPSHOT_LOADED, "Loaded snapshot from disk", null));
            } catch (Exception e) {
                events.publish(new SystemEvent(System.currentTimeMillis(), EventType.SNAPSHOT_LOAD_FAILED, "Snapshot load failed: " + e.getMessage(), null));
            }
        }

        if (system == null) {
            ContactConfig contacts = new ContactConfig("+447000000000", "home@example.com", "+44999000000");
            AlarmConfig alarmConfig = new AlarmConfig(10, 2, 60);

            AlarmEngine alarmEngine = new AlarmEngine(alarmConfig);
            system = new SmartHomeSystem(contacts, alarmEngine, notifier, log, events);
            system.addDefaultSensors();
        }

        events.register(new AutoSaveObserver(system, store, log));

        SmartHomeCLI cli = new SmartHomeCLI(system);
        cli.run();
    }
}
