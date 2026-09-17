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
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

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
    void handleUserInput_bye_showsFarewellThenExitsAfterDelay() throws Exception {
        CountDownLatch exited = new CountDownLatch(1);
        FutureTask<Long> test = new FutureTask<>(() -> {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("/view/MainWindow.fxml"));
            loader.setControllerFactory(type -> new MainWindow(exited::countDown));
            AnchorPane root = loader.load();
            MainWindow controller = loader.getController();
            Gongrilla bot = new Gongrilla(temporaryDirectory.resolve("exit.txt"));
            controller.setGongrilla(bot);
            TextField input = (TextField) root.lookup("#userInput");
            Button send = (Button) root.lookup("#sendButton");
            VBox messages = (VBox) loader.getNamespace().get("dialogContainer");
            input.setText("bye extra");
            input.fireEvent(new ActionEvent());
            assertFalse(input.isDisabled());
            assertFalse(send.isDisabled());
            input.setText("  BYE  ");
            long started = System.nanoTime();
            input.fireEvent(new ActionEvent());
            assertEquals("ExitCommand", bot.getCommandType());
            assertTrue(input.isDisabled());
            assertTrue(send.isDisabled());
            Label reply = (Label) messages.getChildren().getLast().lookup("#dialog");
            assertEquals(bot.getResponse("bye"), reply.getText());
            int messageCount = messages.getChildren().size();
            input.fireEvent(new ActionEvent());
            assertEquals(messageCount, messages.getChildren().size());
            assertEquals(1, exited.getCount());
            return started;
        });
        Platform.runLater(test);
        long started = test.get(15, TimeUnit.SECONDS);
        assertTrue(exited.await(10, TimeUnit.SECONDS));
        assertTrue(System.nanoTime() - started >= TimeUnit.MILLISECONDS.toNanos(1900));
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
