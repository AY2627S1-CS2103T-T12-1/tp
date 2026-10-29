package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BENSON;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.testutil.EventBuilder;

public class AddEventCommandTest {

    private static final LocalDateTime FOURTEEN = LocalDateTime.of(2026, 11, 5, 14, 0);
    private static final LocalDateTime FIFTEEN = LocalDateTime.of(2026, 11, 5, 15, 0);

    private final Model model = modelWithAliceAndBenson();

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddEventCommand(null));
    }

    @Test
    public void constructor_eventWithId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new AddEventCommand(new EventBuilder().withEventId(3).build()));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddEventCommand(new EventBuilder().build()).execute(null));
    }

    @Test
    public void execute_noConflicts_addsEventAndShowsAssignedId() throws Exception {
        CommandResult result = new AddEventCommand(event(FOURTEEN, FIFTEEN, 1, 2)).execute(model);

        Event stored = model.getEventList().get(0);
        assertEquals(1, stored.getEventId().orElseThrow().getValue());
        assertEquals(String.format(AddEventCommand.MESSAGE_SUCCESS,
                "#1 Interview (05/11/2026 14:00 to 05/11/2026 15:00)"), result.getFeedbackToUser());
    }

    @Test
    public void execute_overlappingEvent_warnsWithIdsButStillAdds() throws Exception {
        model.addEvent(new EventBuilder().withName("Standup").withTimes(FOURTEEN, FIFTEEN).build());

        CommandResult result = new AddEventCommand(event(FOURTEEN.plusMinutes(30), FIFTEEN.plusMinutes(30)))
                .execute(model);

        assertEquals(2, model.getEventList().size());
        assertEquals(String.format(AddEventCommand.MESSAGE_SUCCESS,
                "#2 Interview (05/11/2026 14:30 to 05/11/2026 15:30)")
                + String.format(AddEventCommand.MESSAGE_CONFLICT_WARNING, 1)
                + "\n#1 Standup (05/11/2026 14:00 to 05/11/2026 15:00)", result.getFeedbackToUser());
    }

    @Test
    public void execute_severalOverlappingEvents_listsEachInOrder() throws Exception {
        model.addEvent(new EventBuilder().withName("First").withTimes(FOURTEEN, FIFTEEN).build());
        model.addEvent(new EventBuilder().withName("Elsewhere").withTimes(FIFTEEN, FIFTEEN.plusHours(1)).build());
        model.addEvent(new EventBuilder().withName("Third").withTimes(FOURTEEN.plusMinutes(15),
                FOURTEEN.plusMinutes(45)).build());

        String feedback = new AddEventCommand(event(FOURTEEN, FIFTEEN)).execute(model).getFeedbackToUser();

        assertTrue(feedback.endsWith(String.format(AddEventCommand.MESSAGE_CONFLICT_WARNING, 2)
                + "\n#1 First (05/11/2026 14:00 to 05/11/2026 15:00)"
                + "\n#3 Third (05/11/2026 14:15 to 05/11/2026 14:45)"), feedback);
    }

    @Test
    public void execute_backToBackAndCancelledEvents_noWarning() throws Exception {
        model.addEvent(new EventBuilder().withTimes(FOURTEEN.minusHours(1), FOURTEEN).build());
        model.addEvent(new EventBuilder().withTimes(FOURTEEN, FIFTEEN).withCancelled(true).build());

        String feedback = new AddEventCommand(event(FOURTEEN, FIFTEEN)).execute(model).getFeedbackToUser();

        assertFalse(feedback.contains("Warning"), feedback);
        assertEquals(3, model.getEventList().size());
    }

    @Test
    public void execute_unknownCandidateIds_rejectedListingAllAndNothingAdded() {
        long counterBefore = model.getHireBase().getNextEventId();

        assertThrows(CommandException.class, String.format(AddEventCommand.MESSAGE_UNKNOWN_CANDIDATES, "99, 98"), () ->
                new AddEventCommand(event(FOURTEEN, FIFTEEN, 1, 99, 2, 98)).execute(model));
        assertEquals(List.of(), model.getEventList());
        assertEquals(counterBefore, model.getHireBase().getNextEventId());
    }

    @Test
    public void execute_deletedCandidateId_rejected() {
        model.deletePerson(model.getHireBase().getPersonList().get(0));

        assertThrows(CommandException.class, String.format(AddEventCommand.MESSAGE_UNKNOWN_CANDIDATES, "1"), () ->
                new AddEventCommand(event(FOURTEEN, FIFTEEN, 1)).execute(model));
    }

    @Test
    public void execute_eventIdsExhausted_rejectedAndNothingAdded() {
        HireBase full = new HireBase();
        full.reserveEventIdsUpTo((long) Integer.MAX_VALUE + 1);
        Model fullModel = new ModelManager(full, new UserPrefs());

        assertThrows(CommandException.class, HireBase.MESSAGE_EVENT_IDS_EXHAUSTED, () ->
                new AddEventCommand(event(FOURTEEN, FIFTEEN)).execute(fullModel));
        assertEquals(List.of(), fullModel.getEventList());
    }

    @Test
    public void equals() {
        AddEventCommand command = new AddEventCommand(event(FOURTEEN, FIFTEEN));

        assertTrue(command.equals(command));
        assertTrue(command.equals(new AddEventCommand(event(FOURTEEN, FIFTEEN))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(new AddEventCommand(event(FOURTEEN, FIFTEEN.plusHours(1)))));
    }

    @Test
    public void toStringMethod() {
        Event event = event(FOURTEEN, FIFTEEN);

        assertEquals(AddEventCommand.class.getCanonicalName() + "{toAdd=" + event + "}",
                new AddEventCommand(event).toString());
    }

    private static Event event(LocalDateTime start, LocalDateTime end, int... candidateIds) {
        return new EventBuilder().withTimes(start, end).withCandidateIds(candidateIds).build();
    }

    private static Model modelWithAliceAndBenson() {
        // Alice gets candidate ID 1 and Benson gets 2.
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        return model;
    }
}
