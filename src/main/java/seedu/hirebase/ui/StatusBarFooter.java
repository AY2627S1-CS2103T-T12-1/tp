package seedu.hirebase.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.job.JobOpening;

/**
 * A UI for the status bar that is displayed at the footer of the application.
 */
public class StatusBarFooter extends UiPart<Region> {

    private static final String FXML = "StatusBarFooter.fxml";

    @FXML
    private Label saveLocationStatus;
    @FXML
    private Label recordCountStatus;

    /**
     * Creates a {@code StatusBarFooter} showing the save location and a live count
     * of the records in the currently displayed list.
     *
     * @param saveLocation path of the data file.
     * @param persons the candidate list, counted when the candidates tab is shown.
     * @param jobs the job opening list, counted when the jobs tab is shown.
     * @param showingJobs {@code true} when the jobs tab is active, {@code false} for candidates.
     */
    public StatusBarFooter(Path saveLocation, ObservableList<Person> persons,
            ObservableList<JobOpening> jobs, ObservableValue<Boolean> showingJobs) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        recordCountStatus.textProperty().bind(Bindings.createStringBinding(() ->
                Boolean.TRUE.equals(showingJobs.getValue())
                        ? jobs.size() + " job opening" + (jobs.size() == 1 ? "" : "s")
                        : persons.size() + " candidate" + (persons.size() == 1 ? "" : "s"),
                showingJobs, persons, jobs));
    }

}
