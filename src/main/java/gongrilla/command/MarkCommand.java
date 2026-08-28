package gongrilla.command;

import gongrilla.exception.GongrillaException;
import gongrilla.storage.Storage;
import gongrilla.task.Task;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

import java.io.IOException;

/**
 * Marks one task as completed.
 */
public class MarkCommand extends Command {
    private final int index;

    /**
     * Creates a command that marks one task complete.
     *
     * @param index zero-based index of the task
     */
    public MarkCommand(int index) {
        this.index = index;
    }

    /**
     * Marks the task complete, records the change, and displays the result.
     *
     * @param tasks task list to modify
     * @param ui user interface used to display the marked task
     * @param storage storage used to persist the state change
     * @throws GongrillaException if the index does not identify a task
     * @throws IOException if the state change cannot be saved
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws GongrillaException, IOException {
        /*
         * TODO:
         * 1. Validate and retrieve the task.
         * 2. Check whether it is already completed.
         * 3. If necessary, record the mark operation first.
         * 4. Mark it through TaskList.
         * 5. Display the result.
         */
        if (this.index < 0 || this.index >= tasks.size()) {
            throw new GongrillaException("No task there. Human seeing things?");
        } else {
            Task task = tasks.get(index);
            if (!task.isDone()) {
                storage.appendMark(index);
                task = tasks.mark(index);
            }
            ui.showMarkedTask(task);
        }
    }
}
