package taskTypes;

/**
 * Represents the shared description and completion state of a task.
 */
public class Task {
    private final String name;
    private boolean isDone;

    /**
     * Creates an incomplete task with the given description.
     *
     * @param name description of the task
     */
    public Task(String name) {
        this.name = name;
        this.isDone = false;
    }

    public String getName() {
        return name;
    }

    public String getIsDoneStatus() {
        return isDone ? "[X]" : "[ ]";
    }

    public void markDone() {
        this.isDone = true;
    }

    public void unmarkDone() {
        this.isDone = false;
    }

    @Override
    public String toString() {
        return getIsDoneStatus() + " " + this.name;
    }
}
