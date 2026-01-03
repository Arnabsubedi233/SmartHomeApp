import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class SnapshotStore {
    private final String filePath;

    public SnapshotStore(String filePath) {
        this.filePath = filePath;
    }

    public void save(SystemSnapshot snapshot) throws IOException {
        Properties p = new Properties();
        p.setProperty("armed", String.valueOf(snapshot.isArmed()));

        ContactConfig c = snapshot.getContacts();
        p.setProperty("contacts.homeownerPhone", safe(c.getHomeownerPhone()));
        p.setProperty("contacts.homeownerEmail", safe(c.getHomeownerEmail()));
        p.setProperty("contacts.authorityPhone", safe(c.getAuthorityPhone()));

        AlarmConfig a = snapshot.getAlarmConfig();
        p.setProperty("alarm.windowSec", String.valueOf(a.getConfirmationWindowSeconds()));
        p.setProperty("alarm.minDistinct", String.valueOf(a.getMinDistinctSensorsToConfirm()));
        p.setProperty("alarm.pendingExpirySec", String.valueOf(a.getPendingExpirySeconds()));

        List<Sensor> sensors = snapshot.getSensors();
        p.setProperty("sensor.count", String.valueOf(sensors.size()));
        for (int i = 0; i < sensors.size(); i++) {
            Sensor s = sensors.get(i);
            p.setProperty("sensor." + i + ".name", safe(s.getName()));
            p.setProperty("sensor." + i + ".type", s.getType().name());
            p.setProperty("sensor." + i + ".zone", String.valueOf(s.getZone()));
            p.setProperty("sensor." + i + ".enabled", String.valueOf(s.isEnabled()));
        }

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            p.store(out, "SmartHomeCLI autosave");
        }
    }

    public SystemSnapshot load() throws IOException {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(filePath)) {
            p.load(in);
        }

        boolean armed = Boolean.parseBoolean(p.getProperty("armed", "false"));

        String hp = p.getProperty("contacts.homeownerPhone", "");
        String he = p.getProperty("contacts.homeownerEmail", "");
        String ap = p.getProperty("contacts.authorityPhone", "");

        int windowSec = parseInt(p.getProperty("alarm.windowSec"), 10);
        int minDistinct = parseInt(p.getProperty("alarm.minDistinct"), 2);
        int pendingExpirySec = parseInt(p.getProperty("alarm.pendingExpirySec"), 60);

        AlarmConfig alarmConfig = new AlarmConfig(windowSec, minDistinct, pendingExpirySec);
        ContactConfig contacts = new ContactConfig(hp, he, ap);

        int count = parseInt(p.getProperty("sensor.count"), 0);
        List<Sensor> sensors = new ArrayList<Sensor>();

        for (int i = 0; i < count; i++) {
            String name = p.getProperty("sensor." + i + ".name", "");
            String typeS = p.getProperty("sensor." + i + ".type", "DOOR");
            int zone = parseInt(p.getProperty("sensor." + i + ".zone"), 0);
            boolean enabled = Boolean.parseBoolean(p.getProperty("sensor." + i + ".enabled", "true"));

            SensorType type;
            try {
                type = SensorType.valueOf(typeS);
            } catch (Exception e) {
                type = SensorType.DOOR;
            }

            sensors.add(new Sensor(i, name, type, zone, enabled));
        }

        return new SystemSnapshot(armed, contacts, alarmConfig, sensors);
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    public String getFilePath() { return filePath; }
}
