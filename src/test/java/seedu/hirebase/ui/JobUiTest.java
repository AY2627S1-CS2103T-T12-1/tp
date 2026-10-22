package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.hirebase.logic.LogicManager;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class JobUiTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(started::countDown);
        }
        assertTrue(started.await(15, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    @Test
    public void card_longDetails_wrapAndRemainComplete() throws Exception {
        onFxThread(() -> {
            JobOpening job = job("Long description ".repeat(100));
            JobCard card = new JobCard(job.withJobId(new JobId(3)));
            Scene scene = new Scene(card.getRoot(), 350, 800);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            Label title = (Label) card.getRoot().lookup("#title");
            Label description = (Label) card.getRoot().lookup("#description");
            assertEquals("3. Developer", title.getText());
            Label department = (Label) card.getRoot().lookup("#department");
            assertEquals("Engineering", department.getText());
            assertTrue(department.getStyleClass().contains("dept-pill"));
            assertEquals(job.getDescription().value, description.getText());
            assertTrue(description.isWrapText());
            assertTrue(description.getHeight() > title.getHeight());
        });
    }

    @Test
    public void card_requiredSkills_areSortedAndWrap() throws Exception {
        onFxThread(() -> {
            JobOpening base = job("Build services");
            JobOpening tagged = new JobOpening(base.getTitle(), base.getDepartment(), base.getDescription(),
                    new JobId(7), Set.of(new Skill("Python"), new Skill("Java")));
            JobCard card = new JobCard(tagged);
            Label skills = (Label) card.getRoot().lookup("#skills");
            assertEquals("Required skills: Java, Python", skills.getText());
            assertTrue(skills.isWrapText());
            JobCard empty = new JobCard(base.withJobId(new JobId(8)));
            assertEquals("Required skills: None", ((Label) empty.getRoot().lookup("#skills")).getText());
        });
    }

    @Test
    public void cell_reuseAndReindex_preservesIdAndClearsCards() throws Exception {
        onFxThread(() -> {
            var jobs = FXCollections.observableArrayList(job("Build services").withJobId(new JobId(7)));
            JobListPanel panel = new JobListPanel(jobs);
            JobListPanel.JobListCell cell = panel.new JobListCell();
            cell.updateIndex(0);
            cell.updateItem(jobs.getFirst(), false);
            assertNotNull(cell.getGraphic());
            assertEquals("7. Developer", ((Label) cell.getGraphic().lookup("#title")).getText());
            cell.updateIndex(2);
            cell.updateItem(jobs.getFirst(), false);
            assertEquals("7. Developer", ((Label) cell.getGraphic().lookup("#title")).getText());
            cell.updateItem(null, false);
            assertEquals(null, cell.getGraphic());
            cell.updateIndex(-1);
            cell.updateItem(jobs.getFirst(), false);
            assertEquals(null, cell.getGraphic());
            cell.updateItem(null, true);
            assertEquals(null, cell.getGraphic());
        });
    }

    @Test
    public void mainWindow_commandsSwitchTabsAndFailuresKeepSelection() throws Exception {
        onFxThread(() -> {
            ModelManager model = new ModelManager();
            LogicManager logic = new LogicManager(model, new StorageManager(
                    new JsonHireBaseStorage(temporaryFolder.resolve("data.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, temporaryFolder.resolve("data.json"));
            window.fillInnerParts();
            TabPane tabs = (TabPane) stage.getScene().lookup("#recordTabs");
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            assertEquals("Candidates", tabs.getSelectionModel().getSelectedItem().getText());
            submit(command, "add job t/Developer d/Engineering desc/Build services");
            assertEquals("Job openings", tabs.getSelectionModel().getSelectedItem().getText());
            assertEquals(1, logic.getJobOpeningList().size());
            submit(command, "list candidates");
            assertEquals("Candidates", tabs.getSelectionModel().getSelectedItem().getText());
            submit(command, "delete job 99");
            assertEquals("Candidates", tabs.getSelectionModel().getSelectedItem().getText());
            submit(command, "list jobs");
            assertEquals("Job openings", tabs.getSelectionModel().getSelectedItem().getText());
            submit(command, "delete job 1");
            assertTrue(logic.getJobOpeningList().isEmpty());
            submit(command, "add candidate n/Alice p/91234567 e/alice@example.com a/Singapore s/Java r/Engineer");
            assertEquals("Candidates", tabs.getSelectionModel().getSelectedItem().getText());
            for (String input : new String[] {"find Alice", "edit candidate 1 p/91234567",
                "note 1 Interview scheduled", "delete candidate 1"}) {
                tabs.getSelectionModel().select(1);
                submit(command, input);
                assertEquals("Candidates", tabs.getSelectionModel().getSelectedItem().getText());
            }
            tabs.getSelectionModel().select(1);
            submit(command, "clear");
            assertEquals("Job openings", tabs.getSelectionModel().getSelectedItem().getText());
            assertFalse(stage.isShowing());
            stage.close();
        });
    }

    private static void submit(TextField field, String input) {
        field.setText(input);
        field.fireEvent(new ActionEvent());
    }

    private static JobOpening job(String description) {
        return new JobOpening(new JobTitle("Developer"), new Department("Engineering"), new Description(description));
    }

    private static void onFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
