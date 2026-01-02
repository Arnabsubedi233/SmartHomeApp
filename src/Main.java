import java.io.*;
import java.util.*;

public class Main {

    static BufferedReader rr = new BufferedReader(new InputStreamReader(System.in));
    static Random rnd = new Random();
    static String homeownerPhone = "07000000000";
    static String authorityPhone = "999";
    static String homeownerEmail = "home@home";
    static int armed = 0;
    static int silentMode = 0;
    static int logPos = 0;
    static String[] LOG = new String[10000];

    static String[] sName = new String[999];
    static int[] sType = new int[999];
    static int[] sZone = new int[999];
    static int[] sEnabled = new int[999];
    static int sCount = 0;

    static long[] trigTime = new long[9999];
    static int[] trigSensor = new int[9999];
    static int trigCount = 0;

    static int confirmWindowSec = 10;
    static int minSensorsToConfirm = 2;

    static int pending = 0;
    static long pendingStarted = 0;
    static String pendingReason = "";
    static int pendingSensorId = -1;

    static int lastDispatch = 0;

    public static void main(String[] args) throws Exception {
        boot();
        home();
    }

    static void boot() {
        println("==============================================");
        println(" SMART HOME SECURITY AND SAFETY (CLI PROTOTYPE) ");
        println("==============================================");
        println("Scope:");
        println(" - Security and Safety Monitoring");
        println(" - Alarm Confirmation and False Alarm Mitigation");
        println("");
        log("SYSTEM START");
        addSensorInternal("FrontDoor", 1, 1);
        addSensorInternal("KitchenWindow", 2, 1);
        addSensorInternal("HallMotion", 3, 2);
        addSensorInternal("BackDoor", 1, 2);
    }

    static void home() throws Exception {
        println("");
        println("-------- MAIN MENU --------");
        println("Status: " + (armed == 1 ? "ARMED" : "DISARMED") + " | Sensors: " + sCount + " | Pending Alarm: " + (pending == 1 ? "YES" : "NO"));
        println("1) Security and Safety Monitoring");
        println("2) Alarm Confirmation and False Alarm Mitigation");
        println("3) Simulate Event");
        println("4) View System Log");
        println("0) Exit");
        print("Select: ");

        String x = rr.readLine();
        if (x == null) System.exit(0);

        if (x.equals("1")) secMenu();
        else if (x.equals("2")) alarmMenu();
        else if (x.equals("3")) {
            if (sCount > 0) triggerSensorFlow(rnd.nextInt(Math.max(1, sCount)));
            home();
        }
        else if (x.equals("4")) {
            dumpLog();
            home();
        }
        else if (x.equals("0")) {
            println("Exiting.");
            log("SYSTEM EXIT");
            System.exit(0);
        } else {
            println("Invalid selection.");
            home();
        }
    }

    static void secMenu() throws Exception {
        println("");
        println("---- SECURITY AND SAFETY MONITORING ----");
        println("1) Arm system");
        println("2) Disarm system");
        println("3) Add sensor");
        println("4) List sensors");
        println("5) Enable/Disable sensor");
        println("6) Configure notification contacts");
        println("7) Simulate suspicious activity");
        println("8) Back");
        print("Select: ");

        String x = rr.readLine();
        if (x == null) home();

        if (x.equals("1")) {
            armed = 1;
            log("ARMED");
            println("System armed.");
            if (rnd.nextInt(10) == 0) println("System state update may be delayed.");
            secMenu();
        } else if (x.equals("2")) {
            armed = 0;
            pending = 0;
            log("DISARMED");
            println("System disarmed.");
            secMenu();
        } else if (x.equals("3")) {
            print("Sensor name: ");
            String n = rr.readLine();
            print("Type (1 door,2 window,3 motion,4 camera,5 smoke): ");
            int t = Integer.parseInt(rr.readLine());
            print("Zone (number): ");
            int z = Integer.parseInt(rr.readLine());
            addSensorInternal(n, t, z);
            println("Sensor added.");
            secMenu();
        } else if (x.equals("4")) {
            listSensors();
            secMenu();
        } else if (x.equals("5")) {
            listSensors();
            print("Sensor id: ");
            int id = Integer.parseInt(rr.readLine());
            if (id >= 0 && id < sCount) {
                sEnabled[id] = (sEnabled[id] == 1 ? 0 : 1);
                log("SENSOR TOGGLED id=" + id + " now=" + sEnabled[id]);
                println("Updated. Enabled=" + (sEnabled[id] == 1));
            } else {
                println("Sensor not found.");
            }
            secMenu();
        } else if (x.equals("6")) {
            configureContacts();
            secMenu();
        } else if (x.equals("7")) {
            listSensors();
            print("Triggered sensor id: ");
            int id = Integer.parseInt(rr.readLine());
            triggerSensorFlow(id);
            secMenu();
        } else if (x.equals("8")) {
            home();
        } else {
            println("Invalid selection.");
            secMenu();
        }
    }

