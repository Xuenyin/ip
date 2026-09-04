package gongrilla.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import gongrilla.task.Todo;

/** Tests that multiline responses retain their text and line separators. */
class UiTest {
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
