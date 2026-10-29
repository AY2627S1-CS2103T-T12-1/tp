package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.hirebase.logic.LogicManager;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

/**
 * Verifies that candidate details stay readable and agree with the selected record.
 */
public class CandidateDetailsRegressionTest {
    @TempDir
    public Path temporaryFolder;

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
        ready.get(15, TimeUnit.SECONDS);
    }

    @AfterEach
    public void closeDetailsWindows() throws Exception {
        onFxThread(() -> Stage.getWindows().stream().filter(Stage.class::isInstance).map(Stage.class::cast)
                .filter(window -> "Candidate Details".equals(window.getTitle())).toList().forEach(Stage::close));
    }

    @Test
    public void details_longNote_staysWithinScreenAndScrollsToEnd() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                String note = "Experienced developer with production services experience. ".repeat(80);
                submit(primary, "note 1 " + note);
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                layout(details);
                ScrollPane scroll = (ScrollPane) details.getScene().lookup("#detailsScrollPane");
                Label noteLabel = (Label) details.getScene().lookup("#note");
                double initialBottom = noteLabel.localToScene(noteLabel.getBoundsInLocal()).getMaxY();
                assertTrue(details.getHeight() <= Screen.getPrimary().getVisualBounds().getHeight());
                assertTrue(scroll.getContent().getBoundsInLocal().getHeight() > scroll.getViewportBounds().getHeight());
                assertEquals(note.stripTrailing(), noteLabel.getText());

                scroll.setVvalue(scroll.getVmax());
                layout(details);
                double finalBottom = noteLabel.localToScene(noteLabel.getBoundsInLocal()).getMaxY();
                assertTrue(finalBottom < initialBottom);
                assertTrue(finalBottom <= details.getScene().getHeight());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_addLongNoteWhileOpen_keepsWindowWithinScreen() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                submit(primary, "note 1 " + "Experienced developer with production services experience. ".repeat(80));
                layout(details);
                assertTrue(details.isShowing());
                assertTrue(details.getY() >= Screen.getPrimary().getVisualBounds().getMinY());
                assertTrue(details.getY() + details.getHeight() <= Screen.getPrimary().getVisualBounds().getMaxY());
                ScrollPane scroll = (ScrollPane) details.getScene().lookup("#detailsScrollPane");
                assertTrue(scroll.getContent().getBoundsInLocal().getHeight() > scroll.getViewportBounds().getHeight());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_maximumWidthSkill_keepsCompletePillWithinViewport() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                String skill = "W".repeat(30);
                submit(primary, "edit candidate 1 s/" + skill);
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                layout(details);
                ScrollPane scroll = (ScrollPane) details.getScene().lookup("#detailsScrollPane");
                FlowPane skills = (FlowPane) details.getScene().lookup("#skills");
                Label pill = (Label) skills.getChildren().getFirst();
                Bounds pillBounds = pill.localToScene(pill.getBoundsInLocal());
                Bounds viewportBounds = scroll.lookup(".viewport").localToScene(scroll.getViewportBounds());
                assertEquals(skill, pill.getText());
                assertTrue(pill.getWidth() >= pill.prefWidth(-1));
                assertTrue(pillBounds.getMinX() >= viewportBounds.getMinX());
                assertTrue(pillBounds.getMaxX() <= viewportBounds.getMaxX());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_deleteSelectedFirstOfTwo_closesWithoutOpeningRemainingCandidate() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                submit(primary, "delete candidate 1");
                assertEquals(1, candidates(primary).getItems().size());
                assertEquals("Bob", candidates(primary).getItems().getFirst().getName().fullName);
                assertFalse(details.isShowing());
                assertTrue(details.getScene().lookup("#placeholder").isVisible());
                assertTrue(candidates(primary).getSelectionModel().isEmpty());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_deleteOtherCandidate_keepsSelectedProfile() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                submit(primary, "delete candidate 2");
                assertTrue(details.isShowing());
                assertEquals("Alice", ((Label) details.getScene().lookup("#name")).getText());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_clear_closesSelectedProfile() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                submit(primary, "clear");
                assertTrue(candidates(primary).getItems().isEmpty());
                assertFalse(details.isShowing());
                assertTrue(details.getScene().lookup("#placeholder").isVisible());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_filterOutSelectedCandidate_closesAndCanReopenSameProfile() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                submit(primary, "find Bob");
                assertEquals(1, candidates(primary).getItems().size());
                assertFalse(details.isShowing());
                submit(primary, "list candidates");
                candidates(primary).getSelectionModel().select(0);
                assertTrue(details.isShowing());
                assertEquals("Alice", ((Label) details.getScene().lookup("#name")).getText());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_failedStageSave_preservesProfileAndSelection() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                submit(primary, "note 1 Current screening note");
                candidates(primary).getSelectionModel().select(0);
                Person selected = candidates(primary).getSelectionModel().getSelectedItem();
                Stage details = visibleDetailsWindow();
                assertDoesNotThrow(() -> {
                    Path dataPath = temporaryFolder.resolve("data.json");
                    Files.delete(dataPath);
                    Files.createDirectory(dataPath);
                });

                submit(primary, "accept candidate 1");

                assertTrue(details.isShowing());
                assertSame(selected, candidates(primary).getSelectionModel().getSelectedItem());
                assertEquals("Alice", ((Label) details.getScene().lookup("#name")).getText());
                assertEquals("Screening", ((Label) details.getScene().lookup("#stage")).getText());
                assertEquals("Current screening note", ((Label) details.getScene().lookup("#note")).getText());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_editAfterEarlierCandidateDeleted_updatesSelectedPermanentId() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                submit(primary, "delete candidate 1");
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                submit(primary, "edit candidate 2 p/98765432");
                assertTrue(details.isShowing());
                assertEquals("Bob", ((Label) details.getScene().lookup("#name")).getText());
                assertEquals("98765432", ((Label) details.getScene().lookup("#phone")).getText());
                assertEquals(2, candidates(primary).getItems().getFirst()
                        .getCandidateId().orElseThrow().getValue());
            } finally {
                primary.close();
            }
        });
    }

    @Test
    public void details_editSelectedCandidate_refreshesContactStageAndNote() throws Exception {
        onFxThread(() -> {
            MainWindow window = createWindowWithCandidates();
            Stage primary = window.getPrimaryStage();
            try {
                candidates(primary).getSelectionModel().select(0);
                Stage details = visibleDetailsWindow();
                submit(primary, "edit candidate 1 p/98765432 r/Senior Engineer");
                submit(primary, "accept candidate 1");
                submit(primary, "note 1 Strong system design skills");
                assertTrue(details.isShowing());
                assertEquals("Alice", ((Label) details.getScene().lookup("#name")).getText());
                assertEquals("98765432", ((Label) details.getScene().lookup("#phone")).getText());
                assertEquals("Senior Engineer", ((Label) details.getScene().lookup("#role")).getText());
                assertEquals("Accepted", ((Label) details.getScene().lookup("#stage")).getText());
                assertEquals("Strong system design skills", ((Label) details.getScene().lookup("#note")).getText());
            } finally {
                primary.close();
            }
        });
    }

    private MainWindow createWindowWithCandidates() {
        LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonHireBaseStorage(temporaryFolder.resolve("data.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        MainWindow window = new MainWindow(new Stage(), logic, temporaryFolder.resolve("data.json"));
        window.fillInnerParts();
        Stage primary = window.getPrimaryStage();
        submit(primary, "add candidate n/Alice p/91234567 e/alice@example.com a/Singapore s/Java r/Engineer");
        submit(primary, "add candidate n/Bob p/91234568 e/bob@example.com a/Singapore s/SQL r/Engineer");
        primary.show();
        layout(primary);
        return window;
    }

    @SuppressWarnings("unchecked")
    private static ListView<Person> candidates(Stage primary) {
        return (ListView<Person>) primary.getScene().lookup("#personListView");
    }

    private static Stage visibleDetailsWindow() {
        return Stage.getWindows().stream().filter(Stage.class::isInstance).map(Stage.class::cast)
                .filter(window -> "Candidate Details".equals(window.getTitle()) && window.isShowing())
                .findFirst().orElseThrow();
    }

    private static void submit(Stage primary, String command) {
        TextField field = (TextField) primary.getScene().lookup("#commandTextField");
        field.setText(command);
        field.fireEvent(new ActionEvent());
    }

    private static void layout(Stage window) {
        window.getScene().getRoot().applyCss();
        window.getScene().getRoot().layout();
    }

    private static void onFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }
}
