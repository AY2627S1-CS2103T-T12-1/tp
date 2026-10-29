package seedu.hirebase.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.NullNode;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.commons.util.JsonUtil;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;
import seedu.hirebase.testutil.EventBuilder;

public class JsonAdaptedEventTest {

    private static final String VALID_START = "2026-11-05T14:00";
    private static final String VALID_END = "2026-11-05T15:00";

    //// round trip

    @Test
    public void roundTrip_fullEvent_preservesEveryField() throws Exception {
        Event event = new EventBuilder().withName("Final interview").withDescription("Bring portfolio")
                .withTimes(LocalDateTime.of(2026, 2, 28, 9, 5), LocalDateTime.of(2026, 2, 28, 23, 59))
                .withCancelled(true).withCandidateIds(3, 1).withEventId(42).build();

        assertEquals(event, reload(new JsonAdaptedEvent(event)));
    }

    @Test
    public void roundTrip_eventWithoutIdOrCandidates_preservesEveryField() throws Exception {
        Event event = new EventBuilder().build();

        assertEquals(event, reload(new JsonAdaptedEvent(event)));
    }

    @Test
    public void serialize_datesUseIsoMinutePrecision() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedEvent(new EventBuilder()
                .withTimes(LocalDateTime.of(2026, 1, 2, 3, 4), LocalDateTime.of(2026, 1, 2, 5, 6)).build()));

        assertTrue(json.contains("\"start\" : \"2026-01-02T03:04\""), json);
        assertTrue(json.contains("\"end\" : \"2026-01-02T05:06\""), json);
    }

    //// valid optional fields

    @Test
    public void toModelType_onlyRequiredFields_usesDefaults() throws Exception {
        Event event = load("{" + required() + "}");

        assertTrue(event.getEventId().isEmpty());
        assertEquals(EventDescription.EMPTY, event.getDescription());
        assertFalse(event.isCancelled());
        assertEquals(List.of(), event.getCandidateIds());
    }

    @Test
    public void toModelType_nullOptionalFields_usesDefaults() throws Exception {
        Event event = load("{" + required()
                + ",\"id\":null,\"description\":null,\"cancelled\":null,\"candidateIds\":null}");

        assertTrue(event.getEventId().isEmpty());
        assertEquals(EventDescription.EMPTY, event.getDescription());
        assertFalse(event.isCancelled());
        assertEquals(List.of(), event.getCandidateIds());
    }

    @Test
    public void toModelType_allFieldsPresent_success() throws Exception {
        Event event = load("{" + required()
                + ",\"id\":7,\"description\":\"Round 2\",\"cancelled\":true,\"candidateIds\":[2,5,2]}");

        assertEquals(new EventId(7), event.getEventId().orElseThrow());
        assertEquals("Round 2", event.getDescription().toString());
        assertTrue(event.isCancelled());
        // Repeated candidate IDs are dropped, keeping the first occurrence.
        assertEquals(List.of(new CandidateId(2), new CandidateId(5)), event.getCandidateIds());
    }

    @Test
    public void toModelType_boundaryTimesAndLeapDay_success() throws Exception {
        Event event = load(json("Leap", "2028-02-29T00:00", "2028-02-29T23:59"));

        assertEquals(LocalDateTime.of(2028, 2, 29, 0, 0), event.getStart());
        assertEquals(LocalDateTime.of(2028, 2, 29, 23, 59), event.getEnd());
    }

    //// missing or invalid required fields

    @Test
    public void toModelType_missingName_throwsWithFieldName() {
        assertMissing("{\"start\":\"" + VALID_START + "\",\"end\":\"" + VALID_END + "\"}", "name");
    }

    @Test
    public void toModelType_missingStart_throwsWithFieldName() {
        assertMissing("{\"name\":\"Interview\",\"end\":\"" + VALID_END + "\"}", "start");
    }

    @Test
    public void toModelType_missingEnd_throwsWithFieldName() {
        assertMissing("{\"name\":\"Interview\",\"start\":\"" + VALID_START + "\"}", "end");
    }

    @Test
    public void toModelType_blankName_rejected() {
        assertInvalid(json("   ", VALID_START, VALID_END));
        assertInvalid(json("", VALID_START, VALID_END));
    }

    @Test
    public void toModelType_nameAtLengthLimit_success() throws Exception {
        String longestName = "a".repeat(EventName.MAX_LENGTH);

        assertEquals(longestName, load(json(longestName, VALID_START, VALID_END)).getName().toString());
    }

    @Test
    public void toModelType_nameOverLengthLimit_rejected() {
        Exception e = assertInvalid(json("a".repeat(EventName.MAX_LENGTH + 1), VALID_START, VALID_END));
        assertEquals(EventName.MESSAGE_CONSTRAINTS, e.getMessage());
    }

    @Test
    public void toModelType_malformedDates_rejected() {
        for (String date : List.of(
                "05/11/2026 14:00", // display format, not storage format
                "2026-11-05T14:00:00", // seconds
                "2026-11-05 14:00", // space instead of T
                "2026-11-5T14:00", // single-digit day
                "2026-11-05T9:00", // single-digit hour
                "2026-11-05", // date only
                "14:00", // time only
                "", // empty
                "2026-02-30T10:00", // non-existent date
                "2027-02-29T10:00", // not a leap year
                "2026-04-31T10:00", // April has 30 days
                "2026-11-05T24:00", // hour out of range
                "2026-11-05T14:60", // minute out of range
                "+2026-11-05T14:00")) { // signed year
            assertInvalid(json("Interview", date, VALID_END));
            assertInvalid(json("Interview", VALID_START, date));
        }
    }

    @Test
    public void toModelType_nonTextDate_rejected() {
        assertInvalid("{\"name\":\"Interview\",\"start\":20261105,\"end\":\"" + VALID_END + "\"}");
        assertInvalid("{\"name\":\"Interview\",\"start\":{},\"end\":\"" + VALID_END + "\"}");
    }

    @Test
    public void toModelType_invalidTimeRange_rejected() {
        // Zero length, end before start, and an event crossing midnight.
        assertInvalid(json("Interview", VALID_START, VALID_START));
        assertInvalid(json("Interview", VALID_END, VALID_START));
        assertInvalid(json("Interview", "2026-11-05T23:00", "2026-11-06T00:00"));
    }

    //// invalid optional fields

    @Test
    public void toModelType_invalidIds_rejected() {
        for (String id : List.of("0", "-1", "2147483648", "9223372036854775808", "1.5", "\"7\"", "true", "{}",
                "[]")) {
            assertInvalid("{" + required() + ",\"id\":" + id + "}");
        }
    }

    @Test
    public void toModelType_invalidCancelled_rejected() {
        for (String cancelled : List.of("\"true\"", "1", "0", "{}", "[]")) {
            assertInvalid("{" + required() + ",\"cancelled\":" + cancelled + "}");
        }
    }

    @Test
    public void toModelType_invalidCandidateIds_rejected() {
        for (String ids : List.of("1", "\"1\"", "{}", "[null]", "[0]", "[-1]", "[\"1\"]", "[1.5]", "[true]",
                "[2147483648]", "[9223372036854775808]", "[[1]]")) {
            assertInvalid("{" + required() + ",\"candidateIds\":" + ids + "}");
        }
    }

    @Test
    public void constructor_javaNullAndJsonNullOptionalNodes_treatedAsAbsent() throws Exception {
        Event fromJavaNull = new JsonAdaptedEvent(null, "Interview", null, VALID_START, VALID_END, null, null)
                .toModelType();
        Event fromJsonNull = new JsonAdaptedEvent(NullNode.getInstance(), "Interview", null, VALID_START,
                VALID_END, NullNode.getInstance(), NullNode.getInstance()).toModelType();

        assertEquals(new EventBuilder().build(), fromJavaNull);
        assertEquals(fromJavaNull, fromJsonNull);
    }

    @Test
    public void toModelType_nonTextDescription_rejected() {
        assertInvalid("{" + required() + ",\"description\":{}}");
    }

    //// helpers

    private static String required() {
        return "\"name\":\"Interview\",\"start\":\"" + VALID_START + "\",\"end\":\"" + VALID_END + "\"";
    }

    private static String json(String name, String start, String end) {
        return "{\"name\":\"" + name + "\",\"start\":\"" + start + "\",\"end\":\"" + end + "\"}";
    }

    private static Event load(String json) throws Exception {
        return JsonUtil.fromJsonString(json, JsonAdaptedEvent.class).toModelType();
    }

    private static Event reload(JsonAdaptedEvent adapted) throws Exception {
        return load(JsonUtil.toJsonString(adapted));
    }

    /**
     * Asserts that {@code json} is rejected, either while reading the JSON or while converting it to an event.
     */
    private static Exception assertInvalid(String json) {
        try {
            Event event = load(json);
            return fail("Expected rejection of " + json + " but loaded " + event);
        } catch (IOException | IllegalValueException e) {
            return e;
        } catch (Exception e) {
            return fail("Unexpected exception type for " + json + ": " + e);
        }
    }

    private static void assertMissing(String json, String field) {
        Exception e = assertInvalid(json);
        assertEquals(String.format(JsonAdaptedEvent.MISSING_FIELD_MESSAGE_FORMAT, field), e.getMessage());
    }
}
