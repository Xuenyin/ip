package gongrilla.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import gongrilla.Gongrilla;
import javafx.application.Platform;
import javafx.scene.control.Label;

/** Verifies that failed commands are highlighted without affecting later successful replies. */
class DialogBoxTest {
    @TempDir
    private Path temporaryDirectory;

    @BeforeAll
    static void startToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(started::countDown);
        assertTrue(started.await(10, TimeUnit.SECONDS));
    }

    @Test
    void getGongrillaDialog_invalidThenValidCommand_highlightsOnlyError() throws Exception {
        FutureTask<Void> test = new FutureTask<>(() -> {
            Gongrilla gongrilla = new Gongrilla(temporaryDirectory.resolve("tasks.txt"));
            String error = gongrilla.getResponse("unknown command");
            DialogBox errorBox = DialogBox.getGongrillaDialog(error, null, gongrilla.getCommandType());
            Label errorLabel = (Label) errorBox.lookup("#dialog");
            assertTrue(errorLabel.getStyleClass().contains("error-label"));
            assertEquals("ERROR\n" + error, errorLabel.getText());

            String success = gongrilla.getResponse("todo read book");
            DialogBox successBox = DialogBox.getGongrillaDialog(success, null, gongrilla.getCommandType());
            Label successLabel = (Label) successBox.lookup("#dialog");
            assertFalse(successLabel.getStyleClass().contains("error-label"));
            assertTrue(successLabel.getStyleClass().contains("add-label"));
            assertEquals(success, successLabel.getText());
            return null;
        });
        Platform.runLater(test);
        test.get(15, TimeUnit.SECONDS);
    }
}
