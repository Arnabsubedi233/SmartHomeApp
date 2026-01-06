package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {
    @Test
    void testValidSensorName() {
        assertTrue(Validator.isValidSensorName("FrontDoor"));
        assertFalse(Validator.isValidSensorName(""));
        assertFalse(Validator.isValidSensorName(null));
        assertFalse(Validator.isValidSensorName("--bad"));
    }

    @Test
    void testValidPhone() {
        assertTrue(Validator.isValidPhone("+1234567890"));
        assertFalse(Validator.isValidPhone("abc"));
    }

    @Test
    void testValidEmail() {
        assertTrue(Validator.isValidEmail("test@example.com"));
        assertFalse(Validator.isValidEmail("bademail"));
    }

    @Test
    void testMaskPhone() {
        assertEquals("****7890", Validator.maskPhone("+1234567890"));
        assertEquals("****", Validator.maskPhone("123"));
    }

    @Test
    void testMaskEmail() {
        assertEquals("t***@example.com", Validator.maskEmail("test@example.com"));
        assertEquals("***@***", Validator.maskEmail("a@b"));
    }
}

