package seedu.hirebase.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.hirebase.logic.commands.CommandResult.ListView;
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
        this(saveLocation, persons, jobs, Bindings.createObjectBinding(() ->
                Boolean.TRUE.equals(showingJobs.getValue()) ? ListView.JOBS : ListView.CANDIDATES,
                showingJobs), new SimpleIntegerProperty());
    }

    /**
     * Creates a footer counting the selected candidate, job or calendar view.
     * Calendar counts reflect only events visible under the current view options.
     */
    public StatusBarFooter(Path saveLocation, ObservableList<Person> persons, ObservableList<JobOpening> jobs,
            ObservableValue<ListView> selectedView, ObservableIntegerValue calendarEventCount) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        recordCountStatus.textProperty().bind(Bindings.createStringBinding(() -> {
            if (selectedView.getValue() == ListView.JOBS) {
                return formatCount(jobs.size(), "job opening");
            }
            if (selectedView.getValue() == ListView.CALENDAR) {
                return formatCount(calendarEventCount.get(), "event");
            }
            return formatCount(persons.size(), "candidate");
        }, selectedView, persons, jobs, calendarEventCount));
    }

    private static String formatCount(int count, String recordType) {
        return count + " " + recordType + (count == 1 ? "" : "s");
    }

}
