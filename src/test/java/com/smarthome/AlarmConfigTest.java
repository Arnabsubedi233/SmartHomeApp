package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlarmConfigTest {
    @Test
    void testAlarmConfigGetters() {
        AlarmConfig config = new AlarmConfig(30, 2, 60);
        assertEquals(30, config.getConfirmationWindowSeconds());
        assertEquals(2, config.getMinDistinctSensorsToConfirm());
        assertEquals(60, config.getPendingExpirySeconds());
    }

    @Test
    void testWithMethods() {
        AlarmConfig config = new AlarmConfig(30, 2, 60);
        AlarmConfig updated = config.withConfirmationWindowSeconds(45);
        assertEquals(45, updated.getConfirmationWindowSeconds());
        assertEquals(2, updated.getMinDistinctSensorsToConfirm());
        assertEquals(60, updated.getPendingExpirySeconds());
    }

    @Test
    void testToString() {
        AlarmConfig config = new AlarmConfig(30, 2, 60);
        String str = config.toString();
        assertTrue(str.contains("windowSec=30"));
        assertTrue(str.contains("minDistinct=2"));
        assertTrue(str.contains("pendingExpirySec=60"));
    }
}

