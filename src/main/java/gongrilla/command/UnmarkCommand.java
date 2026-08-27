package gongrilla.command;

import gongrilla.exception.GongrillaException;
import gongrilla.storage.Storage;
import gongrilla.task.Task;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

import java.io.IOException;

/**
 * Marks one task as incomplete.
 */
public class UnmarkCommand extends Command {
    private final int index;

    /** @param index zero-based index of the task */
    public UnmarkCommand(int index) {
        this.index = index;
    }

    /** Marks the task incomplete and records the change. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws GongrillaException, IOException {
        /*
         * TODO:
         * 1. Validate and retrieve the task.
         * 2. Check whether it is currently completed.
         * 3. If necessary, record the unmark operation first.
         * 4. Unmark it through TaskList.
         * 5. Display the result.
         */
        if (this.index < 0 || this.index >= tasks.size()) {
            throw new GongrillaException("No task there. Human seeing things?");
        } else {
            Task task = tasks.get(index);
            if (task.isDone()) {
                storage.appendUnmark(index);
                task = tasks.unmark(index);
            }
            ui.showUnmarkedTask(task);
        }
    }
}
