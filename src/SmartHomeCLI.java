import java.util.List;
import java.util.Scanner;

public final class SmartHomeCLI {
    private final SmartHomeSystem system;
    private final Scanner scanner = new Scanner(System.in);

    public SmartHomeCLI(SmartHomeSystem system) {
        this.system = system;
    }

    public void run() {
        system.getEvents().publish(new SystemEvent(System.currentTimeMillis(), EventType.SNAPSHOT_LOADED, "CLI started", null));

        while (true) {
            println("");
            println("==============================================");
            println(" SMART HOME SECURITY AND SAFETY (CLI PROTOTYPE)");
            println("==============================================");
            println("Status: " + (system.isArmed() ? "ARMED" : "DISARMED")
                    + " | Sensors: " + system.getSensors().size()
                    + " | Pending alarm: " + (system.getAlarmEngine().hasPending() ? "YES" : "NO"));
            println("");
            println("1) Security and Safety Monitoring");
            println("2) Alarm Confirmation and False Alarm Mitigation");
            println("3) Simulate Sensor Event");
            println("4) View Audit Log");
            println("0) Exit");

            int choice = readInt("Select: ", 0, 4);
            if (choice == 0) {
                println("Exiting.");
                return;
            } else if (choice == 1) {
                securityMenu();
            } else if (choice == 2) {
                alarmMenu();
            } else if (choice == 3) {
                simulateEventMenu();
            } else if (choice == 4) {
                viewLog();
            }
        }
    }

    private void securityMenu() {
        while (true) {
            println("");
            println("---- SECURITY AND SAFETY MONITORING ----");
            println("1) Arm system");
            println("2) Disarm system");
            println("3) Add sensor");
            println("4) List sensors");
            println("5) Enable/Disable sensor");
            println("6) Configure notification contacts");
            println("7) Back");

            int choice = readInt("Select: ", 1, 7);
            if (choice == 7) return;

            switch (choice) {
                case 1:
                    system.arm();
                    println("System armed.");
                    break;
                case 2:
                    system.disarm();
                    println("System disarmed.");
                    break;
                case 3:
                    addSensorMenu();
                    break;
                case 4:
                    listSensors();
                    break;
                case 5:
                    toggleSensorMenu();
                    break;
                case 6:
                    configureContactsMenu();
                    break;
                default:
                    println("Invalid selection.");
            }
        }
    }

    private void alarmMenu() {
        while (true) {
            AlarmConfig cfg = system.getAlarmEngine().getConfig();
            println("");
            println("---- ALARM CONFIRMATION / FALSE ALARM MITIGATION ----");
            println("Current settings:");
            println(" - Confirmation window (seconds): " + cfg.getConfirmationWindowSeconds());
            println(" - Minimum distinct sensors to confirm: " + cfg.getMinDistinctSensorsToConfirm());
            println(" - Pending alarm expiry (seconds): " + cfg.getPendingExpirySeconds());
            println("");
            println("1) Update confirmation window seconds");
            println("2) Update minimum distinct sensors to confirm");
            println("3) Update pending alarm expiry seconds");
            println("4) Review pending alarm");
            println("5) View recent event buffer");
            println("6) Back");

            int choice = readInt("Select: ", 1, 6);
            if (choice == 6) return;

            switch (choice) {
                case 1: {
                    int v = readInt("Enter seconds (1-3600): ", 1, 3600);
                    if (!system.updateAlarmConfig(cfg.withConfirmationWindowSeconds(v))) {
                        println("Rejected: settings not feasible or invalid.");
                    } else {
                        println("Updated.");
                    }
                    break;
                }
                case 2: {
                    int maxFeasible = Math.max(1, enabledSecuritySensors());
                    int v = readInt("Enter count (1-" + Math.min(10, maxFeasible) + "): ", 1, Math.min(10, maxFeasible));
                    if (!system.updateAlarmConfig(cfg.withMinDistinctSensorsToConfirm(v))) {
                        println("Rejected: settings not feasible or invalid.");
                    } else {
                        println("Updated.");
                    }
                    break;
                }
                case 3: {
                    int v = readInt("Enter seconds (5-86400): ", 5, 86400);
                    if (!system.updateAlarmConfig(cfg.withPendingExpirySeconds(v))) {
                        println("Rejected: settings not feasible or invalid.");
                    } else {
                        println("Updated.");
                    }
                    break;
                }
                case 4:
                    reviewPendingMenu();
                    break;
                case 5:
                    viewRecentEvents();
                    break;
                default:
                    println("Invalid selection.");
            }
        }
    }

    private int enabledSecuritySensors() {
        int enabled = 0;
        for (Sensor s : system.getSensors()) {
            if (s.isEnabled() && s.getType() != SensorType.SMOKE) enabled++;
        }
        return enabled;
    }

    private void simulateEventMenu() {
        if (system.getSensors().isEmpty()) {
            println("No sensors configured.");
            return;
        }
        listSensors();
        int id = readInt("Triggered sensor id: ", 0, system.getSensors().size() - 1);
        SensorEventResult result = system.triggerSensor(id);
        println(result.getUserMessage());
        if (result.getNotificationSummary() != null && !result.getNotificationSummary().trim().isEmpty()) {
            println(result.getNotificationSummary());
        }
    }

    private void addSensorMenu() {
        println("");
        println("---- ADD SENSOR ----");
        String name = readSensorName();
        SensorType type = readSensorType();
        int zone = readInt("Zone (0-50): ", 0, 50);

        int id = system.addSensor(name, type, zone);
        if (id < 0) println("Sensor rejected (validation failed).");
        else println("Sensor added with id=" + id);
    }

