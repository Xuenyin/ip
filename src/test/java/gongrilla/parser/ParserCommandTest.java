package gongrilla.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import gongrilla.command.Command;
import gongrilla.command.DeleteCommand;
import gongrilla.command.ExitCommand;
import gongrilla.exception.GongrillaException;
import gongrilla.storage.Storage;
import gongrilla.task.TaskList;
import gongrilla.task.Todo;
import gongrilla.ui.Ui;

/**
 * Tests command parsing and execution boundaries.
 */
class ParserCommandTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void parse_byeWithWhitespace_returnsExitCommand() throws Exception {
        Command command = Parser.parse("  BYE  ");

        assertInstanceOf(ExitCommand.class, command);
        assertTrue(command.isExit());
    }

    @Test
    void parse_deleteTwo_deletesSecondTask() throws Exception {
        TaskList tasks = new TaskList(List.of(
                new Todo("first"), new Todo("second"), new Todo("third")));
        Command command = Parser.parse("delete 2");

        command.execute(tasks, createUi(), createStorage());

        assertInstanceOf(DeleteCommand.class, command);
        assertEquals(List.of("first", "third"),
                tasks.asList().stream().map(task -> task.getName()).toList());
    }

    @Test
    void parse_taskNumberContainingOtherText_rejectsCommand() {
        GongrillaException exception = assertThrows(GongrillaException.class,
                () -> Parser.parse("delete abc2"));

        assertEquals("Dis not number. Even banana know number.", exception.getMessage());
    }

    @Test
    void parse_zeroTaskNumber_rejectsCommand() {
        GongrillaException exception = assertThrows(GongrillaException.class,
                () -> Parser.parse("mark 0"));

        assertEquals("Task 0? Human counting start at 1. ", exception.getMessage());
    }

    @Test
    void parse_taskNumberWithPlusSign_rejectsCommand() {
        GongrillaException exception = assertThrows(GongrillaException.class,
                () -> Parser.parse("mark +2"));

        assertEquals("Don't put +. Number already positive. Human make simple thing hard.",
                exception.getMessage());
    }

    @Test
    void parse_eventWithToBeforeFrom_rejectsCommand() {
        GongrillaException exception = assertThrows(GongrillaException.class,
                () -> Parser.parse(
                        "event meeting /to 3/12/2019 1700 /from 3/12/2019 0900"));

        assertEquals("Ooo? Event need: <task> /from D/M/YYYY [HHMM] "
                        + "/to D/M/YYYY [HHMM]",
                exception.getMessage());
    }

    @Test
    void delete_indexBeyondTaskList_reportsExistingError() throws Exception {
        Command command = Parser.parse("delete 2");
        TaskList tasks = new TaskList(List.of(new Todo("only task")));

        GongrillaException exception = assertThrows(GongrillaException.class,
                () -> command.execute(tasks, createUi(), createStorage()));

        assertEquals("No task there. Human seeing things?",
                exception.getMessage());
    }

    @Test
    void parse_missingTaskNumber_reportsSpecificError() {
        assertParsingError("delete", "No number. Gongrilla pick air?");
    }

    @Test
    void parse_negativeTaskNumber_reportsSpecificError() {
        assertParsingError("mark -1", "Negative task? What next, negative banana?");
    }

    @Test
    void parse_overflowingTaskNumber_reportsSpecificError() {
        assertParsingError("unmark 999999999999999999999999",
                "Number too big. Gongrilla run out of fingers.");
    }

    private void assertParsingError(String command, String expectedMessage) {
        GongrillaException exception = assertThrows(GongrillaException.class,
                () -> Parser.parse(command));
        assertEquals(expectedMessage, exception.getMessage());
    }

    private Ui createUi() {
        return new Ui(new ByteArrayInputStream(new byte[0]),
                new PrintStream(new ByteArrayOutputStream()));
    }

    private Storage createStorage() {
        return new Storage(temporaryDirectory.resolve("data/tasks.txt"));
    }
}
