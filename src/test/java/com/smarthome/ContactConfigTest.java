package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContactConfigTest {
    @Test
    void testContactConfigGetters() {
        ContactConfig config = new ContactConfig("+1234567890", "test@example.com", "+0987654321");
        assertEquals("+1234567890", config.getHomeownerPhone());
        assertEquals("test@example.com", config.getHomeownerEmail());
        assertEquals("+0987654321", config.getAuthorityPhone());
    }

    @Test
    void testWithMethods() {
        ContactConfig config = new ContactConfig("+1234567890", "test@example.com", "+0987654321");
        ContactConfig updated = config.withHomeownerPhone("+1111111111");
        assertEquals("+1111111111", updated.getHomeownerPhone());
        assertEquals("test@example.com", updated.getHomeownerEmail());
        assertEquals("+0987654321", updated.getAuthorityPhone());
    }
}

