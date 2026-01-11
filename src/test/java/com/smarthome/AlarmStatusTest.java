package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlarmStatusTest {
    @Test
    void testEnumValues() {
        assertEquals(AlarmStatus.NONE, AlarmStatus.valueOf("NONE"));
        assertEquals(AlarmStatus.PENDING, AlarmStatus.valueOf("PENDING"));
        assertEquals(AlarmStatus.CONFIRMED, AlarmStatus.valueOf("CONFIRMED"));
    }
}

