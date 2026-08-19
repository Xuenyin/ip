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

            System.out.println(command);
            System.out.println(HORIZONTAL_LINE);
        }

    }
}
