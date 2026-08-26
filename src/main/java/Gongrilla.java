import exceptions.GongrillaException;
import taskTypes.Deadline;
import taskTypes.Event;
import taskTypes.Task;
import taskTypes.Todo;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Runs the Gongrilla chatbot and manages the user's tasks.
 */
public class Gongrilla {
    private static final String HORIZONTAL_LINE = "____________________________________________________________";
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
        String banner =
                "  _--==--_  \n" +
                " / _    _ \\ \n" +
                " \\        / \n" +
                " |  (..)  |  \n" +
                " \\   __   / \n" +
                "  \\______/  \n";

        System.out.println(HORIZONTAL_LINE);
        System.out.print(banner);
        System.out.println("Ooo");
        System.out.println("Human back. Gongrilla ready.");
        System.out.println(HORIZONTAL_LINE);

        ArrayList<Task> tasks;
        try {
            tasks = STORAGE.load();
        } catch (IOException exception) {
            System.out.println("Gongrilla cannot read saved tasks: " + exception.getMessage());
            System.out.println("Fix data file, then start Gongrilla again.");
            System.out.println(HORIZONTAL_LINE);
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            System.out.println(HORIZONTAL_LINE);

            if (command.equalsIgnoreCase("bye")) {
                System.out.println("Fine. Take banana go \uD83C\uDF4C");
                System.out.println(HORIZONTAL_LINE);
                break;
            }

            try {
                if (command.equalsIgnoreCase("list")) {
                    System.out.println("Gongrilla find tasks in list:");
                    for (int i = 0; i < tasks.size(); i++) {
                        Task task = tasks.get(i);
                        System.out.println("  " + (i + 1) + "." + task);
                    }
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

                    System.out.println("Ooo. New deadline:");
                    System.out.println("  " + deadline);
                    System.out.println("Gongrilla count " + tasks.size() + " tasks.");
                } else if (command.equalsIgnoreCase("todo")
                        || command.regionMatches(true, 0, "todo ", 0, 5)) {
                    String name = command.substring("todo".length()).trim();
                    if (name.isBlank()) {
                        throw new GongrillaException("Empty task. What Gongrilla do? Give something.");
                    }
                    Todo todo = new Todo(name);
                    STORAGE.appendAdd(todo);
                    tasks.add(todo);

                    System.out.println("Ooo. New todo:");
                    System.out.println("  " + todo);
                    System.out.println("Gongrilla count " + tasks.size() + " tasks.");
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

                    System.out.println("Ooo. New event:");
                    System.out.println("  " + event);
                    System.out.println("Gongrilla count " + tasks.size() + " tasks.");
                } else if (command.equalsIgnoreCase("delete")
                        || command.regionMatches(true, 0, "delete ", 0, 7)) {
                    int index = parseTaskIndex(command, "delete", tasks.size());
                    if (index == -1) {
                        throw new GongrillaException("Task number bad. Gongrilla look. Gongrilla find nothing.");
                    } else {
                        Task removedTask = tasks.get(index);
                        STORAGE.appendDelete(index);
                        tasks.remove(index);
                        System.out.println("Gongrilla remove task:");
                        System.out.println("  " + removedTask);
                        System.out.println("Now Gongrilla count " + tasks.size() + " tasks in list.");
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
                            task.markDone();
                        }
                        System.out.println("Banana! Gongrilla happy.");
                        System.out.println("  " + task.getIsDoneStatus() + " " + task.getName());
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
                            task.unmarkDone();
                        }
                        System.out.println("No Banana! Gongrilla sad.");
                        System.out.println("  " + task.getIsDoneStatus() + " " + task.getName());
                    }
                } else {
                    throw new GongrillaException(
                            "Hmm. Gongrilla no know that :-(");
                }
            } catch (GongrillaException | IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            } catch (DateTimeParseException exception) {
                System.out.println(
                        "Gongrilla cannot understand that date and time. "
                                + "Use D/M/YYYY with optional HHMM, like 2/12/2019 1800.");
            } catch (IOException exception) {
                System.out.println("Gongrilla cannot save that change: " + exception.getMessage());
                System.out.println("Task list was not changed.");
            }
            System.out.println(HORIZONTAL_LINE);
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
