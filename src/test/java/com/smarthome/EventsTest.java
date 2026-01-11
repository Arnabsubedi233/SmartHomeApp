package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventsTest {
    @Test
    void testEventSubjectPublish() {
        EventSubject subject = new EventSubject();
        SystemEvent event = new SystemEvent(System.currentTimeMillis(), EventType.SYSTEM_ARMED, "msg", null);
        subject.publish(event);
        // No assertion, just ensure no exception
    }
}

