package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleNotificationServiceTest {
    @Test
    void testNotifyAllReturnsSummary() {
        AuditLog log = new AuditLog(10);
        ConsoleNotificationService service = new ConsoleNotificationService(log);
        ContactConfig contact = new ContactConfig("+1234567890", "a@b.com", "+0987654321");
        NotificationSummary summary = service.notifyAll(contact, "subject", "details");
        assertNotNull(summary);
    }
}
