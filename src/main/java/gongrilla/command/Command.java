package gongrilla.command;

import gongrilla.exception.GongrillaException;
import gongrilla.storage.Storage;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

import java.io.IOException;

/**
 * Represents an instruction that can be executed by gongrilla.Gongrilla.
 *
 * <p>Each subclass represents one user operation, such as adding, deleting,
 * listing, marking, or exiting.</p>
 */
public abstract class Command {
    /** Creates a command for a parser to configure and execute. */
    protected Command() {
    }

    /**
     * Executes this command using the application's main components.
     *
     * @param tasks task list to query or modify.
     * @param ui user interface used to display results.
     * @param storage storage used to persist task-list changes.
     * @throws GongrillaException if the command cannot be completed.
     * @throws IOException if a task-list change cannot be saved.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage)
            throws GongrillaException, IOException;

    /**
     * Returns whether this command should stop the application.
     *
     * @return {@code true} if gongrilla.Gongrilla should exit.
     */
    public boolean isExit() {
        return false;
    }
}
