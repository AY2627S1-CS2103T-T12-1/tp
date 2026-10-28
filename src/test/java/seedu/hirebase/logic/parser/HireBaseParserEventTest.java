package seedu.hirebase.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.AddCommand;
import seedu.hirebase.logic.commands.AddEventCommand;
import seedu.hirebase.logic.commands.AddJobCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.testutil.EventBuilder;

public class HireBaseParserEventTest {

    private static final String EVENT_ARGS = " n/Interview from/05/11/2026 14:00 to/05/11/2026 15:00";

    private final HireBaseParser parser = new HireBaseParser();

    @Test
    public void parseCommand_addEvent_routesToAddEventCommand() throws Exception {
        AddEventCommand expected = new AddEventCommand(new EventBuilder().withName("Interview")
                .withTimes(LocalDateTime.of(2026, 11, 5, 14, 0), LocalDateTime.of(2026, 11, 5, 15, 0)).build());

        assertEquals(expected, parser.parseCommand("add event" + EVENT_ARGS));
        assertEquals(expected, parser.parseCommand("  add   event  " + EVENT_ARGS + "  "));
        // Any whitespace separates the words, like add job, not only spaces.
        assertEquals(expected, parser.parseCommand("add\tevent\t" + EVENT_ARGS));
    }

    @Test
    public void parseCommand_addEventWithoutArguments_showsAddEventUsage() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE);

        assertThrows(ParseException.class, usage, () -> parser.parseCommand("add event"));
        assertThrows(ParseException.class, usage, () -> parser.parseCommand("add event   "));
    }

    @Test
    public void parseCommand_wordStartingWithEvent_notRoutedToAddEvent() {
        // Only the exact word "event" selects add event; anything else falls through to add candidate.
        String candidateUsage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertThrows(ParseException.class, candidateUsage, () -> parser.parseCommand("add events" + EVENT_ARGS));
        assertThrows(ParseException.class, candidateUsage, () -> parser.parseCommand("add eventn/Interview"));
        assertThrows(ParseException.class, candidateUsage, () -> parser.parseCommand("add Event" + EVENT_ARGS));
    }

    @Test
    public void parseCommand_addJobAndCandidate_stillRoutedCorrectly() throws Exception {
        assertTrue(parser.parseCommand("add job t/Engineer d/Tech desc/Build things") instanceof AddJobCommand);
        assertFalse(parser.parseCommand("add candidate n/Amy p/91234567 e/amy@example.com a/Street 1 s/Java "
                + "r/Engineer") instanceof AddEventCommand);
    }
}
