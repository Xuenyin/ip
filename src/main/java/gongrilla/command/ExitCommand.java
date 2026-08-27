package gongrilla.command;

import gongrilla.storage.Storage;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

/**
 * Ends the current Gongrilla session.
 */
public class ExitCommand extends Command {
    /**
     * Displays the goodbye message.
     *
     * @param tasks current task list
     * @param ui user interface used to display the goodbye message
     * @param storage task storage
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showGoodbye();
    }

    /**
     * Indicates that the application should stop after this command.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
