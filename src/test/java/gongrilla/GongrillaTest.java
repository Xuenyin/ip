package gongrilla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GongrillaTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void getResponse_addTodo_returnsConfirmationAndTracksCommandType() {
        Gongrilla gongrilla = new Gongrilla(temporaryDirectory.resolve("gongrilla.txt"));

        String response = gongrilla.getResponse("todo read book");

        assertTrue(response.contains("read book"));
        assertEquals("AddCommand", gongrilla.getCommandType());
    }

    @Test
    void getResponse_invalidCommand_returnsErrorAndTracksError() {
        Gongrilla gongrilla = new Gongrilla(temporaryDirectory.resolve("gongrilla.txt"));

        String response = gongrilla.getResponse("unknown command");

        assertTrue(response.contains("not know"));
        assertEquals("Error", gongrilla.getCommandType());
    }
}
