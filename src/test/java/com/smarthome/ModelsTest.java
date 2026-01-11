package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelsTest {
    @Test
    void testServiceResult() {
        ServiceResult ok = new ServiceResult(0, "ok");
        assertTrue(ok.isOk());
        assertEquals(0, ok.getCode());
        assertEquals("ok", ok.getMessage());
    }

    @Test
    void testNotificationSummary() {
        ServiceResult sms = new ServiceResult(0, "ok");
        ServiceResult email = new ServiceResult(1, "fail");
        ServiceResult auth = new ServiceResult(0, "ok");
        NotificationSummary summary = new NotificationSummary(sms, email, auth);
        assertEquals(sms, summary.getSms());
        assertEquals(email, summary.getEmail());
        assertEquals(auth, summary.getAuthority());
        String display = summary.toDisplayString();
        assertTrue(display.contains("SMS=OK"));
        assertTrue(display.contains("Email=FAILED(1)"));
        assertTrue(display.contains("Authority=OK"));
    }
}

