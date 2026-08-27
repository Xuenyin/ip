package gongrilla.ui;

import gongrilla.task.Task;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Handles all console input and output for Gongrilla.
 */
public class Ui {
    private static final String HORIZONTAL_LINE =
            "____________________________________________________________";
    private static final String BANNER =
              "  _--==--_  \n"
            + " / _    _ \\ \n"
            + " \\        / \n"
            + " |  (..)  |  \n"
            + " \\   __   / \n"
            + "  \\______/  \n";

    private final Scanner scanner;
    private final PrintStream output;

    /**
     * Creates a UI connected to the standard console streams.
     */
    public Ui() {
        this(System.in, System.out);
    }

    /**
     * Creates a UI using the supplied streams, which also makes the UI testable.
     *
     * @param input source of user commands
     * @param output destination for chatbot responses
     */
    public Ui(InputStream input, PrintStream output) {
        this.scanner = new Scanner(input);
        this.output = output;
    }

    /** Shows the startup banner and greeting. */
    public void showWelcome() {
        showLine();
        output.print(BANNER);
        output.println("Ooo");
        output.println("Human back. Gongrilla ready.");
        showLine();
    }

    /** @return whether another command is available */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** @return the next command without surrounding whitespace */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Shows the standard divider between interactions. */
    public void showLine() {
        output.println(HORIZONTAL_LINE);
    }

    /** Shows the farewell message. */
    public void showGoodbye() {
        output.println("Fine. Take banana go \uD83C\uDF4C");
    }

    /** Shows every task with its user-facing one-based number. */
    public void showTaskList(List<Task> tasks) {
        output.println("Gongrilla find tasks in list:");
        for (int i = 0; i < tasks.size(); i++) {
            output.println("  " + (i + 1) + "." + tasks.get(i));
        }
    }

    /** Shows a newly added task and the updated task count. */
    public void showAddedTask(String taskType, Task task, int taskCount) {
        output.println("Ooo. New " + taskType + ":");
        output.println("  " + task);
        output.println("Gongrilla count " + taskCount + " tasks.");
    }

    /** Shows a deleted task and the updated task count. */
    public void showDeletedTask(Task task, int taskCount) {
        output.println("Gongrilla remove task:");
        output.println("  " + task);
        output.println("Now Gongrilla count " + taskCount + " tasks in list.");
    }

    /** Shows that a task was marked complete. */
    public void showMarkedTask(Task task) {
        output.println("Banana! Gongrilla happy.");
        output.println("  " + task.getIsDoneStatus() + " " + task.getName());
    }

    /** Shows that a task was marked incomplete. */
    public void showUnmarkedTask(Task task) {
        output.println("No Banana! Gongrilla sad.");
        output.println("  " + task.getIsDoneStatus() + " " + task.getName());
    }

    /** Shows a user-facing error message. */
    public void showError(String message) {
        output.println(message);
    }

    /** Shows an error that prevents saved tasks from loading. */
    public void showLoadingError(String message) {
        output.println("Gongrilla cannot read saved tasks: " + message);
        output.println("Fix data file, then start Gongrilla again.");
        showLine();
    }

    /** Shows a persistence error for a command whose change was not applied. */
    public void showSavingError(String message) {
        output.println("Gongrilla cannot save that change: " + message);
        output.println("Task list was not changed.");
    }
}
