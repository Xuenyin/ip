package taskTypes;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a task occurring between a start and end time.
 */
public class Event extends Task {
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu, h:mma", Locale.ENGLISH);

    @Getter
    private final LocalDateTime from;
    @Getter
    private final LocalDateTime to;

    public Event(String name, LocalDateTime from, LocalDateTime to) {
        super(name);
        this.from = Objects.requireNonNull(from, "Where start time? Gongrilla need.");
        this.to = Objects.requireNonNull(to, "Where end time? Gongrilla need.");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Start time must come before end time. Even banana know that.");
        }
    }

    @Override
    public String toDataString() {
        return "E2 | " + commonDataFields() + " | " + encodeField(from.toString())
                + " | " + encodeField(to.toString());
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from.format(DISPLAY_DATE_TIME_FORMAT)
                + " to: " + to.format(DISPLAY_DATE_TIME_FORMAT) + ")";
    }
}
