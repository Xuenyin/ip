import gongrilla.exception.GongrillaException;
import gongrilla.storage.Storage;
import gongrilla.task.*;
import gongrilla.ui.Ui;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/**
 * Runs the Gongrilla chatbot and manages the user's tasks.
 */
public class Gongrilla {
    private static final Storage STORAGE = new Storage(Path.of("data", "gongrilla.txt"));
    private static final List<DateTimeFormatter> INPUT_DATE_TIME_FORMATS = List.of(
            strictFormatter("d/M/uuuu HHmm"),
            strictFormatter("uuuu-MM-dd HHmm"));
    private static final List<DateTimeFormatter> INPUT_DATE_FORMATS = List.of(
            strictFormatter("d/M/uuuu"),
            DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT));

    /**
     * Reads and processes commands until the user enters {@code bye}.
    **/
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

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.showLine();

            if (command.equalsIgnoreCase("bye")) {
                ui.showGoodbye();
                break;
            }

            try {
                if (command.equalsIgnoreCase("list")) {
                    ui.showTaskList(tasks.asList());
                } else if (command.equalsIgnoreCase("deadline")
                        || command.regionMatches(true, 0, "deadline ", 0, 9)) {
                    String details = command.substring("deadline".length()).trim();
                    String[] parts = details.split("(?i)\\s+/by\\s+", 2);
                    // (?i) -> ignore case, \\s+ -> matches one or more spaces
                    if (parts.length < 2) {
                        throw new GongrillaException(
                                "Ooo? Deadline need: <task> /by D/M/YYYY [HHMM]");
                    }
                    if (parts[0].isBlank()) {
                        throw new GongrillaException("No task. What Gongrilla supposed to do?");
                    }
                    if (parts[1].isBlank()) {
                        throw new GongrillaException("No date. Gongrilla need know when.");
                    }
                    String name = parts[0].trim();
                    String by = parts[1].trim();
                    LocalDateTime byDateTime = parseDateTime(by);
                    Deadline deadline = new Deadline(name, byDateTime);
                    STORAGE.appendAdd(deadline);
                    tasks.add(deadline);

                    ui.showAddedTask("deadline", deadline, tasks.size());
                } else if (command.equalsIgnoreCase("todo")
                        || command.regionMatches(true, 0, "todo ", 0, 5)) {
                    String name = command.substring("todo".length()).trim();
                    if (name.isBlank()) {
                        throw new GongrillaException("Empty task. What Gongrilla do? Give something.");
                    }
                    Todo todo = new Todo(name);
                    STORAGE.appendAdd(todo);
                    tasks.add(todo);

                    ui.showAddedTask("todo", todo, tasks.size());
                } else if (command.equalsIgnoreCase("event")
                        || command.regionMatches(true, 0, "event ", 0, 6)) {
                    String details = command.substring("event".length()).trim();
                    String[] parts = details.split("(?i)\\s+/from\\s+|\\s+/to\\s+", 3);

                    if (parts.length < 3) {
                        throw new GongrillaException(
                                "Ooo? Event need: <task> /from D/M/YYYY [HHMM] "
                                        + "/to D/M/YYYY [HHMM]");
                    }
                    if (parts[0].isBlank()) {
                        throw new GongrillaException("Task missing. No task, no banana.");
                    }
                    if (parts[1].isBlank() || parts[2].isBlank()) {
                        throw new GongrillaException(
                                "Gongrilla need time. When event start and end?");
                    }
                    String name = parts[0].trim();
                    String from = parts[1].trim();
                    String to = parts[2].trim();
                    LocalDateTime fromDateTime = parseDateTime(from);
                    LocalDateTime toDateTime = parseDateTime(to);

                    Event event = new Event(name, fromDateTime, toDateTime);
                    STORAGE.appendAdd(event);
                    tasks.add(event);

                    ui.showAddedTask("event", event, tasks.size());
                } else if (command.equalsIgnoreCase("delete")
                        || command.regionMatches(true, 0, "delete ", 0, 7)) {
                    int index = parseTaskIndex(command, "delete", tasks.size());
                    if (index == -1) {
                        throw new GongrillaException("Task number bad. Gongrilla look. Gongrilla find nothing.");
                    } else {
                        Task removedTask = tasks.get(index);
                        STORAGE.appendDelete(index);
                        tasks.delete(index);
                        ui.showDeletedTask(removedTask, tasks.size());
                    }
                } else if (command.equalsIgnoreCase("mark")
                        || command.regionMatches(true, 0, "mark ", 0, 5)) {
                    int index = parseTaskIndex(command, "mark", tasks.size());
                    if (index == -1) {
                        throw new GongrillaException("Task number bad. Gongrilla look. Gongrilla find nothing.");
                    } else {
                        Task task = tasks.get(index);
                        if (!task.isDone()) {
                            STORAGE.appendMark(index);
                            task = tasks.mark(index);
                        }
                        ui.showMarkedTask(task);
                    }
                } else if (command.equalsIgnoreCase("unmark")
                        || command.regionMatches(true, 0, "unmark ", 0, 7)) {
                    int index = parseTaskIndex(command, "unmark", tasks.size());
                    if (index == -1) {
                        throw new GongrillaException("Task number bad. Gongrilla look. Gongrilla find nothing.");
                    } else {
                        Task task = tasks.get(index);
                        if (task.isDone()) {
                            STORAGE.appendUnmark(index);
                            task = tasks.unmark(index);
                        }
                        ui.showUnmarkedTask(task);
                    }
                } else {
                    throw new GongrillaException(
                            "Hmm. Gongrilla no know that :-(");
                }
            } catch (GongrillaException | IllegalArgumentException exception) {
                ui.showError(exception.getMessage());
            } catch (DateTimeParseException exception) {
                ui.showError(
                        "Gongrilla cannot understand that date and time. "
                                + "Use D/M/YYYY with optional HHMM, like 2/12/2019 1800.");
            } catch (IOException exception) {
                ui.showSavingError(exception.getMessage());
            }
            ui.showLine();
        }
    }

    /**
     * Converts a user-facing task number into a zero-based array index.
     *
     * @param command command containing the task number
     * @param keyword command word before the task number
     * @param taskCount number of tasks currently stored
     * @return the array index, or {@code -1} when the number is invalid
     */
    private static int parseTaskIndex(String command, String keyword, int taskCount) {
        try {
            int taskNumber = Integer.parseInt(command.substring(keyword.length()).trim());
            return taskNumber >= 1 && taskNumber <= taskCount ? taskNumber - 1 : -1;
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    /**
     * Parses either a date or a date and time. A date without a time starts at midnight.
     *
     * @param value date text entered by the user
     * @return parsed date and time
     * @throws DateTimeParseException if none of the supported formats match
     */
    private static LocalDateTime parseDateTime(String value) {
        for (DateTimeFormatter formatter : INPUT_DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }
        for (DateTimeFormatter formatter : INPUT_DATE_FORMATS) {
            try {
                return LocalDate.parse(value, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }
        throw new DateTimeParseException("Unsupported date-time format", value, 0);
    }

    private static DateTimeFormatter strictFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT);
    }

}
