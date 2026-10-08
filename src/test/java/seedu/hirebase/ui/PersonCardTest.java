package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.hirebase.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void setUpJavaFx() throws Exception {
        // Linux CI has no display server; the remaining tests do not need a graphical desktop.
        assumeTrue(!System.getProperty("os.name").startsWith("Linux")
                || System.getenv("DISPLAY") != null, "A display server is required for JavaFX controls");
        CompletableFuture<Void> ready = new CompletableFuture<>();
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.complete(null);
        });
        ready.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_remark_isDisplayed() throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                for (String value : new String[] {"Likes baseball", ""}) {
                    PersonCard card = new PersonCard(new PersonBuilder().withRemark(value).build(), 1);
                    Label remark = (Label) card.getRoot().lookup("#remark");
                    assertNotNull(remark);
                    assertEquals(value, remark.getText());
                }
                result.complete(null);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        result.get(10, TimeUnit.SECONDS);
    }
}
