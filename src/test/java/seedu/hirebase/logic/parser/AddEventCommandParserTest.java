package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.AddEventCommand;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventName;
import seedu.hirebase.testutil.EventBuilder;

public class AddEventCommandParserTest {

    private static final String NAME = " n/Interview with Alex";
    private static final String FROM = " from/05/11/2026 14:00";
    private static final String TO = " to/05/11/2026 15:00";
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE);

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 5, 14, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 11, 5, 15, 0);

    private final AddEventCommandParser parser = new AddEventCommandParser();

    //// valid input

    @Test
    public void parse_requiredFieldsOnly_success() {
        Event expected = new EventBuilder().withName("Interview with Alex").withTimes(START, END).build();

        assertParseSuccess(parser, NAME + FROM + TO, new AddEventCommand(expected));
    }

    @Test
    public void parse_allFields_success() {
        Event expected = new EventBuilder().withName("Interview with Alex").withTimes(START, END)
                .withDescription("Round 2, bring portfolio").withCandidateIds(3, 7).build();

        assertParseSuccess(parser, NAME + FROM + TO + " desc/Round 2, bring portfolio c/3 c/7",
                new AddEventCommand(expected));
    }

    @Test
    public void parse_fieldsInAnyOrderWithExtraSpaces_success() {
        Event expected = new EventBuilder().withName("Interview with Alex").withTimes(START, END)
                .withCandidateIds(7, 3).build();

        assertParseSuccess(parser, "   c/7  " + TO + "   " + NAME + "  c/3 " + FROM + "   ",
                new AddEventCommand(expected));
    }

    @Test
    public void parse_emptyDescription_usesEmptyDescription() {
        Event expected = new EventBuilder().withName("Interview with Alex").withTimes(START, END).build();

        assertParseSuccess(parser, NAME + FROM + TO + " desc/", new AddEventCommand(expected));
    }

    @Test
    public void parse_repeatedCandidateId_keptOnce() {
        Event expected = new EventBuilder().withName("Interview with Alex").withTimes(START, END)
                .withCandidateIds(1).build();

        assertParseSuccess(parser, NAME + FROM + TO + " c/1 c/1", new AddEventCommand(expected));
    }

    @Test
    public void parse_unicodeName_success() {
        Event expected = new EventBuilder().withName("面试 · O'Brien").withTimes(START, END).build();

        assertParseSuccess(parser, " n/面试 · O'Brien" + FROM + TO, new AddEventCommand(expected));
    }

    @Test
    public void parse_boundaryTimes_success() {
        Event expected = new EventBuilder().withName("All day").withTimes(START.withHour(0),
                START.withHour(23).withMinute(59)).build();

        assertParseSuccess(parser, " n/All day from/05/11/2026 00:00 to/05/11/2026 23:59",
                new AddEventCommand(expected));
    }

    //// missing or extra parts

    @Test
    public void parse_missingRequiredPrefix_failure() {
        assertParseFailure(parser, FROM + TO, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, NAME + TO, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, NAME + FROM, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, " foo" + NAME + FROM + TO, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicateSingleValuedPrefix_failure() {
        assertParseFailure(parser, NAME + NAME + FROM + TO,
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME));
        assertParseFailure(parser, NAME + FROM + FROM + TO,
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_FROM));
        assertParseFailure(parser, NAME + FROM + TO + TO,
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TO));
        assertParseFailure(parser, NAME + FROM + TO + " desc/a desc/b",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DESCRIPTION));
    }

    //// invalid values

    @Test
    public void parse_blankName_failure() {
        assertParseFailure(parser, " n/" + FROM + TO, EventName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/   " + FROM + TO, EventName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nameAtLengthLimit_success() {
        String longestName = "a".repeat(EventName.MAX_LENGTH);
        Event expected = new EventBuilder().withName(longestName).withTimes(START, END).build();

        assertParseSuccess(parser, " n/" + longestName + FROM + TO, new AddEventCommand(expected));
    }

    @Test
    public void parse_nameOverLengthLimit_failure() {
        assertParseFailure(parser, " n/" + "a".repeat(EventName.MAX_LENGTH + 1) + FROM + TO,
                EventName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDateTime_failure() {
        assertParseFailure(parser, NAME + " from/31/04/2026 14:00" + TO, ParserUtil.MESSAGE_INVALID_DATE_TIME);
        assertParseFailure(parser, NAME + FROM + " to/15:00", ParserUtil.MESSAGE_INVALID_DATE_TIME);
        assertParseFailure(parser, NAME + " from/" + TO, ParserUtil.MESSAGE_INVALID_DATE_TIME);
    }

    @Test
    public void parse_invalidTimeRange_failure() {
        // Zero length, end before start, and an event crossing midnight.
        assertParseFailure(parser, NAME + FROM + " to/05/11/2026 14:00", Event.MESSAGE_TIME_CONSTRAINTS);
        assertParseFailure(parser, NAME + FROM + " to/05/11/2026 13:59", Event.MESSAGE_TIME_CONSTRAINTS);
        assertParseFailure(parser, NAME + " from/05/11/2026 23:00 to/06/11/2026 00:00",
                Event.MESSAGE_TIME_CONSTRAINTS);
    }

    @Test
    public void parse_invalidCandidateId_failure() {
        for (String id : List.of("0", "-1", "+1", "abc", "1.5", "", "1 2", "99999999999")) {
            assertParseFailure(parser, NAME + FROM + TO + " c/" + id, CandidateId.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_departmentPrefix_rejectedWithHint() {
        // d/ means department in add job; for events it would otherwise be silently absorbed into another field.
        assertParseFailure(parser, NAME + FROM + TO + " d/Round 2", AddEventCommandParser.MESSAGE_DEPARTMENT_PREFIX);
        assertParseFailure(parser, NAME + " d/Round 2" + FROM + TO, AddEventCommandParser.MESSAGE_DEPARTMENT_PREFIX);
        assertParseFailure(parser, " d/Round 2" + NAME + FROM + TO, AddEventCommandParser.MESSAGE_DEPARTMENT_PREFIX);
        assertParseFailure(parser, NAME + FROM + TO + " d/", AddEventCommandParser.MESSAGE_DEPARTMENT_PREFIX);
        assertParseFailure(parser, NAME + FROM + TO + " desc/Round 2 d/x",
                AddEventCommandParser.MESSAGE_DEPARTMENT_PREFIX);
    }

    @Test
    public void parse_departmentPrefixWithoutSpaceBefore_partOfValue() {
        // Like every prefix, d/ only counts when a space comes right before it.
        Event expected = new EventBuilder().withName("Q3 And/Or review").withTimes(START, END)
                .withDescription("Bring CV and/or portfolio").build();

        assertParseSuccess(parser, " n/Q3 And/Or review" + FROM + TO + " desc/Bring CV and/or portfolio",
                new AddEventCommand(expected));
    }

    @Test
    public void parse_prefixInsideName_splitsAtPrefix() {
        // Known limitation, shared with every AB3 command: " to/" inside a value starts a new field.
        assertParseFailure(parser, " n/Talk to/Bob" + FROM + TO,
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TO));
    }
}
