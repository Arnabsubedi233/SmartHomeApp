import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class AuditLog {
    private final Deque<String> lines;
    private final int capacity;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public AuditLog(int capacity) {
        this.capacity = Math.max(50, capacity);
        this.lines = new ArrayDeque<String>(this.capacity);
    }

    public void info(String msg) { append("INFO", msg); }
    public void warn(String msg) { append("WARN", msg); }
    public void error(String msg) { append("ERROR", msg); }

    public List<String> snapshot() {
        return new ArrayList<String>(lines);
    }

    public String fmtTime(long millis) {
        return fmt.format(Instant.ofEpochMilli(millis));
    }

    private void append(String level, String msg) {
        String line = fmtTime(System.currentTimeMillis()) + " [" + level + "] " + msg;
        if (lines.size() >= capacity) lines.removeFirst();
        lines.addLast(line);
    }
}
