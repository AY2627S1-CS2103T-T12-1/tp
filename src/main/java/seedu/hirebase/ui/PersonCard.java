package seedu.hirebase.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.hirebase.model.candidate.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved
     * keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The
     *      issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label candidateId;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label note;
    @FXML
    private FlowPane tags;
    @FXML
    private FlowPane skills;
    @FXML
    private Label role;
    @FXML
    private Label interviewStage;
    @FXML
    private Label priorityScore;

    /**
     * Creates a {@code PersonCard} with the given {@code Person}.
     */
    public PersonCard(Person person) {
        super(FXML);
        this.person = person;
        name.setText(person.getName().fullName);
        candidateId.setText(person.getCandidateId().map(id -> "#" + id + " ").orElse(""));
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        priorityScore.setText("Priority: " + person.getPriorityScore());
        note.setText(person.getNote().value);
        note.setVisible(!person.getNote().value.isEmpty());
        note.setManaged(note.isVisible());
        person.getSkills().stream()
                .map(skill -> skill.value)
                .sorted()
                .forEach(skill -> {
                    Label pill = new Label(skill);
                    pill.getStyleClass().add("skill-pill");
                    skills.getChildren().add(pill);
                });
        skills.setVisible(!person.getSkills().isEmpty());
        skills.setManaged(skills.isVisible());
        role.setText("Target role: " + person.getRole().map(value -> value.value).orElse(""));
        role.setVisible(person.getRole().isPresent());
        role.setManaged(role.isVisible());
        interviewStage.setText(switch (person.getInterviewStage()) {
            case SCREENING -> "Screening";
            case ACCEPTED -> "Accepted";
            case REJECTED -> "Rejected";
        });
        interviewStage.getStyleClass().add("stage-" + person.getInterviewStage().name().toLowerCase());
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        tags.setVisible(!person.getTags().isEmpty());
        tags.setManaged(tags.isVisible());
    }
}
