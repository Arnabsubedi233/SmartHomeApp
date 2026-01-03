import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

enum SensorType { DOOR, WINDOW, MOTION, CAMERA, SMOKE }

final class Sensor {
    private final int id;
    private final String name;
    private final SensorType type;
    private final int zone;
    private final boolean enabled;

    Sensor(int id, String name, SensorType type, int zone, boolean enabled) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.zone = zone;
        this.enabled = enabled;
    }

    int getId() { return id; }
    String getName() { return name; }
    SensorType getType() { return type; }
    int getZone() { return zone; }
    boolean isEnabled() { return enabled; }

    Sensor withEnabled(boolean v) {
        return new Sensor(id, name, type, zone, v);
    }
}

final class SensorEvent {
    private final long timestampMillis;
    private final int sensorId;

    SensorEvent(long timestampMillis, int sensorId) {
        this.timestampMillis = timestampMillis;
        this.sensorId = sensorId;
    }

    long getTimestampMillis() { return timestampMillis; }
    int getSensorId() { return sensorId; }
}

final class ContactConfig {
    private final String homeownerPhone;
    private final String homeownerEmail;
    private final String authorityPhone;

    ContactConfig(String homeownerPhone, String homeownerEmail, String authorityPhone) {
        this.homeownerPhone = homeownerPhone;
        this.homeownerEmail = homeownerEmail;
        this.authorityPhone = authorityPhone;
    }

    String getHomeownerPhone() { return homeownerPhone; }
    String getHomeownerEmail() { return homeownerEmail; }
    String getAuthorityPhone() { return authorityPhone; }

    ContactConfig withHomeownerPhone(String v) { return new ContactConfig(v, homeownerEmail, authorityPhone); }
    ContactConfig withHomeownerEmail(String v) { return new ContactConfig(homeownerPhone, v, authorityPhone); }
    ContactConfig withAuthorityPhone(String v) { return new ContactConfig(homeownerPhone, homeownerEmail, v); }
}

final class AlarmConfig {
    private final int confirmationWindowSeconds;
    private final int minDistinctSensorsToConfirm;
    private final int pendingExpirySeconds;

    AlarmConfig(int confirmationWindowSeconds, int minDistinctSensorsToConfirm, int pendingExpirySeconds) {
        this.confirmationWindowSeconds = confirmationWindowSeconds;
        this.minDistinctSensorsToConfirm = minDistinctSensorsToConfirm;
        this.pendingExpirySeconds = pendingExpirySeconds;
    }

    int getConfirmationWindowSeconds() { return confirmationWindowSeconds; }
    int getMinDistinctSensorsToConfirm() { return minDistinctSensorsToConfirm; }
    int getPendingExpirySeconds() { return pendingExpirySeconds; }

    AlarmConfig withConfirmationWindowSeconds(int v) { return new AlarmConfig(v, minDistinctSensorsToConfirm, pendingExpirySeconds); }
    AlarmConfig withMinDistinctSensorsToConfirm(int v) { return new AlarmConfig(confirmationWindowSeconds, v, pendingExpirySeconds); }
    AlarmConfig withPendingExpirySeconds(int v) { return new AlarmConfig(confirmationWindowSeconds, minDistinctSensorsToConfirm, v); }

    public String toString() {
        return "AlarmConfig{windowSec=" + confirmationWindowSeconds
                + ", minDistinct=" + minDistinctSensorsToConfirm
                + ", pendingExpirySec=" + pendingExpirySeconds + "}";
    }
}

enum AlarmStatus { NONE, PENDING, CONFIRMED }

final class AlarmDecision {
    private final AlarmStatus status;
    private final String message;

    AlarmDecision(AlarmStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    AlarmStatus getStatus() { return status; }
    String getMessage() { return message; }
}

final class PendingAlarm {
    private final long createdAtMillis;
    private final int sensorId;
    private final String reason;

    PendingAlarm(long createdAtMillis, int sensorId, String reason) {
        this.createdAtMillis = createdAtMillis;
        this.sensorId = sensorId;
        this.reason = reason;
    }

    long getCreatedAtMillis() { return createdAtMillis; }
    int getSensorId() { return sensorId; }
    String getReason() { return reason; }
}

enum PendingDecision { CONFIRM, SUPPRESS, DEFER }

final class SensorEventResult {
    private final String userMessage;
    private final String notificationSummary;

    SensorEventResult(String userMessage, String notificationSummary) {
        this.userMessage = userMessage;
        this.notificationSummary = notificationSummary;
    }

    String getUserMessage() { return userMessage; }
    String getNotificationSummary() { return notificationSummary; }
}

final class PendingActionResult {
    private final String userMessage;
    private final String notificationSummary;

    PendingActionResult(String userMessage, String notificationSummary) {
        this.userMessage = userMessage;
        this.notificationSummary = notificationSummary;
    }

    String getUserMessage() { return userMessage; }
    String getNotificationSummary() { return notificationSummary; }
}

final class ServiceResult {
    static final int OK = 0;

    private final int code;
    private final String message;

    ServiceResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    int getCode() { return code; }
    String getMessage() { return message; }
    boolean isOk() { return code == OK; }
}

final class NotificationSummary {
    private final ServiceResult sms;
    private final ServiceResult email;
    private final ServiceResult authority;

    NotificationSummary(ServiceResult sms, ServiceResult email, ServiceResult authority) {
        this.sms = sms;
        this.email = email;
        this.authority = authority;
    }

    ServiceResult getSms() { return sms; }
    ServiceResult getEmail() { return email; }
    ServiceResult getAuthority() { return authority; }

    String toDisplayString() {
        return "Delivery summary: SMS=" + (sms.isOk() ? "OK" : ("FAILED(" + sms.getCode() + ")"))
                + ", Email=" + (email.isOk() ? "OK" : ("FAILED(" + email.getCode() + ")"))
                + ", Authority=" + (authority.isOk() ? "OK" : ("FAILED(" + authority.getCode() + ")"));
    }
}

interface NotificationService {
    NotificationSummary notifyAll(ContactConfig contacts, String subject, String details);
}

final class SystemSnapshot {
    private final boolean armed;
    private final ContactConfig contacts;
    private final AlarmConfig alarmConfig;
    private final List<Sensor> sensors;

    SystemSnapshot(boolean armed, ContactConfig contacts, AlarmConfig alarmConfig, List<Sensor> sensors) {
        this.armed = armed;
        this.contacts = contacts;
        this.alarmConfig = alarmConfig;
        this.sensors = new ArrayList<Sensor>(sensors);
    }

    boolean isArmed() { return armed; }
    ContactConfig getContacts() { return contacts; }
    AlarmConfig getAlarmConfig() { return alarmConfig; }
    List<Sensor> getSensors() { return Collections.unmodifiableList(sensors); }
}
