package seedu.hirebase.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.model.candidate.Skill;

public class JobSkillsStorageTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void legacyMissingNullAndEmptySkills_loadAsEmpty() throws Exception {
        for (String field : List.of("", ",\"skills\":null", ",\"skills\":[]")) {
            JsonHireBaseStorage storage = storage(field);
            assertTrue(storage.readHireBase().orElseThrow().getJobOpeningList().getFirst().getSkills().isEmpty());
        }
    }

    @Test
    public void storedSkills_normalizeDeduplicateAndRoundTrip() throws Exception {
        JsonHireBaseStorage storage = storage(",\"skills\":[\" Java \",\"java\",\"Machine   Learning\"]");
        var loaded = storage.readHireBase().orElseThrow();
        assertEquals(Set.of(new Skill("Java"), new Skill("Machine Learning")),
                loaded.getJobOpeningList().getFirst().getSkills());
        storage.saveHireBase(loaded);
        assertEquals(loaded.getJobOpeningList(), storage.readHireBase().orElseThrow().getJobOpeningList());
    }

    @Test
    public void malformedSkills_rejectWholeFileWithoutRewriting() throws Exception {
        for (String value : List.of("{}", "\"Java\"", "[null]", "[42]", "[true]", "[\"\"]", "[\"bad!\"]",
                "[\"" + "a".repeat(31) + "\"]")) {
            JsonHireBaseStorage storage = storage(",\"skills\":" + value);
            String before = Files.readString(storage.getHireBaseFilePath());
            assertThrows(DataLoadingException.class, storage::readHireBase);
            assertEquals(before, Files.readString(storage.getHireBaseFilePath()));
        }
    }

    private JsonHireBaseStorage storage(String skillsField) throws Exception {
        Path file = temporaryFolder.resolve("jobs.json");
        Files.writeString(file, "{\"persons\":[],\"jobs\":[{\"id\":7,\"title\":\"Engineer\","
                + "\"department\":\"Engineering\",\"description\":\"Build\"" + skillsField + "}]}");
        return new JsonHireBaseStorage(file);
    }
}
