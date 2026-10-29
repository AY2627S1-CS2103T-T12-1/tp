package seedu.hirebase.ui;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.hirebase.model.candidate.Person;

/**
 * A popup window that displays the full profile of the selected {@code Person}.
 * Shows a placeholder message when no candidate is selected.
 */
public class PersonDetailWindow extends UiPart<Stage> {

    private static final String FXML = "PersonDetailWindow.fxml";
    private static final String EMPTY_PLACEHOLDER = "—";
    // Leave room for the native title bar and borders when sizing the scrollable content.
    private static final double WINDOW_DECORATION_ALLOWANCE = 80;

    @FXML
    private ScrollPane detailsScrollPane;
    @FXML
    private StackPane detailsPane;
    @FXML
    private Label placeholder;
    @FXML
    private VBox content;
    @FXML
    private Label name;
    @FXML
    private Label stage;
    @FXML
    private Label priorityScore;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private Label role;
    @FXML
    private FlowPane skills;
    @FXML
    private Label note;

    /**
     * Creates a new {@code PersonDetailWindow} using the given {@code Stage} as its root.
     */
    public PersonDetailWindow(Stage root) {
        super(FXML, root);
        setPerson(null);
    }

    /**
     * Creates a new {@code PersonDetailWindow}.
     */
    public PersonDetailWindow() {
        this(new Stage());
    }

    /**
     * Displays the given {@code person}'s full profile, or the placeholder when
     * {@code person} is {@code null}.
     */
    public void setPerson(Person person) {
        boolean hasPerson = person != null;
        placeholder.setVisible(!hasPerson);
        placeholder.setManaged(!hasPerson);
        content.setVisible(hasPerson);
        content.setManaged(hasPerson);
        if (!hasPerson) {
            return;
        }

        name.setText(person.getName().fullName);
        setStageBadge(person);
        priorityScore.setText("Priority: " + person.getPriorityScore());
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        address.setText(person.getAddress().value);
        role.setText(person.getRole().map(value -> value.value).orElse(EMPTY_PLACEHOLDER));

        skills.getChildren().clear();
        person.getSkills().stream()
                .map(skill -> skill.value)
                .sorted()
                .forEach(skill -> {
                    Label pill = new Label(skill);
                    pill.getStyleClass().add("skill-pill");
                    skills.getChildren().add(pill);
                });

        String noteValue = person.getNote().value;
        note.setText(noteValue.isEmpty() ? EMPTY_PLACEHOLDER : noteValue);
        if (isShowing()) {
            updateWindowSize();
        }
    }

    private void setStageBadge(Person person) {
        stage.setText(switch (person.getInterviewStage()) {
            case SCREENING -> "Screening";
            case ACCEPTED -> "Accepted";
            case REJECTED -> "Rejected";
        });
        stage.getStyleClass().removeAll("stage-screening", "stage-accepted", "stage-rejected");
        stage.getStyleClass().add("stage-" + person.getInterviewStage().name().toLowerCase());
    }

    /**
     * Shows the given {@code person} in the window, opening or focusing it.
     */
    public void showPerson(Person person) {
        setPerson(person);
        detailsScrollPane.setVvalue(0);
        if (getRoot().isShowing()) {
            getRoot().requestFocus();
        } else {
            updateWindowSize();
            getRoot().show();
            getRoot().centerOnScreen();
        }
    }

    private void updateWindowSize() {
        Stage window = getRoot();
        Screen screen = Screen.getPrimary();
        if (window.isShowing()) {
            screen = Screen.getScreensForRectangle(window.getX(), window.getY(),
                    window.getWidth(), window.getHeight()).stream().findFirst().orElse(screen);
        }
        Rectangle2D screenBounds = screen.getVisualBounds();
        window.getScene().getRoot().applyCss();
        double availableWidth = Math.max(1, screenBounds.getWidth() - WINDOW_DECORATION_ALLOWANCE);
        double viewportWidth = Math.min(availableWidth,
                Math.max(detailsPane.getPrefWidth(), detailsPane.minWidth(-1)));
        detailsScrollPane.setPrefViewportWidth(viewportWidth);
        double availableHeight = Math.max(1, screenBounds.getHeight() - WINDOW_DECORATION_ALLOWANCE);
        detailsScrollPane.setPrefViewportHeight(Math.min(availableHeight,
                detailsPane.prefHeight(viewportWidth)));
        window.setMaxWidth(screenBounds.getWidth());
        window.setMaxHeight(screenBounds.getHeight());
        window.sizeToScene();
        if (window.isShowing()) {
            window.setX(Math.max(screenBounds.getMinX(),
                    Math.min(window.getX(), screenBounds.getMaxX() - window.getWidth())));
            window.setY(Math.max(screenBounds.getMinY(),
                    Math.min(window.getY(), screenBounds.getMaxY() - window.getHeight())));
        }
    }

    /**
     * Returns true if the window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the window.
     */
    public void hide() {
        getRoot().hide();
    }
}
