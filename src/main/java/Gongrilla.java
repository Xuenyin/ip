import java.util.Scanner;

public class Gongrilla {
    private static final String HORIZONTAL_LINE = "____________________________________________________________";

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
        System.out.println("Hello! I'm Gongrilla.");
        System.out.println("What can I do for you?");
        System.out.println(HORIZONTAL_LINE);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(HORIZONTAL_LINE);

            if (command.equalsIgnoreCase("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(HORIZONTAL_LINE);
                break;
            }

            if (command.equalsIgnoreCase("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    Task task = tasks[i];
                    System.out.println((i + 1) + "." + task.getIsDoneStatus() + " " + task.getName());
                }
            } else if (command.equalsIgnoreCase("mark")
                    || command.regionMatches(true, 0, "mark ", 0, 5)) {
                int index = parseTaskIndex(command, "mark", taskCount);
                if (index == -1) {
                    System.out.println("Please enter a valid task number.");
                } else {
                    Task task = tasks[index];
                    task.markDone();
                    System.out.println("Nice! I've marked this task as done:");
                    System.out.println("  " + task.getIsDoneStatus() + " " + task.getName());
                }
            } else if (command.equalsIgnoreCase("unmark")
                    || command.regionMatches(true, 0, "unmark ", 0, 7)) {
                int index = parseTaskIndex(command, "unmark", taskCount);
                if (index == -1) {
                    System.out.println("Please enter a valid task number.");
                } else {
                    Task task = tasks[index];
                    task.unmarkDone();
                    System.out.println("OK, I've marked this task as not done yet:");
                    System.out.println("  " + task.getIsDoneStatus() + " " + task.getName());
                }
            } else {
                tasks[taskCount] = new Task(command);
                taskCount++;
                System.out.println("added: " + command);
            }
            System.out.println(HORIZONTAL_LINE);
        }
    }

    private static int parseTaskIndex(String command, String keyword, int taskCount) {
        try {
            int taskNumber = Integer.parseInt(command.substring(keyword.length()).trim());
            return taskNumber >= 1 && taskNumber <= taskCount ? taskNumber - 1 : -1;
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}
