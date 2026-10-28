package seedu.hirebase.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.event.EventName;

public class ParserUtilEventTest {

    //// parseDateTime

    @Test
    public void parseDateTime_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDateTime(null));
    }

    @Test
    public void parseDateTime_valid_returnsDateTime() throws Exception {
        assertEquals(LocalDateTime.of(2026, 11, 5, 14, 0), ParserUtil.parseDateTime("05/11/2026 14:00"));
    }

    @Test
    public void parseDateTime_surroundingWhitespace_trimmed() throws Exception {
        assertEquals(LocalDateTime.of(2026, 11, 5, 14, 0), ParserUtil.parseDateTime("  05/11/2026 14:00  "));
    }

    @Test
    public void parseDateTime_boundaryTimesAndLeapDay_success() throws Exception {
        assertEquals(LocalDateTime.of(2026, 11, 5, 0, 0), ParserUtil.parseDateTime("05/11/2026 00:00"));
        assertEquals(LocalDateTime.of(2026, 11, 5, 23, 59), ParserUtil.parseDateTime("05/11/2026 23:59"));
        assertEquals(LocalDateTime.of(2028, 2, 29, 10, 0), ParserUtil.parseDateTime("29/02/2028 10:00"));
        assertEquals(LocalDateTime.of(2026, 12, 31, 10, 0), ParserUtil.parseDateTime("31/12/2026 10:00"));
    }

    @Test
    public void parseDateTime_invalid_throwsParseExceptionWithFormat() {
        for (String input : List.of(
                "5/11/2026 14:00", // single-digit day
                "05/1/2026 14:00", // single-digit month
                "05/11/2026 9:00", // single-digit hour
                "31/04/2026 14:00", // April has 30 days
                "30/02/2026 14:00", // February never has 30 days
                "29/02/2027 14:00", // not a leap year
                "32/01/2026 14:00", // day out of range
                "05/13/2026 14:00", // month out of range
                "00/11/2026 14:00", // day zero
                "05/11/2026 24:00", // hour out of range
                "05/11/2026 14:60", // minute out of range
                "05/11/2026 14:00:00", // seconds
                "2026-11-05 14:00", // ISO date
                "05-11-2026 14:00", // dashes
                "05/11/2026T14:00", // T separator
                "05/11/26 14:00", // two-digit year
                "05/11/02026 14:00", // five-digit year
                "05/11/2026", // date only
                "14:00", // time only
                "05/11/2026  14:00", // two spaces
                "05/11/2026\t14:00", // tab
                "05/11/2026 2pm", // 12-hour clock
                "tomorrow 14:00", // words
                "", // empty
                "   ")) { // blank
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_DATE_TIME, () ->
                    ParserUtil.parseDateTime(input));
        }
    }

    //// parseEventName

    @Test
    public void parseEventName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEventName(null));
    }

    @Test
    public void parseEventName_blank_throwsParseException() {
        assertThrows(ParseException.class, EventName.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseEventName(""));
        assertThrows(ParseException.class, EventName.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseEventName("   "));
    }

    @Test
    public void parseEventName_valid_returnsTrimmedName() throws Exception {
        assertEquals(new EventName("Interview with Alex"), ParserUtil.parseEventName("  Interview with Alex  "));
        assertEquals(new EventName("面试 · O'Brien"), ParserUtil.parseEventName("面试 · O'Brien"));
    }
}
