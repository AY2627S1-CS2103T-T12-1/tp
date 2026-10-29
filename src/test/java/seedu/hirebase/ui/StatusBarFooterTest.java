package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import seedu.hirebase.logic.commands.CommandResult.ListView;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.testutil.PersonBuilder;

public class StatusBarFooterTest {

    private static final Path DUMMY_PATH = Paths.get("data", "hirebase.json");

    @BeforeAll
    public static void startToolkit() throws Exception {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.complete(null);
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(initialize);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void recordCount_candidatesTab_showsCandidateCountAndPluralises() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList(
                    new PersonBuilder().withName("Alice").build());
            ObservableList<JobOpening> jobs = FXCollections.observableArrayList();
            SimpleBooleanProperty showingJobs = new SimpleBooleanProperty(false);

            StatusBarFooter footer = new StatusBarFooter(DUMMY_PATH, persons, jobs, showingJobs);
            Label count = (Label) footer.getRoot().lookup("#recordCountStatus");

            assertEquals("1 candidate", count.getText());

            persons.add(new PersonBuilder().withName("Bob").build());
            assertEquals("2 candidates", count.getText());
        });
    }

    @Test
    public void recordCount_jobsTab_showsJobCountAndReactsToTabSwitch() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList(
                    new PersonBuilder().withName("Alice").build());
            ObservableList<JobOpening> jobs = FXCollections.observableArrayList(
                    new JobOpening(new JobTitle("Developer"), new Department("Engineering"),
                            new Description("Build things.")));
            SimpleBooleanProperty showingJobs = new SimpleBooleanProperty(true);

            StatusBarFooter footer = new StatusBarFooter(DUMMY_PATH, persons, jobs, showingJobs);
            Label count = (Label) footer.getRoot().lookup("#recordCountStatus");

            assertEquals("1 job opening", count.getText());

            jobs.add(new JobOpening(new JobTitle("Designer"), new Department("Design"),
                    new Description("Design things.")));
            assertEquals("2 job openings", count.getText());

            showingJobs.set(false);
            assertEquals("1 candidate", count.getText());
        });
    }

    @Test
    public void recordCount_calendarTab_tracksVisibleEventsAndOtherTabs() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList(new PersonBuilder().build());
            ObservableList<JobOpening> jobs = FXCollections.observableArrayList();
            SimpleObjectProperty<ListView> selectedView = new SimpleObjectProperty<>(ListView.CALENDAR);
            SimpleIntegerProperty visibleEvents = new SimpleIntegerProperty(2);
            StatusBarFooter footer = new StatusBarFooter(DUMMY_PATH, persons, jobs, selectedView, visibleEvents);
            Label count = (Label) footer.getRoot().lookup("#recordCountStatus");
            assertEquals("2 events", count.getText());
            visibleEvents.set(1);
            assertEquals("1 event", count.getText());
            persons.add(new PersonBuilder().withName("Bob").build());
            assertEquals("1 event", count.getText());
            visibleEvents.set(0);
            assertEquals("0 events", count.getText());
            selectedView.set(ListView.CANDIDATES);
            assertEquals("2 candidates", count.getText());
            selectedView.set(ListView.JOBS);
            assertEquals("0 job openings", count.getText());
        });
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                assertions.run();
                finished.complete(null);
            } catch (Throwable failure) {
                finished.completeExceptionally(failure);
            }
        });
        finished.get(10, TimeUnit.SECONDS);
    }
}
