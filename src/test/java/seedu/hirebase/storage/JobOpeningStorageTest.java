package seedu.hirebase.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BOB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.commons.util.JsonUtil;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;

public class JobOpeningStorageTest {
    @TempDir
    public Path testFolder;

    private final JobOpening engineering = job("C++ Developer", "研发 & Engineering", "Build  services. 😀");
    private final JobOpening research = job("C++ Developer", "Research", "Build prototypes");

    @Test
    public void adapter_roundTrip_preservesAllFields() throws Exception {
        assertEquals(engineering, new JsonAdaptedJobOpening(engineering).toModelType());
        JsonAdaptedJobOpening adapted = JsonUtil.fromJsonString(
                JsonUtil.toJsonString(new JsonAdaptedJobOpening(engineering)), JsonAdaptedJobOpening.class);
        assertEquals(engineering, adapted.toModelType());
    }

    @Test
    public void adapter_normalizesLoadedValues() throws Exception {
        JsonAdaptedJobOpening adapted = new JsonAdaptedJobOpening(
                "  C++   Developer  ", "  研发 &  Engineering ", " Build  services. 😀 ");
        assertEquals(engineering, adapted.toModelType());
    }

    @Test
    public void adapter_missingFields_reportsField() {
        String[][] fields = {{null, "Engineering", "Build services"},
            {"Developer", null, "Build services"}, {"Developer", "Engineering", null}};
        String[] names = {"title", "department", "description"};
        for (int i = 0; i < fields.length; i++) {
            JsonAdaptedJobOpening adapted = new JsonAdaptedJobOpening(fields[i][0], fields[i][1], fields[i][2]);
            IllegalValueException error = assertThrows(IllegalValueException.class, adapted::toModelType);
            assertEquals(String.format(JsonAdaptedJobOpening.MISSING_FIELD_MESSAGE_FORMAT, names[i]),
                    error.getMessage());
        }
    }

    @Test
    public void adapter_invalidFields_reportsValidationMessage() {
        for (String invalid : List.of("", "---", "a".repeat(101), "Developer!")) {
            JsonAdaptedJobOpening title = new JsonAdaptedJobOpening(invalid, "Engineering", "Build services");
            assertEquals(JobTitle.MESSAGE_CONSTRAINTS,
                    assertThrows(IllegalValueException.class, title::toModelType).getMessage());
            JsonAdaptedJobOpening department = new JsonAdaptedJobOpening("Developer", invalid, "Build services");
            assertEquals(Department.MESSAGE_CONSTRAINTS,
                    assertThrows(IllegalValueException.class, department::toModelType).getMessage());
        }
        for (String invalid : List.of("", " ", "a".repeat(2001), "Build\nservices", "\tBuild")) {
            JsonAdaptedJobOpening description = new JsonAdaptedJobOpening("Developer", "Engineering", invalid);
            assertEquals(Description.MESSAGE_CONSTRAINTS,
                    assertThrows(IllegalValueException.class, description::toModelType).getMessage());
        }
    }

    @Test
    public void diskRoundTrip_preservesCandidatesJobsAndCreationOrder() throws Exception {
        Path file = testFolder.resolve("jobs.json");
        JsonHireBaseStorage storage = new JsonHireBaseStorage(file);
        HireBase original = new HireBase();
        original.addPerson(ALICE);
        original.addJobOpening(engineering);
        original.addJobOpening(research);
        storage.saveHireBase(original);
        ReadOnlyHireBase loaded = storage.readHireBase().orElseThrow();
        assertEquals(original, new HireBase(loaded));
        assertEquals(List.of(engineering, research), loaded.getJobOpeningList());
        assertTrue(Files.readString(file).contains("\"jobs\""));

        original.removeJobOpening(engineering);
        original.addPerson(BOB);
        storage.saveHireBase(original, file);
        assertEquals(original, new HireBase(storage.readHireBase(file).orElseThrow()));
        original.removeJobOpening(research);
        storage.saveHireBase(original);
        assertEquals(original, new HireBase(storage.readHireBase().orElseThrow()));
        assertTrue(storage.readHireBase().orElseThrow().getJobOpeningList().isEmpty());
    }

