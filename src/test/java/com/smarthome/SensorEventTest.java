package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SensorEventTest {
    @Test
    void testSensorEventGetters() {
        long now = System.currentTimeMillis();
        SensorEvent event = new SensorEvent(now, 42);
        assertEquals(now, event.getTimestampMillis());
        assertEquals(42, event.getSensorId());
    }
}