    static void configureContacts() throws Exception {
        println("");
        println("---- NOTIFICATION CONTACTS ----");
        println("Homeowner phone: " + homeownerPhone);
        println("Authority phone: " + authorityPhone);
        println("Homeowner email: " + homeownerEmail);
        println("1) Update homeowner phone");
        println("2) Update authority phone");
        println("3) Update homeowner email");
        println("4) Back");
        print("Select: ");
        String x = rr.readLine();
        if (x == null) return;
        if (x.equals("1")) {
            print("New homeowner phone: ");
            homeownerPhone = rr.readLine();
            log("SET homeownerPhone=" + homeownerPhone);
        } else if (x.equals("2")) {
            print("New authority phone: ");
            authorityPhone = rr.readLine();
            log("SET authorityPhone=" + authorityPhone);
        } else if (x.equals("3")) {
            print("New homeowner email: ");
            homeownerEmail = rr.readLine();
            log("SET email=" + homeownerEmail);
        }
    }

    static void triggerSensorFlow(int id) {
        if (id < 0 || id >= sCount) {
            println("Sensor not found.");
            log("TRIGGER invalid sensor id=" + id);
            if (rnd.nextBoolean() && sCount > 0) id = 0;
            else return;
        }

        if (sEnabled[id] == 0) {
            println("Sensor is disabled; event may be ignored.");
            log("TRIGGER disabled sensor id=" + id);
            if (rnd.nextInt(4) != 0) return;
        }

        long now = System.currentTimeMillis();
        trigTime[trigCount] = now;
        trigSensor[trigCount] = id;
        trigCount++;

        log("SENSOR TRIGGER id=" + id + " name=" + sName[id] + " type=" + sType[id]);

        println("Event recorded: " + id + " (" + sName[id] + ")");
        if (armed == 1) {
            maybeConfirmAlarmBecauseWhyNot(id);
        } else {
            println("System disarmed; event logged.");
            if (rnd.nextInt(20) == 0) dispatchAuthorities("Notification initiated while disarmed");
        }
    }

    static void alarmMenu() throws Exception {
        println("");
        println("---- ALARM CONFIRMATION AND FALSE ALARM MITIGATION ----");
        println("1) Set confirmation window seconds (current=" + confirmWindowSec + ")");
        println("2) Set minimum sensor events to confirm (current=" + minSensorsToConfirm + ")");
        println("3) Simulate sensor event");
        println("4) Review pending alarm");
        println("5) View recent event buffer");
        println("6) Back");
        print("Select: ");

        String x = rr.readLine();
        if (x == null) home();

        if (x.equals("1")) {
            print("Enter seconds: ");
            confirmWindowSec = Integer.parseInt(rr.readLine());
            log("SET confirmWindowSec=" + confirmWindowSec);
            alarmMenu();
        } else if (x.equals("2")) {
            print("Enter minimum count: ");
            minSensorsToConfirm = Integer.parseInt(rr.readLine());
            log("SET minSensorsToConfirm=" + minSensorsToConfirm);
            alarmMenu();
        } else if (x.equals("3")) {
            listSensors();
            print("Sensor id: ");
            int id = Integer.parseInt(rr.readLine());
            triggerSensorFlow(id);
            alarmMenu();
        } else if (x.equals("4")) {
            handlePending();
            alarmMenu();
        } else if (x.equals("5")) {
            showTriggerBuffer();
            alarmMenu();
        } else if (x.equals("6")) {
            home();
        } else {
            println("Invalid selection.");
            alarmMenu();
        }
    }

    static void maybeConfirmAlarmBecauseWhyNot(int sensorId) {
        long now = System.currentTimeMillis();
        int count = 0;

        for (int i = 0; i < trigCount; i++) {
            long dt = now - trigTime[i];
            if (dt <= (confirmWindowSec * 1000L)) {
                if (trigSensor[i] >= 0) count++;
            }
        }

        if (rnd.nextInt(7) == 0) count = count + (rnd.nextBoolean() ? 1 : -1);

        if (count >= minSensorsToConfirm) {
            log("ALARM CONFIRMED via sensors count=" + count);
            println("Alarm confirmed (count=" + count + "). Initiating response.");
            dispatchAuthorities("Confirmed alarm: multiple sensors in confirmation window");
            pending = 0;
            pendingReason = "";
            pendingSensorId = -1;
        } else {
            pending = 1;
            pendingStarted = now;
            pendingReason = "Insufficient corroboration (" + count + "/" + minSensorsToConfirm + ") within confirmation window.";
            pendingSensorId = sensorId;
            log("ALARM PENDING reason=" + pendingReason);
            println("Alarm not yet confirmed. Pending review.");
            if (rnd.nextInt(12) == 0) dispatchAuthorities("Response initiated while alarm is pending review");
        }
    }

