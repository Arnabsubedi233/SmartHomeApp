package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    @Test
    void testAuditLogCreation() {
        AuditLog log = new AuditLog(100);
        assertNotNull(log);
    }

    @Test
    void testInfoWarnErrorAndSnapshot() {
        AuditLog log = new AuditLog(5);
        log.info("info");
        log.warn("warn");
        log.error("error");
        assertTrue(log.snapshot().size() >= 3);
    }
}
