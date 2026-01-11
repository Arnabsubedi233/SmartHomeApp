package com.smarthome;

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

    private void println(String message) {
        System.out.println(message);
    }

    private int readInt(String prompt, int min, int max) {
        int choice;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice >= min && choice <= max) {
                    scanner.nextLine(); // consume the newline
                    return choice;
                }
            } else {
                scanner.nextLine(); // consume the invalid input
            }
            System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
        }
    }

    private void securityMenu() {
        // Implementation of security monitoring menu
    }

    private void alarmMenu() {
        // Implementation of alarm confirmation and false alarm mitigation menu
    }

    private void simulateEventMenu() {
        // Implementation of sensor event simulation menu
    }

    private void viewLog() {
        // Implementation of audit log viewing
    }
}
