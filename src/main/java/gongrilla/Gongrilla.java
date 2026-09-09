package gongrilla;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;

import gongrilla.command.Command;
import gongrilla.exception.GongrillaException;
import gongrilla.parser.Parser;
import gongrilla.storage.Storage;
import gongrilla.task.TaskList;
import gongrilla.ui.Ui;

/**
 * Runs the gongrilla.Gongrilla chatbot and manages the user's tasks.
 */
public class Gongrilla {
    private final Storage storage;
    private final TaskList tasks;
    private final String loadingError;
    private String commandType;
    private String mainResponse = "";
    private String scheduleWarning = "";

    /** Creates Gongrilla using the normal application data file. */
    public Gongrilla() {
        this(Path.of("data", "gongrilla.txt"));
    }

    /**
     * Creates Gongrilla using a specified data file.
     *
     * @param dataPath location used to load and save tasks.
     */
    public Gongrilla(Path dataPath) {
        storage = new Storage(dataPath);

        TaskList loadedTasks;
        String loadFailure = null;
        try {
            loadedTasks = new TaskList(storage.load());
        } catch (IOException exception) {
            loadedTasks = new TaskList();
            loadFailure = exception.getMessage();
        }
        tasks = loadedTasks;
        loadingError = loadFailure;
    }

    /**
     * Reads and processes commands until the user enters {@code bye}.
     *
     * @param args command-line arguments; currently unused.
     */
    public static void main(String[] args) {
        Gongrilla gongrilla = new Gongrilla();
        Ui ui = new Ui();
        ui.showWelcome();

        if (gongrilla.loadingError != null) {
            ui.showLoadingError(gongrilla.loadingError);
            return;
        }

        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.showLine();

            isExit = gongrilla.execute(command, ui);
            ui.showLine();
        }
    }

    /**
     * Processes one command and returns the text that a graphical UI should display.
     *
     * @param input raw command entered by the user.
     * @return Gongrilla's response, without console-only divider lines.
     */
    public String getResponse(String input) {
        commandType = "Error";
        mainResponse = "";
        scheduleWarning = "";
        if (loadingError != null) {
            mainResponse = "Gongrilla cannot read saved tasks: " + loadingError;
            return mainResponse;
        }

        ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream warningBytes = new ByteArrayOutputStream();
        try (PrintStream responseOutput = new PrintStream(
                responseBytes, true, StandardCharsets.UTF_8);
                PrintStream warningOutput = new PrintStream(warningBytes, true, StandardCharsets.UTF_8)) {
            Ui responseUi = new Ui(InputStream.nullInputStream(), responseOutput, warningOutput);
            execute(input, responseUi);
        }
        mainResponse = responseBytes.toString(StandardCharsets.UTF_8).stripTrailing();
        scheduleWarning = warningBytes.toString(StandardCharsets.UTF_8).stripTrailing();
        return hasScheduleWarning()
                ? mainResponse + System.lineSeparator().repeat(2) + scheduleWarning : mainResponse;
    }

    /**
     * Returns the type of the last command processed successfully.
     *
     * @return simple command class name, or {@code Error} after an unsuccessful command.
     */
    public String getCommandType() {
        return commandType;
    }

    /**
     * Returns whether the last response contains a schedule warning.
     *
     * @return whether the GUI should use its warning color.
     */
    public boolean hasScheduleWarning() {
        return !scheduleWarning.isEmpty();
    }

    /**
     * Returns the normal reply from the last command without its schedule warning.
     *
     * @return normal reply text for the first GUI bubble.
     */
    public String getMainResponse() {
        return mainResponse;
    }

    /**
     * Returns the last command's schedule warning without executing the command again.
     *
     * @return warning text for a separate bubble, or an empty string when absent.
     */
    public String getScheduleWarning() {
        return scheduleWarning;
    }

    /** Executes one command against this instance's shared task list and storage. */
    private boolean execute(String input, Ui ui) {
        try {
            Command parsedCommand = Parser.parse(input);
            parsedCommand.execute(tasks, ui, storage);
            commandType = parsedCommand.getClass().getSimpleName();
            return parsedCommand.isExit();
        } catch (GongrillaException | IllegalArgumentException exception) {
            ui.showError(exception.getMessage());
        } catch (DateTimeParseException exception) {
            ui.showError(
                    "Gongrilla cannot understand that date and time. "
                            + "Use D/M/YYYY with optional HHMM, like 2/12/2019 1800.");
        } catch (IOException exception) {
            ui.showSavingError(exception.getMessage());
        }
        return false;
    }
}