    private String readSensorName() {
        while (true) {
            String name = readNonEmpty("Sensor name (2-40 chars; letters/digits/space/-/' ; must start with letter): ");
            if (Validator.isValidSensorName(name)) return name.trim();
            println("Invalid sensor name.");
        }
    }

    private void toggleSensorMenu() {
        if (system.getSensors().isEmpty()) {
            println("No sensors configured.");
            return;
        }
        listSensors();
        int id = readInt("Sensor id: ", 0, system.getSensors().size() - 1);
        boolean enabled = system.toggleSensor(id);
        println("Sensor " + id + " enabled=" + enabled);
    }

    private void configureContactsMenu() {
        println("");
        println("---- NOTIFICATION CONTACTS ----");
        ContactConfig cfg = system.getContacts();
        println("Homeowner phone : " + cfg.getHomeownerPhone());
        println("Homeowner email : " + cfg.getHomeownerEmail());
        println("Authority phone : " + cfg.getAuthorityPhone());
        println("");
        println("1) Update homeowner phone");
        println("2) Update homeowner email");
        println("3) Update authority phone");
        println("4) Back");

        int choice = readInt("Select: ", 1, 4);
        if (choice == 4) return;

        ContactConfig next = cfg;
        if (choice == 1) next = cfg.withHomeownerPhone(readPhone("New homeowner phone (+digits, 7-15): "));
        else if (choice == 2) next = cfg.withHomeownerEmail(readEmail("New homeowner email: "));
        else if (choice == 3) next = cfg.withAuthorityPhone(readPhone("New authority phone (+digits, 7-15): "));

        if (!system.updateContacts(next)) {
            println("Rejected: invalid contact details.");
        } else {
            println("Contacts updated.");
        }
    }

    private String readPhone(String prompt) {
        while (true) {
            String v = readNonEmpty(prompt);
            if (Validator.isValidPhone(v)) return v.trim();
            println("Invalid phone format.");
        }
    }

    private String readEmail(String prompt) {
        while (true) {
            String v = readNonEmpty(prompt);
            if (Validator.isValidEmail(v)) return v.trim();
            println("Invalid email format.");
        }
    }

    private void reviewPendingMenu() {
        AlarmEngine engine = system.getAlarmEngine();
        engine.expirePendingIfNeeded();

        if (!engine.hasPending()) {
            println("No pending alarm.");
            return;
        }

        PendingAlarm p = engine.getPendingAlarm();
        Sensor s = system.getSensors().get(p.getSensorId());

        println("");
        println("---- PENDING ALARM ----");
        println("Created : " + system.getLog().fmtTime(p.getCreatedAtMillis()));
        println("Sensor  : " + p.getSensorId() + " (" + s.getName() + ")");
        println("Reason  : " + p.getReason());
        println("");
        println("1) Confirm alarm (initiate response)");
        println("2) Mark false alarm (suppress)");
        println("3) Defer");

        int choice = readInt("Select: ", 1, 3);
        PendingDecision decision;
        if (choice == 1) decision = PendingDecision.CONFIRM;
        else if (choice == 2) decision = PendingDecision.SUPPRESS;
        else decision = PendingDecision.DEFER;

        PendingActionResult result = system.handlePending(decision);
        println(result.getUserMessage());
        if (result.getNotificationSummary() != null && !result.getNotificationSummary().trim().isEmpty()) {
            println(result.getNotificationSummary());
        }
    }

    private void listSensors() {
        println("");
        println("---- SENSORS ----");
        List<Sensor> sensors = system.getSensors();
        for (Sensor s : sensors) {
            println(String.format(
                    "%d) %-16s | type=%-6s | zone=%-2d | enabled=%s",
                    s.getId(), s.getName(), s.getType(), s.getZone(), s.isEnabled()
            ));
        }
    }

    private void viewRecentEvents() {
        println("");
        println("---- RECENT EVENTS ----");
        List<SensorEvent> events = system.getAlarmEngine().recentEventsSnapshot();
        if (events.isEmpty()) {
            println("(empty)");
            return;
        }
        for (SensorEvent e : events) {
            Sensor s = system.getSensors().get(e.getSensorId());
            println(system.getLog().fmtTime(e.getTimestampMillis())
                    + " | sensor=" + e.getSensorId()
                    + " (" + s.getName() + ")"
                    + " | type=" + s.getType()
                    + " | zone=" + s.getZone());
        }
    }

    private void viewLog() {
        println("");
        println("---- AUDIT LOG ----");
        List<String> lines = system.getLog().snapshot();
        if (lines.isEmpty()) {
            println("(empty)");
            return;
        }
        for (String line : lines) println(line);
    }

    private SensorType readSensorType() {
        while (true) {
            println("Type options: 1) DOOR  2) WINDOW  3) MOTION  4) CAMERA  5) SMOKE");
            int t = readInt("Type: ", 1, 5);
            if (t == 1) return SensorType.DOOR;
            if (t == 2) return SensorType.WINDOW;
            if (t == 3) return SensorType.MOTION;
            if (t == 4) return SensorType.CAMERA;
            return SensorType.SMOKE;
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            print(prompt);
            String s = scanner.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < min || v > max) {
                    println("Enter a value between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (Exception e) {
                println("Enter a valid number.");
            }
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            print(prompt);
            String s = scanner.nextLine();
            if (s != null && !s.trim().isEmpty()) return s.trim();
            println("Value cannot be empty.");
        }
    }

    private static void println(String s) { System.out.println(s); }
    private static void print(String s) { System.out.print(s); }
}
