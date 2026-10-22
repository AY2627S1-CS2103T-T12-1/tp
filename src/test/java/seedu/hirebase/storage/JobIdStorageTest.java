package seedu.hirebase.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;

public class JobIdStorageTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void legacyAndMixedFiles_assignMissingIdsAboveStoredIdsAndCounter() throws Exception {
        HireBase legacy = load("{\"persons\":[],\"jobs\":[" + jsonJob("First", "") + ","
                + jsonJob("Second", "") + "]}");
        assertIds(legacy, 1, 2);
        HireBase mixed = load("{\"persons\":[],\"nextJobId\":10,\"jobs\":[" + jsonJob("First", "") + ","
                + jsonJob("Second", "\"id\":7,") + "," + jsonJob("Third", "\"id\":null,") + "]}");
        assertIds(mixed, 10, 7, 11);
        assertEquals(12, mixed.getNextJobId());
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("roundtrip.json"));
        storage.saveHireBase(mixed);
        assertIds(new HireBase(storage.readHireBase().orElseThrow()), 10, 7, 11);
        HireBase staleCounter = load("{\"persons\":[],\"nextJobId\":2,\"jobs\":["
                + jsonJob("First", "\"id\":7,") + "]}");
        assertEquals(8, staleCounter.getNextJobId());
    }

    @Test
    public void deleteHighestAndClear_roundTripNeverReusesRetiredIds() throws Exception {
        HireBase data = load("{\"persons\":[],\"jobs\":[" + jsonJob("First", "\"id\":7,") + "]}");
        data.removeJobOpening(data.getJobOpeningList().getFirst());
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("saved.json"));
        storage.saveHireBase(data);
        HireBase restored = new HireBase(storage.readHireBase().orElseThrow());
        restored.addJobOpening(job("New"));
        assertIds(restored, 8);
        restored.resetData(new HireBase());
        storage.saveHireBase(restored);
        restored = new HireBase(storage.readHireBase().orElseThrow());
        restored.addJobOpening(job("After clear"));
        assertIds(restored, 9);
    }

    @Test
    public void malformedOrDuplicateIds_rejectWholeFileWithoutRewriting() throws Exception {
        for (String id : List.of("0", "-1", "2147483648", "1.5", "\"7\"", "true", "{}")) {
            assertInvalid("{\"persons\":[],\"jobs\":[" + jsonJob("First", "\"id\":" + id + ",") + "]}");
        }
        assertInvalid("{\"persons\":[],\"jobs\":[" + jsonJob("First", "\"id\":7,") + ","
                + jsonJob("Second", "\"id\":7,") + "]}");
        for (String counter : List.of("0", "-1", "2147483649", "1.5", "\"7\"", "true")) {
            assertInvalid("{\"persons\":[],\"jobs\":[],\"nextJobId\":" + counter + "}");
        }
    }

    @Test
    public void integersOutsideLongRange_rejectWholeFileWithoutRewriting() throws Exception {
        for (String overflow : List.of("9223372036854775808", "-9223372036854775809")) {
            assertInvalid("{\"persons\":[],\"jobs\":[" + jsonJob("First", "\"id\":" + overflow + ",") + "]}");
            assertInvalid("{\"persons\":[],\"jobs\":[],\"nextJobId\":" + overflow + "}");
        }
    }

    @Test
    public void exhaustedCounter_loadsButCannotAssignNewIds() throws Exception {
        HireBase data = load("{\"persons\":[],\"nextJobId\":2147483648,\"jobs\":["
                + jsonJob("Last", "\"id\":2147483647,") + "]}");
        assertIds(data, Integer.MAX_VALUE);
        assertThrows(IllegalStateException.class, () -> data.addJobOpening(job("Overflow")));
        assertInvalid("{\"persons\":[],\"nextJobId\":2147483648,\"jobs\":[" + jsonJob("Legacy", "") + "]}");
    }

    private HireBase load(String json) throws Exception {
        Path file = temporaryFolder.resolve("load.json");
        Files.writeString(file, json);
        HireBase loaded = new HireBase(new JsonHireBaseStorage(file).readHireBase().orElseThrow());
        assertEquals(json, Files.readString(file));
        return loaded;
    }

    private void assertInvalid(String json) throws Exception {
        Path file = temporaryFolder.resolve("invalid.json");
        Files.writeString(file, json);
        assertThrows(DataLoadingException.class, () -> new JsonHireBaseStorage(file).readHireBase());
        assertEquals(json, Files.readString(file));
    }

    private static void assertIds(HireBase data, Integer... expected) {
        assertEquals(List.of(expected), data.getJobOpeningList().stream()
                .map(job -> job.getJobId().orElseThrow().getValue()).toList());
    }

    private static String jsonJob(String title, String idField) {
        return "{" + idField + "\"title\":\"" + title + "\",\"department\":\"Engineering\",\"description\":\"Build\"}";
    }

    private static JobOpening job(String title) {
        return new JobOpening(new JobTitle(title), new Department("Engineering"), new Description("Build"));
    }
}
