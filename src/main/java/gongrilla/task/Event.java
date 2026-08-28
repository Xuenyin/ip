package gongrilla.task;

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

    /**
     * Creates an incomplete event covering the supplied time range.
     *
     * @param name description of the event.
     * @param from event start date and time.
     * @param to event end date and time.
     * @throws IllegalArgumentException if {@code from} is after {@code to}.
     */
    public Event(String name, LocalDateTime from, LocalDateTime to) {
        super(name);
        this.from = Objects.requireNonNull(from, "Where start time? gongrilla.Gongrilla need.");
        this.to = Objects.requireNonNull(to, "Where end time? gongrilla.Gongrilla need.");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Start time cannot be after end time. Even banana know that.");
        }
    }

    /**
     * Converts this event into one encoded storage record.
     *
     * @return serialized event record.
     */
    @Override
    public String toDataString() {
        return "E2 | " + commonDataFields() + " | " + encodeField(from.toString())
                + " | " + encodeField(to.toString());
    }

    /**
     * Returns the event's user-facing representation with its time range.
     *
     * @return formatted event.
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from.format(DISPLAY_DATE_TIME_FORMAT)
                + " to: " + to.format(DISPLAY_DATE_TIME_FORMAT) + ")";
    }
}
