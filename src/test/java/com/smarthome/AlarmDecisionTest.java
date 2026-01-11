package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlarmDecisionTest {
    @Test
    void testAlarmDecisionGetters() {
        AlarmDecision decision = new AlarmDecision(AlarmStatus.CONFIRMED, "Alarm triggered");
        assertEquals(AlarmStatus.CONFIRMED, decision.getStatus());
        assertEquals("Alarm triggered", decision.getMessage());
    }
}
