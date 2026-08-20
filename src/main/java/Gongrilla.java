import exceptions.GongrillaException;
import taskTypes.Deadline;
import taskTypes.Event;
import taskTypes.Task;
import taskTypes.Todo;

import java.util.Scanner;

/**
 * Runs the Gongrilla chatbot and manages the user's tasks.
 */
public class Gongrilla {
    private static final String HORIZONTAL_LINE = "____________________________________________________________";

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

        Task[] tasks = new Task[100];
        int taskCount = 0;

        System.out.println(HORIZONTAL_LINE);
        System.out.print(banner);
        System.out.println("Ooo");
        System.out.println("Human back. Gongrilla ready.");
        System.out.println(HORIZONTAL_LINE);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(HORIZONTAL_LINE);

            if (command.equalsIgnoreCase("bye")) {
                System.out.println("Fine. Take banana go \uD83C\uDF4C");
                System.out.println(HORIZONTAL_LINE);
                break;
            }

            try {
                if (command.equalsIgnoreCase("list")) {
                    System.out.println("Gongrilla find tasks in list:");
                    for (int i = 0; i < taskCount; i++) {
                        Task task = tasks[i];
                        System.out.println("  " + (i + 1) + "." + task);
                    }
                } else if (command.equalsIgnoreCase("deadline")
                        || command.regionMatches(true, 0, "deadline ", 0, 9)) {
                    String details = command.substring("deadline".length()).trim();
                    String[] parts = details.split("(?i)\\s+/by\\s+", 2);
                    // (?i) -> ignore case, \\s+ -> matches one or more spaces
                    if (parts.length < 2) {
                        throw new GongrillaException(
                                "Ooo? Deadline need: <task> /by <date/time>");
                    }
                    if (parts[0].isBlank()) {
                        throw new GongrillaException("No task. What Gongrilla supposed to do?");
                    }
                    if (parts[1].isBlank()) {
                        throw new GongrillaException("No date/time. Gongrilla need know when.");
                    }
                    ensureTaskCapacity(taskCount, tasks.length);
                    String name = parts[0].trim();
                    String by = parts[1].trim();
                    Deadline deadline = new Deadline(name, by);
                    tasks[taskCount] = deadline;
                    taskCount++;

                    System.out.println("Ooo. New deadline:");
                    System.out.println("  " + deadline);
                    System.out.println("Gongrilla count " + taskCount + " tasks.");
                } else if (command.equalsIgnoreCase("todo")
                        || command.regionMatches(true, 0, "todo ", 0, 5)) {
                    String name = command.substring("todo".length()).trim();
                    if (name.isBlank()) {
                        throw new GongrillaException("Empty task. What Gongrilla do? Give Gongrilla something.");
                    }
                    ensureTaskCapacity(taskCount, tasks.length);
                    Todo todo = new Todo(name);
                    tasks[taskCount] = todo;
                    taskCount++;

                    System.out.println("Ooo. New todo:");
                    System.out.println("  " + todo);
                    System.out.println("Gongrilla count " + taskCount + " tasks.");
                } else if (command.equalsIgnoreCase("event")
                        || command.regionMatches(true, 0, "event ", 0, 6)) {
                    String details = command.substring("event".length()).trim();
                    String[] parts = details.split("(?i)\\s+/from\\s+|\\s+/to\\s+", 3);

                    if (parts.length < 3) {
                        throw new GongrillaException(
                                "Ooo? Event need: <task> /from <date/time> /to <date/time>");
                    }
                    if (parts[0].isBlank()) {
                        throw new GongrillaException("Task missing. No Task, No Banana.");
                    }
                    if (parts[1].isBlank() || parts[2].isBlank()) {
                        throw new GongrillaException(
                                "Gongrilla need time. When event start and end?");
                    }
                    ensureTaskCapacity(taskCount, tasks.length);
                    String name = parts[0].trim();
                    String from = parts[1].trim();
                    String to = parts[2].trim();

                    Event event = new Event(name, from, to);
                    tasks[taskCount] = event;
                    taskCount++;

                    System.out.println("Ooo. New event:");
                    System.out.println("  " + event);
                    System.out.println("Gongrilla count " + taskCount + " tasks.");
                } else if (command.equalsIgnoreCase("mark")
                        || command.regionMatches(true, 0, "mark ", 0, 5)) {
                    int index = parseTaskIndex(command, "mark", taskCount);
                    if (index == -1) {
                        throw new GongrillaException("Task number bad. Gongrilla look. Gongrilla find nothing.");
                    } else {
                        Task task = tasks[index];
                        task.markDone();
                        System.out.println("Banana! Gongrilla happy.");
                        System.out.println("  " + task.getIsDoneStatus() + " " + task.getName());
                    }
                } else if (command.equalsIgnoreCase("unmark")
                        || command.regionMatches(true, 0, "unmark ", 0, 7)) {
                    int index = parseTaskIndex(command, "unmark", taskCount);
                    if (index == -1) {
                        throw new GongrillaException("Task number bad. Gongrilla look. Gongrilla find nothing.");
                    } else {
                        Task task = tasks[index];
                        task.unmarkDone();
                        System.out.println("No Banana! Gongrilla sad.");
                        System.out.println("  " + task.getIsDoneStatus() + " " + task.getName());
                    }
                } else {
                    throw new GongrillaException(
                            "Hmm. Gongrilla no know that :-(");
                }
            } catch (GongrillaException exception) {
                System.out.println(exception.getMessage());
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
     * Ensures another task can be stored in the fixed-size task array.
     *
     * @param taskCount number of tasks currently stored
     * @param capacity maximum number of tasks that can be stored
     * @throws GongrillaException if the task array is full
     */
    private static void ensureTaskCapacity(int taskCount, int capacity) throws GongrillaException {
        if (taskCount >= capacity) {
            throw new GongrillaException("OOPS!!! The task list is full.");
        }
    }
}
