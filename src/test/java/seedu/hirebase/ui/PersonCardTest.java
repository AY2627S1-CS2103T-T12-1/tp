package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.testutil.PersonBuilder;

public class PersonCardTest {
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
    public void constructor_remark_isDisplayed() throws Exception {
        runOnFxThread(() -> {
            for (String value : new String[] {"Likes baseball", ""}) {
                PersonCard card = new PersonCard(new PersonBuilder().withRemark(value).build(), 1);
                Label remark = (Label) card.getRoot().lookup("#remark");
                assertEquals(value, remark.getText());
            }
        });
    }

    @Test
    public void constructor_recruitmentDetails_displaysSortedSkillsAndRole() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().withSkills("SQL", "C++", "Java")
                    .withRole("Backend Engineer").build(), 2);
            FlowPane skills = (FlowPane) card.getRoot().lookup("#skills");
            Label role = (Label) card.getRoot().lookup("#role");
            List<String> skillPills = skills.getChildren().stream()
                    .map(node -> ((Label) node).getText())
                    .toList();
            assertEquals(List.of("C++", "Java", "SQL"), skillPills);
            assertTrue(skills.getChildren().stream()
                    .allMatch(node -> node.getStyleClass().contains("skill-pill")));
            assertEquals("Target role: Backend Engineer", role.getText());
            assertTrue(skills.isVisible());
            assertTrue(skills.isManaged());
            assertTrue(role.isVisible());
            assertTrue(role.isManaged());
            assertEquals("2. ", ((Label) card.getRoot().lookup("#id")).getText());
        });
    }

    @Test
    public void constructor_absentRecruitmentDetails_hidesLabelsAndTheirLayoutSpace() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            FlowPane skills = (FlowPane) card.getRoot().lookup("#skills");
            Label role = (Label) card.getRoot().lookup("#role");
            assertTrue(skills.getChildren().isEmpty());
            assertFalse(skills.isVisible());
            assertFalse(skills.isManaged());
            assertFalse(role.isVisible());
            assertFalse(role.isManaged());
        });
    }

    @Test
    public void constructor_defaultInterviewStage_displaysScreeningWithoutRecruitmentDetails() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            Label stage = (Label) card.getRoot().lookup("#interviewStage");
            assertEquals("Screening", stage.getText());
            assertTrue(stage.getStyleClass().contains("stage-badge"));
            assertTrue(stage.getStyleClass().contains("stage-screening"));
            assertTrue(stage.isVisible());
            assertTrue(stage.isManaged());
        });
    }

    @Test
    public void constructor_nonDefaultInterviewStage_displaysCandidateStage() throws Exception {
        runOnFxThread(() -> {
            Map<InterviewStage, String> expectedLabels = Map.of(
                    InterviewStage.ACCEPTED, "Accepted",
                    InterviewStage.REJECTED, "Rejected");
            expectedLabels.forEach((value, expectedText) -> {
                PersonCard card = new PersonCard(new PersonBuilder().withInterviewStage(value)
                        .withSkills("Java").withRole("Backend Engineer").build(), 1);
                Label stage = (Label) card.getRoot().lookup("#interviewStage");
                assertEquals(expectedText, stage.getText());
                assertTrue(stage.getStyleClass().contains("stage-badge"));
                assertTrue(stage.getStyleClass().contains("stage-" + value.name().toLowerCase()));
                assertTrue(stage.isVisible());
                assertTrue(stage.isManaged());
            });
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