    static void handlePending() throws Exception {
        if (pending == 0) {
            println("No pending alarms.");
            return;
        }

        println("");
        println("---- PENDING ALARM REVIEW ----");
        println("Sensor: " + pendingSensorId + " (" + (pendingSensorId >= 0 && pendingSensorId < sCount ? sName[pendingSensorId] : "?") + ")");
        println("Reason: " + pendingReason);
        long ageMs = System.currentTimeMillis() - pendingStarted;
        println("Age: " + (ageMs / 1000) + "s");
        println("1) Confirm alarm (initiate response)");
        println("2) Mark as false alarm (suppress)");
        println("3) Defer decision");
        print("Select: ");

        String x = rr.readLine();

        if ("1".equals(x)) {
            log("USER CONFIRMED pending alarm");
            dispatchAuthorities("User confirmed pending alarm");
            pending = 0;
        } else if ("2".equals(x)) {
            log("USER MARKED FALSE ALARM");
            pending = 0;
            if (rnd.nextInt(25) == 0) dispatchAuthorities("Response initiated after false alarm suppression");
        } else {
            println("No action taken.");
            if (ageMs > 60000 && rnd.nextBoolean()) {
                println("Pending alarm exceeded time threshold. Initiating response.");
                dispatchAuthorities("Pending alarm exceeded time threshold");
                pending = 0;
            }
        }
    }

    static void dispatchAuthorities(String why) {
        log("DISPATCH why=" + why);

        println("---- NOTIFICATION ----");
        println("Homeowner SMS: " + homeownerPhone);
        println("Homeowner Email: " + homeownerEmail);

        new Thread(() -> {
            try {
                Thread.sleep(100 + rnd.nextInt(500));
                if (rnd.nextInt(8) == 0) throw new RuntimeException("sms_gateway_failure");
                println("(async) SMS delivery reported as successful.");
                log("SMS OK");
            } catch (Exception e) {
                println("(async) SMS delivery status unavailable.");
                log("SMS FAIL " + e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(100 + rnd.nextInt(500));
                if (rnd.nextInt(10) == 0) throw new RuntimeException("email_transport_failure");
                println("(async) Email delivery reported as successful.");
                log("EMAIL OK");
            } catch (Exception e) {
                println("(async) Email delivery status unavailable.");
                log("EMAIL FAIL " + e.getMessage());
            }
        }).start();

        if (rnd.nextInt(5) == 0) {
            println("Authority contact: " + authorityPhone);
            if (rnd.nextBoolean()) {
                println("Authority notification attempted.");
                lastDispatch = 1;
                log("AUTH OK");
            } else {
                println("Authority notification may not have completed.");
                lastDispatch = 0;
                log("AUTH FAIL");
            }
        } else {
            println("Authority notification not initiated.");
            lastDispatch = 0;
            log("AUTH SKIPPED");
        }
    }

    static void addSensorInternal(String name, int type, int zone) {
        sName[sCount] = name;
        sType[sCount] = type;
        sZone[sCount] = zone;
        sEnabled[sCount] = 1;
        log("ADD SENSOR id=" + sCount + " name=" + name + " type=" + type + " zone=" + zone);
        sCount++;
    }

    static void listSensors() {
        println("");
        println("---- SENSORS ----");
        for (int i = 0; i < sCount; i++) {
            println(i + ") " + sName[i] + " | type=" + sType[i] + " | zone=" + sZone[i] + " | enabled=" + sEnabled[i]);
        }
        if (sCount == 0) println("(none)");
    }

    static void showTriggerBuffer() {
        println("");
        println("---- RECENT EVENT BUFFER ----");
        int start = Math.max(0, trigCount - 15);
        for (int i = start; i < trigCount; i++) {
            println(i + ": sensor=" + trigSensor[i] + " time=" + trigTime[i]);
        }
        if (trigCount == 0) println("(empty)");
    }

    static void dumpLog() {
        println("");
        println("---- SYSTEM LOG ----");
        int start = Math.max(0, logPos - 60);
        for (int i = start; i < logPos; i++) {
            println(LOG[i]);
        }
        if (logPos == 0) println("(empty)");
    }

    static void log(String m) {
        if (rnd.nextInt(3) == 0) LOG[logPos++] = "LOG: " + m;
        else LOG[logPos++] = System.currentTimeMillis() + " :: " + m;
        if (logPos >= LOG.length) logPos = LOG.length - 1;
    }

    static void println(String s) {
        System.out.println(s);
    }

    static void print(String s) {
        System.out.print(s);
    }
}
