package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.Messages;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.testutil.EventBuilder;
import seedu.hirebase.testutil.PersonBuilder;

public class ViewEventCommandTest {

    private static final EventId ID_1 = new EventId(1);
    private static final EventId ID_2 = new EventId(2);

    private final Model model = new ModelManager();

    @Test
    public void execute_allFieldsPresent_showsFullDetails() throws Exception {
        model.addPerson(new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com").build());
        model.addPerson(new PersonBuilder().withName("Bob Lee").withEmail("bob@example.com").build());
        model.addEvent(new EventBuilder().withName("Technical interview").withDescription("Round 2, bring laptop")
                .withTimes(LocalDateTime.of(2026, 11, 5, 9, 5), LocalDateTime.of(2026, 11, 5, 10, 30))
                .withCandidateIds(2, 1).build());

        CommandResult result = new ViewEventCommand(ID_1).execute(model);

        // Candidates keep the order they were added in. An event that is not cancelled shows no status.
        assertEquals("Event:       #1 Technical interview\n"
                + "Description: Round 2, bring laptop\n"
                + "Start:       05/11/2026 09:05\n"
                + "End:         05/11/2026 10:30\n"
                + "Candidates:  #2 Bob Lee, #1 Alice Tan", result.getFeedbackToUser());
        assertEquals(CommandResult.ListView.KEEP, result.getListView());
    }

    @Test
    public void execute_noDescriptionAndNoCandidates_showsPlaceholders() throws Exception {
        model.addEvent(new EventBuilder().build());

        String text = new ViewEventCommand(ID_1).execute(model).getFeedbackToUser();

        assertTrue(text.contains("Description: —\n"));
        assertTrue(text.endsWith("Candidates:  —"));
    }

    @Test
    public void execute_cancelledEvent_showsCancelledStatusBeforeCandidates() throws Exception {
        model.addEvent(new EventBuilder().withCancelled(true).build());

        String text = new ViewEventCommand(ID_1).execute(model).getFeedbackToUser();

        assertEquals("Event:       #1 Interview\n"
                + "Description: —\n"
                + "Start:       05/11/2026 14:00\n"
                + "End:         05/11/2026 15:00\n"
                + "Status:      Cancelled\n"
                + "Candidates:  —", text);
    }

    @Test
    public void execute_notCancelledEvent_showsNoStatus() throws Exception {
        // Past and future events alike: only cancellation is shown.
        model.addEvent(new EventBuilder().withTimes(LocalDateTime.of(2020, 1, 1, 9, 0),
                LocalDateTime.of(2020, 1, 1, 10, 0)).build());
        model.addEvent(new EventBuilder().build());

        assertFalse(new ViewEventCommand(ID_1).execute(model).getFeedbackToUser().contains("Status"));
        assertFalse(new ViewEventCommand(ID_2).execute(model).getFeedbackToUser().contains("Status"));
    }

    @Test
    public void execute_candidateDeletedAfterEventAdded_showsDeletedMarker() throws Exception {
        model.addPerson(new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com").build());
        model.addPerson(new PersonBuilder().withName("Bob Lee").withEmail("bob@example.com").build());
        model.addEvent(new EventBuilder().withCandidateIds(1, 2).build());
        model.deletePerson(model.getHireBase().getPersonByCandidateId(new CandidateId(1)).orElseThrow());

        String text = new ViewEventCommand(ID_1).execute(model).getFeedbackToUser();

        // The event keeps the deleted candidate's ID; only the name is gone.
        assertTrue(text.endsWith("Candidates:  #1 (deleted), #2 Bob Lee"));
    }

    @Test
    public void execute_allCandidatesMissing_showsEveryIdAsDeleted() throws Exception {
        // As loaded from a data file that names candidates no longer present.
        model.addEvent(new EventBuilder().withCandidateIds(4, 9).build());

        String text = new ViewEventCommand(ID_1).execute(model).getFeedbackToUser();

        assertTrue(text.endsWith("Candidates:  #4 (deleted), #9 (deleted)"));
    }

    @Test
    public void execute_idDiffersFromPosition_viewsEventWithThatId() throws Exception {
        model.addEvent(new EventBuilder().withName("First").build());
        model.addEvent(new EventBuilder().withName("Second").build());
        model.deleteEvent(model.findEvent(ID_1).orElseThrow());

        // Second is now first in the list, but its ID is still 2.
        assertTrue(new ViewEventCommand(ID_2).execute(model).getFeedbackToUser()
                .startsWith("Event:       #2 Second\n"));
        assertCommandFailure(new ViewEventCommand(ID_1), model,
                String.format(Messages.MESSAGE_EVENT_ID_NOT_FOUND, 1));
    }

    @Test
    public void execute_unknownEventId_throwsCommandException() {
        // No events at all.
        assertCommandFailure(new ViewEventCommand(ID_1), model,
                String.format(Messages.MESSAGE_EVENT_ID_NOT_FOUND, 1));

        // One event, but not this ID; the largest ID is also handled.
        model.addEvent(new EventBuilder().build());
        assertCommandFailure(new ViewEventCommand(ID_2), model,
                String.format(Messages.MESSAGE_EVENT_ID_NOT_FOUND, 2));
        assertCommandFailure(new ViewEventCommand(new EventId(Integer.MAX_VALUE)), model,
                String.format(Messages.MESSAGE_EVENT_ID_NOT_FOUND, Integer.MAX_VALUE));
    }

    @Test
    public void execute_validId_leavesModelUnchanged() throws Exception {
        model.addPerson(new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com").build());
        model.addPerson(new PersonBuilder().withName("Bob Lee").withEmail("bob@example.com").build());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Bob Lee"));
        model.addEvent(new EventBuilder().withCandidateIds(1).build());
        Model expected = new ModelManager(model.getHireBase(), new UserPrefs());
        expected.updateFilteredPersonList(person -> person.getName().fullName.equals("Bob Lee"));

        new ViewEventCommand(ID_1).execute(model);

        // Same data, same event ID counter, and the candidate list is still filtered.
        assertEquals(expected, model);
        assertEquals(2, model.getHireBase().getNextEventId());
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewEventCommand(ID_1).execute(null));
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewEventCommand(null));
    }

    @Test
    public void equals() {
        ViewEventCommand first = new ViewEventCommand(ID_1);
        assertTrue(first.equals(first));
        assertTrue(first.equals(new ViewEventCommand(new EventId(1))));
        assertFalse(first.equals(new ViewEventCommand(ID_2)));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
        // A candidate view with the same number is a different command.
        assertFalse(first.equals(new ViewCommand(new CandidateId(1))));
    }

    @Test
    public void toStringMethod() {
        String expected = ViewEventCommand.class.getCanonicalName() + "{targetId=" + ID_1 + "}";
        assertEquals(expected, new ViewEventCommand(ID_1).toString());
    }
}
