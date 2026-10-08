package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.hirebase.testutil.PersonBuilder;

public class PersonCardTest {
    @BeforeAll
    public static void startToolkit() throws Exception {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.complete(null);
        });
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
            Label skills = (Label) card.getRoot().lookup("#skills");
            Label role = (Label) card.getRoot().lookup("#role");
            assertEquals("Skills: C++, Java, SQL", skills.getText());
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
            Label skills = (Label) card.getRoot().lookup("#skills");
            Label role = (Label) card.getRoot().lookup("#role");
            assertFalse(skills.isVisible());
            assertFalse(skills.isManaged());
            assertFalse(role.isVisible());
            assertFalse(role.isManaged());
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
