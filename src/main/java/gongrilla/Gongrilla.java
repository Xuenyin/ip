package gongrilla;

import gongrilla.command.Command;
import gongrilla.exception.GongrillaException;
import gongrilla.parser.Parser;
import gongrilla.storage.Storage;
import gongrilla.task.*;
import gongrilla.ui.Ui;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;

/**
 * Runs the gongrilla.Gongrilla chatbot and manages the user's tasks.
 */
public class Gongrilla {
    private static final Storage STORAGE = new Storage(Path.of("data", "gongrilla.txt"));

    /** Creates a Gongrilla application entry point. */
    public Gongrilla() {
    }

    /**
     * Reads and processes commands until the user enters {@code bye}.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        TaskList tasks;
        try {
            tasks = new TaskList(STORAGE.load());
        } catch (IOException exception) {
            ui.showLoadingError(exception.getMessage());
            return;
        }

        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.showLine();

            try {
                Command parsedCommand = Parser.parse(command);
                parsedCommand.execute(tasks, ui, STORAGE);
                isExit = parsedCommand.isExit();
            } catch (GongrillaException | IllegalArgumentException exception) {
                ui.showError(exception.getMessage());
            } catch (DateTimeParseException exception) {
                ui.showError(
                        "gongrilla.Gongrilla cannot understand that date and time. "
                                + "Use D/M/YYYY with optional HHMM, like 2/12/2019 1800.");
            } catch (IOException exception) {
                ui.showSavingError(exception.getMessage());
            }
            ui.showLine();
        }
    }
}
