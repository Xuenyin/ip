package gongrilla.parser;

import gongrilla.command.AddCommand;
import gongrilla.command.Command;
import gongrilla.command.DeleteCommand;
import gongrilla.command.ExitCommand;
import gongrilla.command.ListCommand;
import gongrilla.command.MarkCommand;
import gongrilla.command.UnmarkCommand;
import gongrilla.exception.GongrillaException;
import gongrilla.task.Deadline;
import gongrilla.task.Event;
import gongrilla.task.Todo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/**
 * Converts raw user input into executable command objects.
 */
public class Parser {
    private static final List<DateTimeFormatter> INPUT_DATE_TIME_FORMATS = List.of(
            strictFormatter("d/M/uuuu HHmm"),
            strictFormatter("uuuu-MM-dd HHmm"));
    private static final List<DateTimeFormatter> INPUT_DATE_FORMATS = List.of(
            strictFormatter("d/M/uuuu"),
            DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT));

    /** Prevents construction because Parser does not require object state. */
    private Parser() {
        // Utility class.
    }

    /**
     * Parses one complete line of user input.
     *
     * @param command raw command entered by the user.
     * @return command representing the requested action.
     * @throws GongrillaException if the command is missing or malformed.
     */
    public static Command parse(String command) throws GongrillaException {
        command = command == null ? "" : command.trim();
        if (command.equalsIgnoreCase("bye")) {
            return new ExitCommand();
        } else if (command.equalsIgnoreCase("list")) {// listCommand needs to be executed
            return new ListCommand();
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
                throw new GongrillaException("No task. What gongrilla.Gongrilla supposed to do?");
            }
            if (parts[1].isBlank()) {
                throw new GongrillaException("When task due? gongrilla.Gongrilla need date or date-time.");
            }
            String name = parts[0].trim();
            String by = parts[1].trim();
            LocalDateTime byDateTime = parseDateTime(by);
            Deadline deadline = new Deadline(name, byDateTime);

            return new AddCommand(deadline, "D");
        } else if (command.equalsIgnoreCase("todo")
                || command.regionMatches(true, 0, "todo ", 0, 5)) {
            String name = command.substring("todo".length()).trim();
            if (name.isBlank()) {
                throw new GongrillaException("Empty task. What gongrilla.Gongrilla do? Give something.");
            }
            Todo todo = new Todo(name);

            return new AddCommand(todo, "T");
        } else if (command.equalsIgnoreCase("event")
                || command.regionMatches(true, 0, "event ", 0, 6)) {
            String details = command.substring("event".length()).trim();
            String[] descriptionAndTimes = details.split("(?i)\\s+/from\\s+", 2);

            if (descriptionAndTimes.length < 2) {
                throw new GongrillaException(
                        "Ooo? Event need: <task> /from D/M/YYYY [HHMM] "
                                + "/to D/M/YYYY [HHMM]");
            }
            String[] fromAndTo = descriptionAndTimes[1].split("(?i)\\s+/to\\s+", 2);
            if (fromAndTo.length < 2) {
                throw new GongrillaException(
                        "Ooo? Event need: <task> /from D/M/YYYY [HHMM] "
                                + "/to D/M/YYYY [HHMM]");
            }
            if (descriptionAndTimes[0].isBlank()) {
                throw new GongrillaException("Task missing. No task, no banana.");
            }
            if (fromAndTo[0].isBlank() || fromAndTo[1].isBlank()) {
                throw new GongrillaException(
                        "When event start? When event end? gongrilla.Gongrilla need know.");
            }
            String name = descriptionAndTimes[0].trim();
            String from = fromAndTo[0].trim();
            String to = fromAndTo[1].trim();
            LocalDateTime fromDateTime = parseDateTime(from);
            LocalDateTime toDateTime = parseDateTime(to);

            Event event = new Event(name, fromDateTime, toDateTime);

            return new AddCommand(event, "E");
        } else if (command.equalsIgnoreCase("delete")
                || command.regionMatches(true, 0, "delete ", 0, 7)) {
            int index = parseTaskIndex(command, "delete");

            return new DeleteCommand(index);
        } else if (command.equalsIgnoreCase("mark")
                || command.regionMatches(true, 0, "mark ", 0, 5)) {
            int index = parseTaskIndex(command, "mark");
            return new MarkCommand(index);
        } else if (command.equalsIgnoreCase("unmark")
                || command.regionMatches(true, 0, "unmark ", 0, 7)) {
            int index = parseTaskIndex(command, "unmark");
            return new UnmarkCommand(index);
        } else {
            throw new GongrillaException(
                    "Hmm. gongrilla.Gongrilla no know that :-(");
        }
    }

    /**
     * Converts a user-facing task number into a zero-based index.
     *
     * @param command complete command containing the task number.
     * @param keyword command keyword that precedes the number.
     * @return zero-based task index.
     * @throws GongrillaException if the number is missing, malformed, signed, zero, or too large.
     */
    private static int parseTaskIndex(String command, String keyword)
            throws GongrillaException {
        String value = command.substring(keyword.length()).trim();
        if (value.isEmpty()) {
            throw new GongrillaException("No number. Gongrilla pick air?");
        }
        if (value.startsWith("+")) {
            throw new GongrillaException(
                    "Don't put +. Number already positive. Human make simple thing hard.");
        }
        if (value.startsWith("-")) {
            throw new GongrillaException("Negative task? What next, negative banana?");
        }
        if (!value.matches("[0-9]+")) {
            throw new GongrillaException("Dis not number. Even banana know number.");
        }
        try {
            int taskNumber = Integer.parseInt(value);
            if (taskNumber == 0) {
                throw new GongrillaException("Task 0? Human counting start at 1. ");
            }
            return taskNumber - 1;
        } catch (NumberFormatException exception) {
            throw new GongrillaException("Number too big. Gongrilla run out of fingers.");
        }
    }


    /**
     * Parses either a date or a date and time. A date without a time starts at midnight.
     *
     * @param value date text entered by the user.
     * @return parsed date and time.
     * @throws DateTimeParseException if none of the supported formats match.
     */
    private static LocalDateTime parseDateTime(String value) throws GongrillaException {
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

    /**
     * Creates a locale-stable, strict formatter for a user-input pattern.
     *
     * @param pattern date-time pattern accepted from user input.
     * @return strict English-language formatter.
     */
    private static DateTimeFormatter strictFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT);
    }
}
