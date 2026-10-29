package seedu.hirebase.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.testutil.EventBuilder;

public class EventStorageTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void load_fileWithoutEventsField_hasNoEvents() throws Exception {
        HireBase data = load("{\"persons\":[],\"jobs\":[]}");

        assertEquals(List.of(), data.getEventList());
        assertEquals(1, data.getNextEventId());
    }

    @Test
    public void load_nullEvents_hasNoEvents() throws Exception {
        HireBase data = load("{\"persons\":[],\"events\":null,\"nextEventId\":null}");

        assertEquals(List.of(), data.getEventList());
        assertEquals(1, data.getNextEventId());
    }

    @Test
    public void load_mixedIds_assignsMissingIdsAboveStoredIdsAndCounter() throws Exception {
        HireBase data = load("{\"persons\":[],\"nextEventId\":10,\"events\":[" + jsonEvent("A", "") + ","
                + jsonEvent("B", "\"id\":7,") + "," + jsonEvent("C", "\"id\":null,") + "]}");

        assertIds(data, 10, 7, 11);
        assertEquals(12, data.getNextEventId());
    }

    @Test
    public void load_staleCounterBelowStoredId_movesCounterPastIt() throws Exception {
        HireBase data = load("{\"persons\":[],\"nextEventId\":2,\"events\":[" + jsonEvent("A", "\"id\":7,") + "]}");

        assertEquals(8, data.getNextEventId());
    }

    @Test
    public void roundTrip_preservesEveryEventFieldAndCounter() throws Exception {
        HireBase data = new HireBase();
        data.addEvent(new EventBuilder().withName("Kept").withDescription("Room 3").withCandidateIds(2, 4)
                .withTimes(LocalDateTime.of(2026, 12, 31, 23, 0), LocalDateTime.of(2026, 12, 31, 23, 59))
                .build());
        Event deleted = data.addEvent(new EventBuilder().withName("Deleted").build());
        data.addEvent(new EventBuilder().withName("Cancelled").withCancelled(true).build());
        data.removeEvent(deleted);

        HireBase restored = saveAndReload(data);

        assertEquals(data.getEventList(), restored.getEventList());
        assertEquals(4, restored.getNextEventId());
    }

    @Test
    public void roundTrip_afterDeletingHighestAndClearing_neverReusesIds() throws Exception {
        HireBase data = new HireBase();
        data.addEvent(new EventBuilder().build());
        data.removeEvent(data.getEventList().get(0));

        HireBase restored = saveAndReload(data);
        assertEquals(new EventBuilder().withEventId(2).build().getEventId(),
                restored.addEvent(new EventBuilder().build()).getEventId());

        restored.resetData(new HireBase());
        HireBase cleared = saveAndReload(restored);
        assertEquals(new EventBuilder().withEventId(3).build().getEventId(),
                cleared.addEvent(new EventBuilder().build()).getEventId());
    }

    @Test
    public void load_idsWithoutCounter_continuesAfterLargestStoredId() throws Exception {
        HireBase data = load("{\"persons\":[],\"events\":[" + jsonEvent("A", "\"id\":4,") + ","
                + jsonEvent("B", "\"id\":9,") + "," + jsonEvent("C", "") + "]}");

        assertIds(data, 4, 9, 10);
        assertEquals(11, data.getNextEventId());
    }

    @Test
    public void roundTrip_unicodeAndSpecialCharacters_preservedThroughFile() throws Exception {
        HireBase data = new HireBase();
        data.addEvent(new EventBuilder().withName("面试 · O'Brien \"Ali\" \\ ✓ émigré 🎉")
                .withDescription("Line 1\nLine 2\tTabbed, {braces} [brackets] <tags> & 100%").build());

        HireBase restored = saveAndReload(data);

        assertEquals(data.getEventList(), restored.getEventList());
    }

    @Test
    public void roundTrip_candidateIdWithNoCandidate_isKept() throws Exception {
        // A deleted candidate stays referenced by their events, so viewing the event can show them as deleted.
        HireBase data = load("{\"persons\":[],\"events\":[{\"name\":\"Interview\",\"start\":\"2026-11-05T14:00\","
                + "\"end\":\"2026-11-05T15:00\",\"candidateIds\":[99]}]}");

        HireBase restored = saveAndReload(data);

        assertEquals(List.of(new CandidateId(99)), restored.getEventList().get(0).getCandidateIds());
    }

    @Test
    public void load_invalidEventsShape_rejectsWholeFileWithoutRewriting() throws Exception {
        assertInvalid("{\"persons\":[],\"events\":[null]}");
        assertInvalid("{\"persons\":[],\"events\":{}}");
        assertInvalid("{\"persons\":[],\"events\":\"none\"}");
        // One bad event among good ones still rejects the whole file; nothing is silently dropped.
        assertInvalid("{\"persons\":[],\"events\":[" + jsonEvent("Good", "") + ",{\"name\":\"Bad\"}]}");
    }

    @Test
    public void load_duplicateEventIds_rejectsWholeFileWithoutRewriting() throws Exception {
        assertInvalid("{\"persons\":[],\"events\":[" + jsonEvent("A", "\"id\":7,") + ","
                + jsonEvent("B", "\"id\":7,") + "]}");
    }

    @Test
    public void load_invalidCounter_rejectsWholeFileWithoutRewriting() throws Exception {
        for (String counter : List.of("0", "-1", "2147483649", "9223372036854775808", "1.5", "\"7\"", "true",
                "{}")) {
            assertInvalid("{\"persons\":[],\"events\":[],\"nextEventId\":" + counter + "}");
        }
    }

    @Test
    public void load_exhaustedCounter_loadsStoredIdsButRejectsUnnumberedEvent() throws Exception {
        HireBase data = load("{\"persons\":[],\"nextEventId\":2147483648,\"events\":["
                + jsonEvent("Last", "\"id\":2147483647,") + "]}");
        assertIds(data, Integer.MAX_VALUE);

        assertInvalid("{\"persons\":[],\"nextEventId\":2147483648,\"events\":[" + jsonEvent("Legacy", "") + "]}");
    }

    private HireBase load(String json) throws Exception {
        Path file = temporaryFolder.resolve("load.json");
        Files.writeString(file, json);
        HireBase loaded = new HireBase(new JsonHireBaseStorage(file).readHireBase().orElseThrow());
        assertEquals(json, Files.readString(file));
        return loaded;
    }

    private HireBase saveAndReload(HireBase data) throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("saved.json"));
        storage.saveHireBase(data);
        return new HireBase(storage.readHireBase().orElseThrow());
    }

    private void assertInvalid(String json) throws Exception {
        Path file = temporaryFolder.resolve("invalid.json");
        Files.writeString(file, json);
        assertThrows(DataLoadingException.class, () -> new JsonHireBaseStorage(file).readHireBase(), json);
        assertEquals(json, Files.readString(file));
    }

    private static void assertIds(HireBase data, Integer... expected) {
        assertEquals(List.of(expected), data.getEventList().stream()
                .map(event -> event.getEventId().orElseThrow().getValue()).toList());
    }

    private static String jsonEvent(String name, String idField) {
        return "{" + idField + "\"name\":\"" + name
                + "\",\"start\":\"2026-11-05T14:00\",\"end\":\"2026-11-05T15:00\"}";
    }
}
