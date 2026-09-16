package gongrilla.gui;

import gongrilla.Gongrilla;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/** Controls Gongrilla's main window. */
public class MainWindow {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    private final Image userImage = new Image(
            MainWindow.class.getResourceAsStream("/images/Bananini.png"));
    private final Image gongrillaImage = new Image(
            MainWindow.class.getResourceAsStream("/images/Gongrillini.png"));
    private Gongrilla gongrilla;

    /** Initializes scrolling after FXMLLoader injects the controls. */
    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);
    }

    /**
     * Supplies the backend that processes commands.
     *
     * @param gongrilla backend shared by this window's conversations.
     */
    public void setGongrilla(Gongrilla gongrilla) {
        this.gongrilla = gongrilla;
        String welcome =
                  "  _--==--_  \n"
                + " / _    _ \\ \n"
                + " \\        / \n"
                + " |  (..)  |  \n"
                + " \\   __   / \n"
                + "  \\______/  \n"
                + "sup. Type help for commands.";
        dialogContainer.getChildren().add(DialogBox.getGongrillaDialog(welcome, gongrillaImage, "Welcome"));
    }

    /** Displays the user's command and Gongrilla's response. */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = gongrilla.getResponse(input);
        String commandType = gongrilla.getCommandType();

        DialogBox reply = DialogBox.getGongrillaDialog(response, gongrillaImage, commandType);
        dialogContainer.getChildren().addAll(DialogBox.getUserDialog(input, userImage), reply);
        userInput.clear();
        Platform.runLater(() -> {
            scrollPane.applyCss();
            scrollPane.layout();
            double scrollableHeight = dialogContainer.getHeight() - scrollPane.getViewportBounds().getHeight();
            double position = "HelpCommand".equals(commandType) && scrollableHeight > 0
                    ? Math.min(1, reply.getBoundsInParent().getMinY() / scrollableHeight) : 1;
            scrollPane.setVvalue(position);
        });
    }
}
