package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.ViewCalendarCommand;
import seedu.hirebase.logic.commands.ViewCommand;
import seedu.hirebase.logic.commands.ViewEventCommand;
import seedu.hirebase.model.calendar.CalendarViewOptions;
import seedu.hirebase.model.calendar.CalendarViewOptions.Period;
import seedu.hirebase.model.event.EventId;

public class ViewCommandParserTest {

    private static final String MESSAGE_VIEW_USAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
    private static final String MESSAGE_VIEW_EVENT_USAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewEventCommand.MESSAGE_USAGE);
    private static final String MESSAGE_VIEW_CALENDAR_USAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCalendarCommand.MESSAGE_USAGE);

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, " candidate 1", new ViewCommand(ID_FIRST_PERSON));
        // Extra surrounding whitespace is tolerated.
        assertParseSuccess(parser, "   candidate   1  ", new ViewCommand(ID_FIRST_PERSON));
    }

    @Test
    public void parse_missingOrWrongRecordType_failure() {
        assertParseFailure(parser, " 1", MESSAGE_VIEW_USAGE); // no record-type keyword
        assertParseFailure(parser, " person 1", MESSAGE_VIEW_USAGE); // wrong keyword
        assertParseFailure(parser, "", MESSAGE_VIEW_USAGE); // empty input
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String input : new String[] {" candidate", " candidate 0", " candidate -1", " candidate abc",
            " candidate 1.5", " candidate 1 2", " candidate 2147483648"}) {
            assertParseFailure(parser, input, MESSAGE_VIEW_USAGE);
        }
    }

    @Test
    public void parse_validEventArgs_returnsViewEventCommand() {
        assertParseSuccess(parser, " event 1", new ViewEventCommand(new EventId(1)));
        // Extra whitespace, including tabs, is tolerated.
        assertParseSuccess(parser, "   event \t  12  ", new ViewEventCommand(new EventId(12)));
        // Largest event ID.
        assertParseSuccess(parser, " event 2147483647", new ViewEventCommand(new EventId(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_invalidEventId_showsViewEventUsage() {
        for (String input : new String[] {" event", " event   ", " event 0", " event -1", " event +1",
            " event abc", " event 1.5", " event 1 2", " event 2147483648", " event #1"}) {
            assertParseFailure(parser, input, MESSAGE_VIEW_EVENT_USAGE);
        }
    }

    @Test
    public void parse_eventKeywordNotExact_showsViewUsage() {
        // The keyword is matched exactly, the same way as candidate.
        for (String input : new String[] {" events 1", " Event 1", " EVENT 1", " event1", " evnt 1"}) {
            assertParseFailure(parser, input, MESSAGE_VIEW_USAGE);
        }
    }

    @Test
    public void parse_validCalendarArgs_returnsViewCalendarCommand() {
        assertParseSuccess(parser, " calendar", new ViewCalendarCommand(CalendarViewOptions.DEFAULT));
        assertParseSuccess(parser, " calendar period/month upcoming/true",
                new ViewCalendarCommand(new CalendarViewOptions(Period.MONTH, false, true)));
        assertParseSuccess(parser, " calendar period/ today cancelled/ true",
                new ViewCalendarCommand(new CalendarViewOptions(Period.TODAY, true, false)));
        assertParseSuccess(parser, " calendar period/WeEk cancelled/FALSE upcoming/TRUE",
                new ViewCalendarCommand(new CalendarViewOptions(Period.WEEK, false, true)));
    }

    @Test
    public void parse_invalidCalendarArgs_showsViewCalendarUsage() {
        for (String input : new String[] {" calendar period/", " calendar period/year",
            " calendar cancelled/yes", " calendar upcoming/", " calendar period/month period/week",
            " calendar unknown/value", " calendar unexpected"}) {
            assertParseFailure(parser, input, MESSAGE_VIEW_CALENDAR_USAGE);
        }
        assertParseFailure(parser, " Calendar", MESSAGE_VIEW_USAGE);
    }
}
