package taskTypes;

/**
 * Represents a task that must be completed by a specified time.
 */
public class Deadline extends Task {
    private final String by;

    public Deadline(String name, String by) {
        super(name);
        this.by = by;
    }

    @Override
    public String toDataString() {
        return "D2 | " + commonDataFields() + " | " + encodeField(by);
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
