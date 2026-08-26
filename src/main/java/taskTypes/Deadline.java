package taskTypes;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a task that must be completed by a specified time.
 */
public class Deadline extends Task {
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu, h:mma", Locale.ENGLISH);

    @Getter
    private final LocalDateTime by;

    public Deadline(String name, LocalDateTime by) {
        super(name);
        this.by = Objects.requireNonNull(by, "Where deadline? Gongrilla need.");
    }

    @Override
    public String toDataString() {
        return "D2 | " + commonDataFields() + " | " + encodeField(by.toString());
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: "
                + by.format(DISPLAY_DATE_TIME_FORMAT) + ")";
    }
}
