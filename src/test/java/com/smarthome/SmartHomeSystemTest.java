package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;

class DummyObserver implements Observer {
    @Override
    public void update(SystemEvent event) {
        // do nothing
    }
}

class SmartHomeSystemTest {
    @Test
    void testAddSensorAndToggle() {
        EventSubject subject = new EventSubject();
        subject.register(new DummyObserver());
        SmartHomeSystem sys = new SmartHomeSystem(
            new ContactConfig("+1234567890", "a@b.com", "+0987654321"),
            new AlarmEngine(new AlarmConfig(10, 2, 5)),
            (c, s, d) -> null,
            new AuditLog(10),
            subject
        );
        int id = sys.addSensor("FrontDoor", SensorType.DOOR, 1);
        assertEquals(0, id);
        // Sensor is enabled by default, so first toggle disables (returns false), second enables (returns true)
        assertFalse(sys.toggleSensor(0));
        assertTrue(sys.toggleSensor(0));
    }

    @Test
    void testUpdateContacts() {
        EventSubject subject = new EventSubject();
        subject.register(new DummyObserver());
        SmartHomeSystem sys = new SmartHomeSystem(
            new ContactConfig("+1234567890", "a@b.com", "+0987654321"),
            new AlarmEngine(new AlarmConfig(10, 2, 5)),
            (c, s, d) -> null,
            new AuditLog(10),
            subject
        );
        assertTrue(sys.updateContacts(new ContactConfig("+1111111111", "b@c.com", "+2222222222")));
        assertFalse(sys.updateContacts(new ContactConfig("bad", "bad", "bad")));
    }
}
