package taskTypes;

/**
 * Represents the shared description and completion state of a task.
 */
public class Task {
    private final String name;
    private CompletionStatus completionStatus;

    private enum CompletionStatus {
        NOT_DONE("[ ]"),
        DONE("[X]");

        private final String marker;

        CompletionStatus(String marker) {
            this.marker = marker;
        }

        String getMarker() {
            return marker;
        }
    }

    /**
     * Creates an incomplete task with the given description.
     *
     * @param name description of the task
     */
    public Task(String name) {
        this.name = name;
        this.completionStatus = CompletionStatus.NOT_DONE;
    }

    public String getName() {
        return name;
    }

    public String getIsDoneStatus() {
        return completionStatus.getMarker();
    }

    public void markDone() {
        this.completionStatus = CompletionStatus.DONE;
    }

    public void unmarkDone() {
        this.completionStatus = CompletionStatus.NOT_DONE;
    }

    @Override
    public String toString() {
        return getIsDoneStatus() + " " + this.name;
    }
}
