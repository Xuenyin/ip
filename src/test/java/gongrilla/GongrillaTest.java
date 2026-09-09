package gongrilla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests command responses, persistence, and GUI response metadata together. */
class GongrillaTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void getResponse_clash_separatesBubblesAndExecutesOnlyOnce() throws Exception {
        Path data = temporaryDirectory.resolve("bubbles.txt");
        Gongrilla gongrilla = new Gongrilla(data);
        String command = "event workshop /from 9/9/2026 1000 /to 9/9/2026 1100";
        gongrilla.getResponse(command);

        String combined = gongrilla.getResponse(command);
        String task = "[E][ ] workshop (from: 9 Sep 2026, 10:00AM to: 9 Sep 2026, 11:00AM)";
        String confirmation = String.join(System.lineSeparator(),
                "Ooo. New event:", "  " + task, "Gongrilla count 2 tasks.");
        String warning = String.join(System.lineSeparator(),
                "Ooo. Schedule clash! Task added anyway.", "Clashes with:", "  1." + task);

        assertEquals(confirmation, gongrilla.getMainResponse());
        assertEquals(warning, gongrilla.getScheduleWarning());
        assertEquals(confirmation + System.lineSeparator().repeat(2) + warning, combined);
        assertEquals(2, Files.readAllLines(data).size());
    }

    @Test
    void getResponse_clashThenOtherCommands_resetsWarningAndPreservesCommandIdentity() {
        Gongrilla gongrilla = new Gongrilla(temporaryDirectory.resolve("clashes.txt"));
        String command = "event workshop /from 9/9/2026 1000 /to 9/9/2026 1100";
        gongrilla.getResponse(command);
        assertFalse(gongrilla.hasScheduleWarning());
        for (String next : List.of("list", "unknown", "todo read", "mark 1", "unmark 1")) {
            gongrilla.getResponse(command);
            assertTrue(gongrilla.hasScheduleWarning());
            assertEquals("AddCommand", gongrilla.getCommandType());

            String response = gongrilla.getResponse(next);

            assertFalse(gongrilla.hasScheduleWarning());
            assertFalse(response.contains("Schedule clash"));
            assertEquals("", gongrilla.getScheduleWarning());
            assertEquals(response, gongrilla.getMainResponse());
        }
        assertEquals("Hmm. Gongrilla no know that :-(", gongrilla.getResponse("anomalies"));
    }

    @Test
    void getResponse_legacyDataAndRestart_preservesDatesAndDetectsExistingClashes() throws Exception {
        Path data = temporaryDirectory.resolve("legacy.txt");
        Files.write(data, List.of("E | 0 | day | 2026-09-09 | 2026-09-10",
                "E | 0 | instant | 2026-09-09 | 2026-09-09"));
        Gongrilla gongrilla = new Gongrilla(data);
        String command = "event morning /from 9/9/2026 1000 /to 9/9/2026 1100";
        assertTrue(gongrilla.getResponse(command).contains("  1.[E][ ] day"));

        Gongrilla restored = new Gongrilla(data);
        assertFalse(restored.hasScheduleWarning());
        String response = restored.getResponse(command);
        assertTrue(response.contains("  1.[E][ ] day"));
        assertTrue(response.contains("  3.[E][ ] morning"));
        assertFalse(response.contains("instant"));
        assertTrue(restored.hasScheduleWarning());
    }

    @Test
    void getResponse_saveFailureAfterWarning_clearsWarningAndShowsOnlySaveError() throws Exception {
        Path parent = temporaryDirectory.resolve("journal");
        Path data = parent.resolve("tasks.txt");
        Gongrilla gongrilla = new Gongrilla(data);
        String command = "event workshop /from 9/9/2026 1000 /to 9/9/2026 1100";
        gongrilla.getResponse(command);
        gongrilla.getResponse(command);
        assertTrue(gongrilla.hasScheduleWarning());
        Files.delete(data);
        Files.delete(parent);
        Files.writeString(parent, "blocks directory creation");

        String response = gongrilla.getResponse(command);

        assertTrue(response.startsWith("Gongrilla cannot save that change: "));
        assertTrue(response.endsWith("Task list was not changed."));
        assertFalse(response.contains("Schedule clash"));
        assertFalse(response.contains("Ooo. New"));
        assertFalse(gongrilla.hasScheduleWarning());
        assertEquals("Error", gongrilla.getCommandType());
    }

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

        assertTrue(response.contains("no know"));
        assertEquals("Error", gongrilla.getCommandType());
    }
}
