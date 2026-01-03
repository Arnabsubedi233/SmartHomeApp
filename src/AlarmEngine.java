import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AlarmEngine {
    private AlarmConfig config;
    private final Deque<SensorEvent> recentEvents = new ArrayDeque<SensorEvent>();
    private final int maxRecentEvents = 200;
    private PendingAlarm pending;

    public AlarmEngine(AlarmConfig config) {
        this.config = config;
    }

    public AlarmConfig getConfig() { return config; }

    public void updateConfig(AlarmConfig config) {
        this.config = config;
    }

    public AlarmDecision recordEvent(SensorEvent event) {
        addEvent(event);
        cleanupOldEvents(event.getTimestampMillis());
        expirePendingIfNeeded(event.getTimestampMillis());

        int distinctCount = distinctSensorsWithinWindow(event.getTimestampMillis()).size();
        if (distinctCount >= config.getMinDistinctSensorsToConfirm()) {
            clearPending();
            return new AlarmDecision(
                    AlarmStatus.CONFIRMED,
                    "Corroboration met (" + distinctCount + "/" + config.getMinDistinctSensorsToConfirm() + ") distinct sensors within "
                            + config.getConfirmationWindowSeconds() + " seconds."
            );
        }

        if (pending == null) {
            pending = new PendingAlarm(event.getTimestampMillis(), event.getSensorId(),
                    "Corroboration not met (" + distinctCount + "/" + config.getMinDistinctSensorsToConfirm() + ").");
        }

        return new AlarmDecision(
                AlarmStatus.PENDING,
                "Pending alarm: " + distinctCount + "/" + config.getMinDistinctSensorsToConfirm() + " distinct sensors in window."
        );
    }

    public boolean hasPending() { return pending != null; }

    public PendingAlarm getPendingAlarm() { return pending; }

    public void clearPending() { pending = null; }

    public void expirePendingIfNeeded() { expirePendingIfNeeded(System.currentTimeMillis()); }

    private void expirePendingIfNeeded(long nowMillis) {
        if (pending == null) return;
        long ageSec = (nowMillis - pending.getCreatedAtMillis()) / 1000L;
        if (ageSec >= config.getPendingExpirySeconds()) {
            pending = null;
        }
    }

    public List<SensorEvent> recentEventsSnapshot() {
        return new ArrayList<SensorEvent>(recentEvents);
    }

    private void addEvent(SensorEvent event) {
        recentEvents.addLast(event);
        while (recentEvents.size() > maxRecentEvents) recentEvents.removeFirst();
    }

    private void cleanupOldEvents(long nowMillis) {
        long cutoff = nowMillis - (config.getConfirmationWindowSeconds() * 1000L);
        while (!recentEvents.isEmpty() && recentEvents.peekFirst().getTimestampMillis() < cutoff) {
            recentEvents.removeFirst();
        }
    }

    private Set<Integer> distinctSensorsWithinWindow(long nowMillis) {
        long cutoff = nowMillis - (config.getConfirmationWindowSeconds() * 1000L);
        Set<Integer> sensors = new HashSet<Integer>();
        for (SensorEvent e : recentEvents) {
            if (e.getTimestampMillis() >= cutoff) sensors.add(e.getSensorId());
        }
        return sensors;
    }
}
