package gongrilla.command;

import gongrilla.storage.Storage;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

/**
 * Displays every task currently in the task list.
 */
public class ListCommand extends Command {
    /**
     * Displays the current task list without modifying it.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // TODO: Ask Ui to display an immutable snapshot of TaskList.
        ui.showTaskList(tasks.asList());
    }
}
