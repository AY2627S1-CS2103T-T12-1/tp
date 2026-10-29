package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;
import seedu.hirebase.testutil.EventBuilder;

public class UpdateEventCommandTest {
    private final EventId id = new EventId(1);
    private final EventName name = new EventName("Final round");
    private final EventDescription description = new EventDescription("Bring portfolio");
    private final LocalDateTime start = EventBuilder.DEFAULT_START;
    private final LocalDateTime end = EventBuilder.DEFAULT_END;
    private final List<CandidateId> candidates = List.of(new CandidateId(1));

    @Test
    public void constructor_requiresIdAndAtLeastOneField() {
        assertThrows(NullPointerException.class, () ->
                new UpdateEventCommand(null, null, null, null, null, true, null));
        assertThrows(IllegalArgumentException.class, () ->
                new UpdateEventCommand(id, null, null, null, null, null, null));
        assertThrows(NullPointerException.class, () ->
                new UpdateEventCommand(id, null, null, null, null, true, null).execute(null));
    }

    @Test
    public void constructor_mutableCandidateIds_copiesIds() throws Exception {
        Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());
        model.addEvent(new EventBuilder().withCandidateIds(1).build());
        List<CandidateId> ids = new ArrayList<>(List.of(new CandidateId(2), new CandidateId(2)));
        UpdateEventCommand command = new UpdateEventCommand(id, null, null, null, null, null, ids);
        ids.clear();
        command.execute(model);
        assertEquals(List.of(new CandidateId(2)), model.getEventList().getFirst().getCandidateIds());
    }

    @Test
    public void equalsAndHashCode_compareAllSuppliedFields() {
        UpdateEventCommand command = new UpdateEventCommand(id, name, description, start, end, true, candidates);
        assertEquals(command, command);
        UpdateEventCommand equivalent = new UpdateEventCommand(id, name, description, start, end, true,
                List.of(new CandidateId(1), new CandidateId(1)));
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new UpdateEventCommand(new EventId(2), name, description,
                start, end, true, candidates));
        assertNotEquals(command, new UpdateEventCommand(id, null, description, start, end, true, candidates));
        assertNotEquals(command, new UpdateEventCommand(id, name, null, start, end, true, candidates));
        assertNotEquals(command, new UpdateEventCommand(id, name, description, null, end, true, candidates));
        assertNotEquals(command, new UpdateEventCommand(id, name, description, start, null, true, candidates));
        assertNotEquals(command, new UpdateEventCommand(id, name, description, start, end, false, candidates));
        assertNotEquals(command, new UpdateEventCommand(id, name, description, start, end, true, null));
    }

    @Test
    public void toString_includesTargetAndCancellation() {
        String text = new UpdateEventCommand(id, null, null, null, null, true, null).toString();
        assertTrue(text.contains("targetId=1"));
        assertTrue(text.contains("cancelled=true"));
    }
}
