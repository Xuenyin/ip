package gongrilla.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
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
    void execute_clashingEvent_savesOnceAndReportsOriginalIndices() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 9, 9, 10, 0);
        Event first = new Event("workshop", start, start.plusHours(1));
        TaskList tasks = new TaskList(new Todo("read"), first, new Todo("write"),
                new Event("workshop", start, start.plusHours(1)));
        Storage storage = new Storage(temporaryDirectory.resolve("clashes.txt"));
        for (Task task : tasks.asList()) {
            storage.appendAdd(task);
        }
        Event candidate = new Event("review", start.plusMinutes(30), start.plusMinutes(90));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(new ByteArrayInputStream(new byte[0]), new PrintStream(output));

        new AddCommand(candidate).execute(tasks, ui, storage);

        assertEquals(String.join(System.lineSeparator(),
                "Ooo. New event:", "  " + candidate, "Gongrilla count 5 tasks.", "",
                "Ooo. Schedule clash! Task added anyway.", "Clashes with:",
                "  2." + first, "  4." + first, ""), output.toString());
        assertTrue(ui.hasScheduleWarning());
        assertSame(candidate, tasks.get(4));
        assertEquals(tasks.asList().stream().map(Task::toDataString).toList(),
                storage.load().stream().map(Task::toDataString).toList());
        assertEquals(5, Files.readAllLines(temporaryDirectory.resolve("clashes.txt")).size());
    }

    @Test
    void execute_clashingEventWithSaveFailure_preservesMemoryAndSuppressesOutput() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 9, 9, 10, 0);
        TaskList tasks = new TaskList(new Event("existing", start, start.plusHours(1)));
        Event candidate = new Event("review", start, start.plusHours(1));
        // A directory cannot be opened as a journal file.
        Storage storage = new Storage(temporaryDirectory);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(new ByteArrayInputStream(new byte[0]), new PrintStream(output));

        assertThrows(IOException.class, () -> new AddCommand(candidate).execute(tasks, ui, storage));

        assertEquals(1, tasks.size());
        assertEquals("", output.toString());
        assertFalse(ui.hasScheduleWarning());
    }

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
