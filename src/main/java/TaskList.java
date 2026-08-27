import taskTypes.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the application's tasks and provides operations that modify them.
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this(List.of());
    }

    /**
     * Creates a task list containing the supplied tasks.
     *
     * @param tasks initial tasks
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /** Adds a task to the end of the list. */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns a task.
     *
     * @param index zero-based task index
     * @return removed task
     */
    public Task delete(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns a task without modifying it.
     *
     * @param index zero-based task index
     * @return task at the index
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /** Marks a task complete and returns it for display. */
    public Task mark(int index) {
        Task task = get(index);
        task.markDone();
        return task;
    }

    /** Marks a task incomplete and returns it for display. */
    public Task unmark(int index) {
        Task task = get(index);
        task.unmarkDone();
        return task;
    }

    /** @return the number of tasks */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns a read-only snapshot for displaying tasks without exposing mutations.
     *
     * @return immutable view of the current tasks
     */
    public List<Task> asList() {
        return List.copyOf(tasks);
    }
}
