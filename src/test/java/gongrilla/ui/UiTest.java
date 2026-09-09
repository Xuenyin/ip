package gongrilla.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import gongrilla.task.Todo;

/** Tests that multiline responses retain their text and line separators. */
class UiTest {
    @Test
    void showTaskList_multipleTasks_preservesOrderAndCompletionMarkers() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(InputStream.nullInputStream(), new PrintStream(output));
        Todo completed = new Todo("second");
        completed.markDone();

        ui.showTaskList(List.of(new Todo("first"), completed));

        assertEquals(String.join(System.lineSeparator(), "Gongrilla find tasks in list:",
                "  1.[T][ ] first", "  2.[T][X] second", ""), output.toString());
    }

    @Test
    void showMatchingTasks_filteredResults_numbersFromOne() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(InputStream.nullInputStream(), new PrintStream(output));

        ui.showMatchingTasks(List.of(new Todo("second"), new Todo("fourth")));

        assertEquals(String.join(System.lineSeparator(), "Here are the matching tasks in your list:",
                "  1.[T][ ] second", "  2.[T][ ] fourth", ""), output.toString());
    }

    @Test
    void showTaskLists_emptyResults_printsOnlyHeaders() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(InputStream.nullInputStream(), new PrintStream(output));

        ui.showTaskList(List.of());
        ui.showMatchingTasks(List.of());

        assertEquals(String.join(System.lineSeparator(), "Gongrilla find tasks in list:",
                "Here are the matching tasks in your list:", ""), output.toString());
    }

    @Test
    void showAddedTask_printsAllLinesInOrder() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(InputStream.nullInputStream(),
                new PrintStream(output, true, StandardCharsets.UTF_8));

        ui.showAddedTask("todo", new Todo("read book"), 1);

        assertEquals(String.join(System.lineSeparator(),
                "Ooo. New todo:", "  [T][ ] read book", "Gongrilla count 1 tasks.", ""),
                output.toString(StandardCharsets.UTF_8));
    }
}
