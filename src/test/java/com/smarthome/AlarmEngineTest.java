import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.smarthome.AlarmConfig;
import com.smarthome.AlarmEngine;
import com.smarthome.SensorEvent;
import com.smarthome.AlarmDecision;
import com.smarthome.AlarmStatus;

class AlarmEngineTest {
    private AlarmConfig config;
    private AlarmEngine engine;

    @BeforeEach
    void setUp() {
        config = new AlarmConfig(10, 2, 5);
        engine = new AlarmEngine(config);
    }

    @Test
    void testRecordEventReturnsPendingWhenNotEnoughDistinctSensors() {
        SensorEvent event1 = new SensorEvent(System.currentTimeMillis(), 1);
        AlarmDecision decision = engine.recordEvent(event1);
        assertEquals(AlarmStatus.PENDING, decision.getStatus());
        assertTrue(engine.hasPending());
    }

    @Test
    void testRecordEventReturnsConfirmedWhenEnoughDistinctSensors() {
        long now = System.currentTimeMillis();
        SensorEvent event1 = new SensorEvent(now, 1);
        SensorEvent event2 = new SensorEvent(now + 100, 2);
        engine.recordEvent(event1);
        AlarmDecision decision = engine.recordEvent(event2);
        assertEquals(AlarmStatus.CONFIRMED, decision.getStatus());
        assertFalse(engine.hasPending());
    }

    @Test
    void testPendingAlarmExpiresAfterExpiryTime() throws InterruptedException {
        long now = System.currentTimeMillis();
        SensorEvent event1 = new SensorEvent(now, 1);
        engine.recordEvent(event1);
        assertTrue(engine.hasPending());
        engine.expirePendingIfNeeded(now + 6000);
        assertFalse(engine.hasPending());
    }

    @Test
    void testUpdateConfigAffectsBehavior() {
        SensorEvent event1 = new SensorEvent(System.currentTimeMillis(), 1);
        engine.recordEvent(event1);
        engine.updateConfig(new AlarmConfig(10, 1, 5));
        SensorEvent event2 = new SensorEvent(System.currentTimeMillis(), 2);
        AlarmDecision decision = engine.recordEvent(event2);
        assertEquals(AlarmStatus.CONFIRMED, decision.getStatus());
    }

    @Test
    void testRecentEventsSnapshot() {
        SensorEvent event1 = new SensorEvent(System.currentTimeMillis(), 1);
        engine.recordEvent(event1);
        assertFalse(engine.recentEventsSnapshot().isEmpty());
    }

    @Test
    void testClearPending() {
        SensorEvent event1 = new SensorEvent(System.currentTimeMillis(), 1);
        engine.recordEvent(event1);
        assertTrue(engine.hasPending());
        engine.clearPending();
        assertFalse(engine.hasPending());
    }
}

