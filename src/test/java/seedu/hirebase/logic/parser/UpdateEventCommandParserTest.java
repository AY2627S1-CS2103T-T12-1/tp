package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.UpdateEventCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;

public class UpdateEventCommandParserTest {
    private final UpdateEventCommandParser parser = new UpdateEventCommandParser();

    @Test
    public void parse_cancelAndRestore_acceptsExplicitBooleansIgnoringCase() {
        assertParseSuccess(parser, "event 1 cancel/true", update(null, null, null, null, true, null));
        assertParseSuccess(parser, " \tevent\n1\tcancel/ FALSE ", update(null, null, null, null, false, null));
    }

    @Test
    public void parse_allFieldsInAnyOrder_returnsCommand() {
        assertParseSuccess(parser, "event 1 c/2 to/05/11/2026 17:00 n/Final interview "
                + "cancel/true from/05/11/2026 16:00 desc/Bring portfolio c/1 c/2",
                update(new EventName("Final interview"), new EventDescription("Bring portfolio"),
                        LocalDateTime.of(2026, 11, 5, 16, 0), LocalDateTime.of(2026, 11, 5, 17, 0), true,
                        List.of(new CandidateId(2), new CandidateId(1))));
    }

    @Test
    public void parse_partialUpdates_preservesOmittedFields() {
        assertParseSuccess(parser, "event 1 n/Interview with Alex", update(new EventName("Interview with Alex"),
                null, null, null, null, null));
        assertParseSuccess(parser, "event 1 from/ 05/11/2026 16:00", update(null, null,
                LocalDateTime.of(2026, 11, 5, 16, 0), null, null, null));
        assertParseSuccess(parser, "event 1 desc/Round 2", update(null, new EventDescription("Round 2"),
                null, null, null, null));
    }

    @Test
    public void parse_emptyOptionalFields_clearsDescriptionAndCandidates() {
        assertParseSuccess(parser, "event 1 desc/ c/", update(null, EventDescription.EMPTY,
                null, null, null, List.of()));
    }

    @Test
    public void parse_missingOrInvalidRecordType_rejected() {
        for (String input : List.of("", "event", "1 cancel/true", "Event 1 cancel/true",
                "candidate 1 cancel/true", "event 1 2 cancel/true")) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
        assertParseFailure(parser, "event 1", UpdateEventCommand.MESSAGE_NO_FIELDS);
    }

    @Test
    public void parse_invalidIds_rejected() {
        for (String id : List.of("0", "-1", "+1", "1.5", "abc", "2147483648")) {
            assertParseFailure(parser, "event " + id + " cancel/true", UpdateEventCommand.MESSAGE_INVALID_ID);
            assertThrows(ParseException.class, () -> parser.parse("event 1 c/" + id));
        }
        assertThrows(ParseException.class, () -> parser.parse("event 1 c/ c/1"));
    }

    @Test
    public void parse_invalidCancellation_rejectedRatherThanSilentlyRestoring() {
        for (String value : List.of("", "yes", "1", "no", "tru", "false extra")) {
            assertParseFailure(parser, "event 1 cancel/" + value, UpdateEventCommand.MESSAGE_INVALID_CANCEL);
        }
    }

    @Test
    public void parse_invalidNameOrDates_rejected() {
        assertThrows(ParseException.class, () -> parser.parse("event 1 n/"));
        assertThrows(ParseException.class, () -> parser.parse("event 1 n/" + "a".repeat(101)));
        for (String prefix : List.of("from/", "to/")) {
            for (String value : List.of("", "31/04/2026 14:00", "2026-11-05 14:00", "05/11/2026 24:00")) {
                assertParseFailure(parser, "event 1 " + prefix + value, ParserUtil.MESSAGE_INVALID_DATE_TIME);
            }
        }
    }

    @Test
    public void parse_duplicateSingleValuePrefixes_rejected() {
        for (String prefix : List.of("n/", "desc/", "from/", "to/", "cancel/")) {
            assertParseFailure(parser, "event 1 " + prefix + "one " + prefix + "two",
                    Messages.getErrorMessageForDuplicatePrefixes(new Prefix(prefix)));
        }
    }

    @Test
    public void parse_unknownPrefix_rejectedEvenAfterFreeText() {
        for (String prefix : List.of("d/", "salary/", "cancelled/", "Cancel/", "cancell/")) {
            assertThrows(ParseException.class, () -> parser.parse("event 1 n/Interview " + prefix + "true"));
        }
        assertThrows(ParseException.class, () -> parser.parse("event 1 -cancel true"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    private UpdateEventCommand update(EventName name, EventDescription description, LocalDateTime start,
            LocalDateTime end, Boolean cancelled, List<CandidateId> candidates) {
        return new UpdateEventCommand(new EventId(1), name, description, start, end, cancelled, candidates);
    }
}
