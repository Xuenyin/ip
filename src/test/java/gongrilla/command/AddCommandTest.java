package gongrilla.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import gongrilla.storage.Storage;
import gongrilla.task.Deadline;
import gongrilla.task.Event;
import gongrilla.task.Task;
import gongrilla.task.TaskList;
import gongrilla.task.Todo;
import gongrilla.ui.Ui;

/** Tests that add confirmations agree with the task being added and saved. */
class AddCommandTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void execute_supportedTasks_derivesLabelsFromTasks() throws Exception {
        LocalDateTime date = LocalDateTime.of(2026, 9, 6, 12, 0);
        List<Task> examples = List.of(new Task("basic"), new Todo("read"),
                new Deadline("report", date), new Event("meeting", date, date.plusHours(1)));
        List<String> labels = List.of("todo", "todo", "deadline", "event");
        TaskList tasks = new TaskList();
        Storage storage = new Storage(temporaryDirectory.resolve("tasks.txt"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(new ByteArrayInputStream(new byte[0]), new PrintStream(output));

        for (int i = 0; i < examples.size(); i++) {
            output.reset();
            Task task = examples.get(i);

            new AddCommand(task).execute(tasks, ui, storage);

            assertSame(task, tasks.get(i));
            assertEquals(i + 1, tasks.size());
            assertTrue(output.toString().startsWith("Ooo. New " + labels.get(i) + ":"));
            Task restored = storage.load().get(i);
            assertEquals(task.toDataString(), restored.toDataString());
            assertEquals(task.getType(), restored.getType());
        }
    }
}
