package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.testutil.EventBuilder;

public class ClearCommandTest {

    @Test
    public void execute_emptyHireBase_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyHireBase_success() {
        Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalHireBase(), new UserPrefs());
        expectedModel.setHireBase(new HireBase());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_withEvents_clearsEventsButKeepsEventIdCounter() {
        Model model = new ModelManager();
        model.addEvent(new EventBuilder().build());
        model.addEvent(new EventBuilder().build());

        new ClearCommand().execute(model);

        assertEquals(List.of(), model.getEventList());
        // Event IDs used before clearing are never reused.
        assertEquals(new EventId(3), model.addEvent(new EventBuilder().build()).getEventId().orElseThrow());
    }

}
