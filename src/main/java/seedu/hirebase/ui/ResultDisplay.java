package seedu.hirebase.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final String ERROR_STYLE_CLASS = "result-error";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
    }

    /** Styles the result display to indicate a successful command. */
    public void setStyleToIndicateSuccess() {
        getRoot().getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /** Styles the result display to indicate a failed command. */
    public void setStyleToIndicateError() {
        if (!getRoot().getStyleClass().contains(ERROR_STYLE_CLASS)) {
            getRoot().getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

}
