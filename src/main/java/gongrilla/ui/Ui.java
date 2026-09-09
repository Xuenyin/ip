package gongrilla.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import gongrilla.task.Task;

/**
 * Handles all console input and output for gongrilla.Gongrilla.
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
    private final PrintStream warningOutput;
    private boolean hasScheduleWarning;

    /**
     * Creates a UI connected to the standard console streams.
     */
    public Ui() {
        this(System.in, System.out);
    }

    /**
     * Creates a UI using the supplied streams, which also makes the UI testable.
     *
     * @param input source of user commands.
     * @param output destination for chatbot responses.
     */
    public Ui(InputStream input, PrintStream output) {
        this(input, output, output);
    }

    /**
     * Creates a UI that can route warnings to a separate GUI reply.
     *
     * @param input source of user commands.
     * @param output destination for normal responses.
     * @param warningOutput destination for schedule warnings.
     */
    public Ui(InputStream input, PrintStream output, PrintStream warningOutput) {
        this.scanner = new Scanner(input);
        this.output = output;
        this.warningOutput = warningOutput;
    }

    /** Shows the startup banner and greeting. */
    public void showWelcome() {
        showLine();
        output.print(BANNER);
        printLines(
                "Ooo",
                "Human back. Gongrilla ready.");
        showLine();
    }

    /**
     * Checks whether the input stream contains another command.
     *
     * @return whether another command is available.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command and removes surrounding whitespace.
     *
     * @return the next normalized command.
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Shows the standard divider between interactions. */
    public void showLine() {
        output.println(HORIZONTAL_LINE);
    }

    /** Prints each supplied line in order using the configured output stream. */
    private void printLines(String... lines) {
        for (String line : lines) {
            output.println(line);
        }
    }

    /** Shows the farewell message. */
    public void showGoodbye() {
        output.println("Fine. Take banana go \uD83C\uDF4C");
    }

    /**
     * Shows every task with its user-facing one-based number.
     *
     * @param tasks tasks to display in their current order.
     */
    public void showTaskList(List<Task> tasks) {
        output.println("Gongrilla find tasks in list:");
        printNumberedTasks(tasks);
    }

    /**
     * Shows tasks that match a find command with result-local numbering.
     *
     * @param tasks matching tasks in their original list order.
     */
    public void showMatchingTasks(List<Task> tasks) {
        output.println("Here are the matching tasks in your list:");
        printNumberedTasks(tasks);
    }

    /** Prints tasks in list order with numbering starting at one for this result. */
    private void printNumberedTasks(List<Task> tasks) {
        IntStream.range(0, tasks.size())
                .mapToObj(index -> "  " + (index + 1) + "." + tasks.get(index))
                .forEachOrdered(output::println);
    }

    /**
     * Shows a newly added task and the updated task count.
     *
     * @param taskType user-facing name of the task type.
     * @param task task that was added.
     * @param taskCount number of tasks after the addition.
     */
    public void showAddedTask(String taskType, Task task, int taskCount) {
        printLines(
                "Ooo. New " + taskType + ":",
                "  " + task,
                "Gongrilla count " + taskCount + " tasks.");
    }

    /**
     * Shows conflicts after an event has been successfully saved and added.
     *
     * @param tasks current tasks in full list order.
     * @param indices zero-based indices of conflicting tasks in list order.
     */
    public void showScheduleWarning(List<Task> tasks, List<Integer> indices) {
        hasScheduleWarning = true;
        if (warningOutput == output) {
            output.println();
        }
        warningOutput.println("Ooo. Schedule clash! Task added anyway.");
        warningOutput.println("Clashes with:");
        for (int index : indices) {
            warningOutput.println("  " + (index + 1) + "." + tasks.get(index));
        }
    }

    /**
     * Returns whether this UI has emitted a schedule warning.
     *
     * @return whether a warning was displayed.
     */
    public boolean hasScheduleWarning() {
        return hasScheduleWarning;
    }

    /**
     * Shows a deleted task and the updated task count.
     *
     * @param task task that was deleted.
     * @param taskCount number of tasks after the deletion.
     */
    public void showDeletedTask(Task task, int taskCount) {
        printLines(
                "Gongrilla remove task:",
                "  " + task,
                "Now Gongrilla count " + taskCount + " tasks in list.");
    }

    /**
     * Shows that a task was marked complete.
     *
     * @param task task whose state changed.
     */
    public void showMarkedTask(Task task) {
        printLines(
                "Banana! Gongrilla happy.",
                "  " + task.getIsDoneStatus() + " " + task.getName());
    }

    /**
     * Shows that a task was marked incomplete.
     *
     * @param task task whose state changed.
     */
    public void showUnmarkedTask(Task task) {
        printLines(
                "No Banana! Gongrilla sad.",
                "  " + task.getIsDoneStatus() + " " + task.getName());
    }

    /**
     * Shows a user-facing error message.
     *
     * @param message explanation of the error.
     */
    public void showError(String message) {
        output.println(message);
    }

    /**
     * Shows an error that prevents saved tasks from loading.
     *
     * @param message explanation supplied by the storage layer.
     */
    public void showLoadingError(String message) {
        printLines(
                "Gongrilla cannot read saved tasks: " + message,
                "Fix data file, then start gongrilla.Gongrilla again.");
        showLine();
    }

    /**
     * Shows a persistence error for a command whose change was not applied.
     *
     * @param message explanation supplied by the storage layer.
     */
    public void showSavingError(String message) {
        printLines(
                "Gongrilla cannot save that change: " + message,
                "Task list was not changed.");
    }
}
