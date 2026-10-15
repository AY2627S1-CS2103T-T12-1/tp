package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;

public class ResultDisplayTest {

    private static final String ERROR_STYLE_CLASS = "result-error";

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
    public void setStyle_errorThenSuccess_togglesErrorStyleClass() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();

            // Default: no error style.
            assertFalse(resultDisplay.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));

            resultDisplay.setStyleToIndicateError();
            assertTrue(resultDisplay.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));

            // Calling again must not add a duplicate.
            resultDisplay.setStyleToIndicateError();
            assertEquals(1, resultDisplay.getRoot().getStyleClass().stream()
                    .filter(ERROR_STYLE_CLASS::equals).count());

            resultDisplay.setStyleToIndicateSuccess();
            assertFalse(resultDisplay.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));
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
