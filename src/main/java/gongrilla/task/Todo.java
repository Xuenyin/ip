package gongrilla.task;

/**
 * Represents a task without an attached date or time.
 */
public class Todo extends Task {
    public Todo(String name) {
        super(name);
    }

    @Override
    public String toDataString() {
        return "T2 | " + commonDataFields();
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
