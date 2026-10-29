package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
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
    public void constructor_priorityScores_displaysScoreAndLabel() throws Exception {
        runOnFxThread(() -> {
            String[] expectedLabels = {"1 (Low)", "2 (Medium)", "3 (High)"};
            for (int i = 0; i < expectedLabels.length; i++) {
                PersonCard card = new PersonCard(new PersonBuilder().withPriorityScore(String.valueOf(i + 1)).build());
                Label priorityScore = (Label) card.getRoot().lookup("#priorityScore");
                assertEquals("Priority: " + expectedLabels[i], priorityScore.getText());
                assertTrue(priorityScore.isVisible());
                assertTrue(priorityScore.isManaged());
                assertTrue(priorityScore.getStyleClass().contains("cell_small_label"));
            }
            PersonCard defaultCard = new PersonCard(new PersonBuilder().build());
            Label defaultPriorityScore = (Label) defaultCard.getRoot().lookup("#priorityScore");
            assertEquals("Priority: 1 (Low)", defaultPriorityScore.getText());
        });
    }

    @Test
    public void constructor_note_isDisplayed() throws Exception {
        runOnFxThread(() -> {
            for (String value : new String[] {"Likes baseball", ""}) {
                PersonCard card = new PersonCard(new PersonBuilder().withNote(value).build());
                Label note = (Label) card.getRoot().lookup("#note");
                assertEquals(value, note.getText());
            }
        });
    }

    @Test
    public void constructor_recruitmentDetails_displaysSortedSkillsAndRole() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().withSkills("SQL", "C++", "Java")
                    .withRole("Backend Engineer").build());
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
        });
    }

    @Test
    public void constructor_candidateId_isDisplayedInsteadOfListPosition() throws Exception {
        runOnFxThread(() -> {
            Person candidate = new PersonBuilder().build().withCandidateId(new CandidateId(7));
            PersonCard card = new PersonCard(candidate);
            assertEquals("#7 ", ((Label) card.getRoot().lookup("#candidateId")).getText());
            assertNull(card.getRoot().lookup("#id"));

            PersonCard withoutId = new PersonCard(new PersonBuilder().build());
            assertEquals("", ((Label) withoutId.getRoot().lookup("#candidateId")).getText());
        });
    }

    @Test
    public void constructor_absentRecruitmentDetails_hidesLabelsAndTheirLayoutSpace() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build());
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
            PersonCard card = new PersonCard(new PersonBuilder().build());
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
                        .withSkills("Java").withRole("Backend Engineer").build());
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
