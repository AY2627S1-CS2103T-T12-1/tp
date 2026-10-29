package seedu.hirebase.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.hirebase.model.candidate.Person;

/**
 * A popup window that displays the full profile of the selected {@code Person}.
 * Shows a placeholder message when no candidate is selected.
 */
public class PersonDetailWindow extends UiPart<Stage> {

    private static final String FXML = "PersonDetailWindow.fxml";
    private static final String EMPTY_PLACEHOLDER = "—";

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
        if (getRoot().isShowing()) {
            getRoot().requestFocus();
        } else {
            getRoot().show();
            getRoot().centerOnScreen();
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
