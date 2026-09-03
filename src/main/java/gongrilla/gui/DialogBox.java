package gongrilla.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/** Displays a message together with its speaker's picture. */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;

    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image image) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException("Unable to load a dialog box", exception);
        }

        dialog.setText(text);
        displayPicture.setImage(image);
    }

    /**
     * Creates a dialog box for the user.
     *
     * @param text message to display.
     * @param image user's display picture.
     * @return dialog box with the user positioned on the right.
     */
    public static DialogBox getUserDialog(String text, Image image) {
        return new DialogBox(text, image);
    }

    /**
     * Creates a styled reply from Gongrilla.
     *
     * @param text message to display.
     * @param image Gongrilla's display picture.
     * @param commandType type of command that produced the response.
     * @return dialog box with Gongrilla positioned on the left.
     */
    public static DialogBox getGongrillaDialog(String text, Image image, String commandType) {
        DialogBox dialogBox = new DialogBox(text, image);
        dialogBox.flip();
        dialogBox.applyCommandStyle(commandType);
        return dialogBox;
    }

    /** Places Gongrilla's picture on the left and its response on the right. */
    private void flip() {
        ObservableList<Node> children = FXCollections.observableArrayList(getChildren());
        Collections.reverse(children);
        getChildren().setAll(children);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }

    /** Applies the response color associated with the executed command. */
    private void applyCommandStyle(String commandType) {
        String styleClass = switch (commandType) {
            case "AddCommand" -> "add-label";
            case "MarkCommand" -> "marked-label";
            case "DeleteCommand" -> "delete-label";
            default -> null;
        };

        if (styleClass != null) {
            dialog.getStyleClass().add(styleClass);
        }
    }
}
