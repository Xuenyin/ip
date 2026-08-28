package gongrilla.command;

import gongrilla.exception.GongrillaException;
import gongrilla.storage.Storage;
import gongrilla.task.Task;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

import java.io.IOException;

/**
 * Deletes one task identified by its zero-based index.
 */
public class DeleteCommand extends Command {
    private final int index;

    /**
     * Creates a command that deletes a task.
     *
     * @param index zero-based index of the task
     */
    public DeleteCommand(int index) {
        this.index = index;
    }

    /**
     * Validates, saves, and performs the deletion.
     *
     * @param tasks task list to modify
     * @param ui user interface used to display the deleted task
     * @param storage storage used to persist the deletion
     * @throws GongrillaException if the index does not identify a task
     * @throws IOException if the deletion cannot be saved
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws GongrillaException, IOException {
        /*
         * TODO:
         * 1. Validate the index.
         * 2. Obtain the task for later display.
         * 3. Record the deletion in storage.
         * 4. Delete it from TaskList.
         * 5. Display the deleted task and new count.
         */
        if (this.index < 0 || this.index >= tasks.size()) {
            throw new GongrillaException("No task there. Human seeing things?");
        } else {
            Task removedTask = tasks.get(index);
            storage.appendDelete(index);
            tasks.delete(index);
            ui.showDeletedTask(removedTask, tasks.size());
        }
    }
}
