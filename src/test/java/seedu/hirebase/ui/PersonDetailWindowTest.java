package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.testutil.PersonBuilder;

public class PersonDetailWindowTest {

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
    public void setPerson_null_showsPlaceholderAndHidesContent() throws Exception {
        runOnFxThread(() -> {
            PersonDetailWindow window = new PersonDetailWindow(new Stage());
            window.setPerson(null);
            Node placeholder = lookup(window, "#placeholder");
            Node content = lookup(window, "#content");
            assertTrue(placeholder.isVisible());
            assertFalse(content.isVisible());
            assertFalse(content.isManaged());
        });
    }

    @Test
    public void setPerson_candidate_showsFullProfile() throws Exception {
        runOnFxThread(() -> {
            PersonDetailWindow window = new PersonDetailWindow(new Stage());
            window.setPerson(new PersonBuilder().withName("Jolene Tan").withPhone("91234567")
                    .withEmail("jolene@example.com").withAddress("Blk 30")
                    .withRemark("Strong referral").withSkills("SQL", "Java")
                    .withRole("Backend Engineer").withInterviewStage(InterviewStage.ACCEPTED).build());

            assertTrue(lookup(window, "#content").isVisible());
            assertFalse(lookup(window, "#placeholder").isVisible());

            assertEquals("Jolene Tan", text(window, "#name"));
            assertEquals("Accepted", text(window, "#stage"));
            assertEquals("91234567", text(window, "#phone"));
            assertEquals("jolene@example.com", text(window, "#email"));
            assertEquals("Blk 30", text(window, "#address"));
            assertEquals("Backend Engineer", text(window, "#role"));
            assertEquals("Strong referral", text(window, "#remark"));

            FlowPane skills = (FlowPane) lookup(window, "#skills");
            assertEquals(2, skills.getChildren().size());
            assertEquals("Java", ((Label) skills.getChildren().get(0)).getText());

            assertTrue(lookup(window, "#stage").getStyleClass().contains("stage-accepted"));
        });
    }

    @Test
    public void setPerson_missingOptionalFields_showsEmPlaceholders() throws Exception {
        runOnFxThread(() -> {
            PersonDetailWindow window = new PersonDetailWindow(new Stage());
            window.setPerson(new PersonBuilder().withRemark("").build());
            assertEquals("—", text(window, "#role"));
            assertEquals("—", text(window, "#remark"));
        });
    }

    @Test
    public void setPerson_eachStage_setsMatchingBadge() throws Exception {
        runOnFxThread(() -> {
            PersonDetailWindow window = new PersonDetailWindow(new Stage());

            window.setPerson(new PersonBuilder().withInterviewStage(InterviewStage.SCREENING).build());
            assertEquals("Screening", text(window, "#stage"));
            assertTrue(lookup(window, "#stage").getStyleClass().contains("stage-screening"));

            // Re-selecting a different stage must swap the style class, not stack it.
            window.setPerson(new PersonBuilder().withInterviewStage(InterviewStage.REJECTED).build());
            assertEquals("Rejected", text(window, "#stage"));
            assertTrue(lookup(window, "#stage").getStyleClass().contains("stage-rejected"));
            assertFalse(lookup(window, "#stage").getStyleClass().contains("stage-screening"));
        });
    }

    @Test
    public void noArgConstructor_createsHiddenWindowWithPlaceholder() throws Exception {
        runOnFxThread(() -> {
            PersonDetailWindow window = new PersonDetailWindow();
            assertFalse(window.isShowing());
            assertTrue(lookup(window, "#placeholder").isVisible());
            // Hiding an already-hidden window is a safe no-op.
            window.hide();
            assertFalse(window.isShowing());
        });
    }

    private static Node lookup(PersonDetailWindow window, String id) {
        return window.getRoot().getScene().lookup(id);
    }

    private static String text(PersonDetailWindow window, String id) {
        return ((Label) lookup(window, id)).getText();
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
