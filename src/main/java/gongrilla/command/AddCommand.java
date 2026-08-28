package gongrilla.command;

import gongrilla.exception.GongrillaException;
import gongrilla.storage.Storage;
import gongrilla.task.Task;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

import java.io.IOException;

/**
 * Adds a task to the task list and records the addition in storage.
 */
public class AddCommand extends Command {
    private final Task task;
    private final String taskType;

    /**
     * Creates a command that adds the supplied task.
     *
     * @param task task that should be added.
     * @param taskType user-facing type name, such as {@code todo}.
     */
    public AddCommand(Task task, String taskType) {
        this.task = task;
        this.taskType = taskType;
    }

    /**
     * Saves and adds the task, then displays the result.
     *
     * @param tasks task list to modify.
     * @param ui user interface used to display the added task.
     * @param storage storage used to persist the addition.
     * @throws GongrillaException if the command cannot be completed.
     * @throws IOException if the addition cannot be saved.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws GongrillaException, IOException {
        String taskTypeName = switch (taskType) {
            case "T" -> "todo";
            case "D" -> "deadline";
            case "E" -> "event";
            default -> throw new IllegalArgumentException("Unknown task type: " + taskType);
        };

        storage.appendAdd(task);
        tasks.add(task);
        ui.showAddedTask(taskTypeName, task, tasks.size());
    }
}