    @Test
    public void legacyFile_missingJobs_preservesCandidatesAfterResave() throws Exception {
        Path file = testFolder.resolve("legacy.json");
        Path fixture = Path.of("src/test/data/JsonSerializableHireBaseTest/typicalPersonsHireBase.json");
        Files.copy(fixture, file);
        JsonHireBaseStorage storage = new JsonHireBaseStorage(file);
        ReadOnlyHireBase loaded = storage.readHireBase().orElseThrow();
        assertTrue(loaded.getJobOpeningList().isEmpty());
        assertFalse(loaded.getPersonList().isEmpty());
        storage.saveHireBase(loaded);
        assertEquals(new HireBase(loaded), new HireBase(storage.readHireBase().orElseThrow()));
    }

    @Test
    public void emptyAndNullJobs_loadAsEmptyList() throws Exception {
        for (String json : List.of("{\"persons\":[]}", "{\"persons\":[],\"jobs\":[]}",
                "{\"persons\":[],\"jobs\":null}")) {
            JsonSerializableHireBase adapted = JsonUtil.fromJsonString(json, JsonSerializableHireBase.class);
            assertEquals(new HireBase(), adapted.toModelType());
        }
    }

    @Test
    public void snapshot_laterSourceChangesDoNotAffectSavedData() throws Exception {
        HireBase source = new HireBase();
        source.addPerson(ALICE);
        source.addJobOpening(engineering);
        JsonSerializableHireBase snapshot = new JsonSerializableHireBase(source);
        source.removePerson(ALICE);
        source.removeJobOpening(engineering);
        source.addJobOpening(research);
        HireBase restored = snapshot.toModelType();
        assertEquals(List.of(ALICE), restored.getPersonList());
        assertEquals(List.of(engineering), restored.getJobOpeningList());
    }

    @Test
    public void duplicateJobs_normalizedIdentity_rejectsWholeFile() throws Exception {
        String first = JsonUtil.toJsonString(new JsonAdaptedJobOpening(engineering));
        String duplicate = JsonUtil.toJsonString(new JsonAdaptedJobOpening(
                " c++  developer ", "研发 & ENGINEERING", "Different description"));
        String json = "{\"persons\":[],\"jobs\":[" + first + "," + duplicate + "]}";
        JsonSerializableHireBase adapted = JsonUtil.fromJsonString(json, JsonSerializableHireBase.class);
        assertEquals(JsonSerializableHireBase.MESSAGE_DUPLICATE_JOB,
                assertThrows(IllegalValueException.class, adapted::toModelType).getMessage());
        assertInvalidFile(json);
    }

    @Test
    public void invalidJobEntries_rejectWholeFileAndDoNotRewriteIt() throws Exception {
        String valid = JsonUtil.toJsonString(new JsonAdaptedJobOpening(engineering));
        for (String invalid : List.of("null", "{}", "{\"title\":\"Developer\",\"department\":\"Engineering\"}",
                "{\"title\":\"\",\"department\":\"Engineering\",\"description\":\"Work\"}",
                "{\"title\":\"Developer\",\"department\":\"Engineering\",\"description\":\"Line\\nBreak\"}")) {
            assertInvalidFile("{\"persons\":[],\"jobs\":[" + valid + "," + invalid + "]}");
        }
        assertInvalidFile("{\"persons\":[],\"jobs\":{}}");
        assertInvalidFile("{\"persons\":[],\"jobs\":[42]}");
    }

    private void assertInvalidFile(String json) throws Exception {
        Path file = testFolder.resolve("invalid.json");
        Files.writeString(file, json);
        JsonHireBaseStorage storage = new JsonHireBaseStorage(file);
        assertThrows(DataLoadingException.class, storage::readHireBase);
        assertEquals(json, Files.readString(file));
    }

    private static JobOpening job(String title, String department, String description) {
        return new JobOpening(new JobTitle(title), new Department(department), new Description(description));
    }
}
