package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SmartHomeCLITest {
    @Test
    void testCLICreation() {
        SmartHomeSystem sys = new SmartHomeSystem(
            new ContactConfig("+1", "a@b.com", "+2"),
            new AlarmEngine(new AlarmConfig(10, 2, 5)),
            (c, s, d) -> null,
            null,
            null
        );
        SmartHomeCLI cli = new SmartHomeCLI(sys);
        assertNotNull(cli);
    }
}

