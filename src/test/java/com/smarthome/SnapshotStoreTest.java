package com.smarthome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

class SnapshotStoreTest {
    @Test
    void testSaveAndLoad() throws IOException {
        String path = "test_snapshot.properties";
        ContactConfig contact = new ContactConfig("+1", "a@b.com", "+2");
        AlarmConfig alarm = new AlarmConfig(10, 2, 5);
        List<Sensor> sensors = List.of();
        SystemSnapshot snap = new SystemSnapshot(true, contact, alarm, sensors);
        SnapshotStore store = new SnapshotStore(path);
        store.save(snap);
        SystemSnapshot loaded = store.load();
        assertEquals(snap.isArmed(), loaded.isArmed());
        assertEquals(snap.getContacts().getHomeownerPhone(), loaded.getContacts().getHomeownerPhone());
        assertEquals(snap.getAlarmConfig().getConfirmationWindowSeconds(), loaded.getAlarmConfig().getConfirmationWindowSeconds());
        new File(path).delete();
    }
}

