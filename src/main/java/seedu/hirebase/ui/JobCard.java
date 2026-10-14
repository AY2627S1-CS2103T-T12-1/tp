package seedu.hirebase.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.hirebase.model.job.JobOpening;

/**
 * Displays an opening's current index and full details.
 */
public class JobCard extends UiPart<Region> {
    private static final String FXML = "JobListCard.fxml";

    @FXML
    private Label title;
    @FXML
    private Label department;
    @FXML
    private Label description;

    /**
     * Creates the job display from the supplied model data.
     */
    public JobCard(JobOpening job, int index) {
        super(FXML);
        title.setText(index + ". " + job.getTitle());
        department.setText("Department: " + job.getDepartment());
        description.setText("Description: " + job.getDescription());
    }
}
