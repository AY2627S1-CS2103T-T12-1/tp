package seedu.hirebase.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.hirebase.model.job.JobOpening;

/**
 * Displays an opening's permanent ID and full details.
 */
public class JobCard extends UiPart<Region> {
    private static final String FXML = "JobListCard.fxml";

    @FXML
    private Label title;
    @FXML
    private Label department;
    @FXML
    private Label description;
    @FXML
    private Label skills;

    /**
     * Creates the job display from the supplied model data.
     */
    public JobCard(JobOpening job) {
        super(FXML);
        title.setText(job.getJobId().orElseThrow() + ". " + job.getTitle());
        department.setText(job.getDepartment().value);
        description.setText(job.getDescription().value);
        skills.setText("Required skills: " + job.getSkillsText());
    }
}
