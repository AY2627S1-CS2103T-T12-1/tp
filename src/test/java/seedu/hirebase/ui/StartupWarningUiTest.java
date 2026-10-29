package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.hirebase.logic.LogicManager;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class StartupWarningUiTest {
    private static final String WARNING = "Could not read the data file data.json, so HireBase started with no data.";

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
    public void start_withWarning_showsWarningAsErrorUntilNextCommand() throws Exception {
        onFxThread(() -> {
            Stage stage = new Stage();
            new UiManager(logic(), temporaryFolder.resolve("data.json"), Optional.of(WARNING)).start(stage);
            try {
                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                assertEquals(WARNING, result.getText());
                assertNotNull(stage.getScene().lookup(".result-error"));

                TextField command = (TextField) stage.getScene().lookup("#commandTextField");
                command.setText("list candidates");
                command.fireEvent(new ActionEvent());
                assertEquals("No candidates found.", result.getText());
                assertNull(stage.getScene().lookup(".result-error"));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void start_withoutWarning_resultDisplayEmpty() throws Exception {
        onFxThread(() -> {
            Stage stage = new Stage();
            new UiManager(logic(), temporaryFolder.resolve("data.json"), Optional.empty()).start(stage);
            try {
                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                assertEquals("", result.getText());
                assertNull(stage.getScene().lookup(".result-error"));
            } finally {
                stage.close();
            }
        });
    }

    private LogicManager logic() {
        return new LogicManager(new ModelManager(), new StorageManager(
                new JsonHireBaseStorage(temporaryFolder.resolve("data.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    private static void onFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
