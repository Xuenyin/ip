package gongrilla.command;

import java.io.IOException;

import gongrilla.exception.GongrillaException;
import gongrilla.storage.Storage;
import gongrilla.task.Task;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

/**
 * Adds a task to the task list and records the addition in storage.
 */
public class AddCommand extends Command {
    private final Task task;

    /**
     * Creates a command that adds the supplied task.
     *
     * @param task task that should be added.
     */
    public AddCommand(Task task) {
        this.task = task;
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
        String taskTypeName = switch (task.getType()) {
            case TODO -> "todo";
            case DEADLINE -> "deadline";
            case EVENT -> "event";
        };

        storage.appendAdd(task);
        tasks.add(task);
        ui.showAddedTask(taskTypeName, task, tasks.size());
    }
}
