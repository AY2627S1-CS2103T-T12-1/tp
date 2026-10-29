package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UsageMessageTest {

    @Test
    public void usageMessages_mentionOtherRecordTypes() {
        assertTrue(AddCommand.MESSAGE_USAGE.contains(AddJobCommand.MESSAGE_USAGE));
        assertTrue(DeleteCommand.MESSAGE_USAGE.contains(DeleteJobCommand.MESSAGE_USAGE));
        assertTrue(DeleteCommand.MESSAGE_USAGE.contains(DeleteEventCommand.MESSAGE_USAGE));
        assertTrue(EditCommand.MESSAGE_USAGE.contains(EditJobCommand.MESSAGE_USAGE));
        assertTrue(ListCommand.MESSAGE_USAGE.contains(ListJobsCommand.MESSAGE_USAGE));
        assertTrue(ViewCommand.MESSAGE_USAGE.contains(ViewEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void clearAndExitMessages_useHireBaseWording() {
        assertEquals("All candidates, job openings and events have been cleared!", ClearCommand.MESSAGE_SUCCESS);
        assertFalse(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT.toLowerCase().contains("address book"));
        assertTrue(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT.contains("HireBase"));
    }

    @Test
    public void listResultMessage_handlesZeroOneAndMany() {
        assertEquals(ListCommand.MESSAGE_NO_CANDIDATES, ListCommand.getResultMessage(0));
        assertEquals("Listed 1 candidate.", ListCommand.getResultMessage(1));
        assertEquals("Listed 2 candidates.", ListCommand.getResultMessage(2));
    }
}
