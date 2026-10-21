package seedu.hirebase.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.commons.util.JsonUtil;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.testutil.TypicalPersons;

public class JsonSerializableHireBaseTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableHireBaseTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsHireBase.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonHireBase.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonHireBase.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableHireBase dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableHireBase.class).get();
        HireBase hireBaseFromFile = dataFromFile.toModelType();
        HireBase typicalPersonsHireBase = TypicalPersons.getTypicalHireBase();
        assertEquals(hireBaseFromFile, typicalPersonsHireBase);
    }

    @Test
    public void toModelType_fileWithoutCandidateIds_assignsIdsInFileOrder() throws Exception {
        JsonSerializableHireBase dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableHireBase.class).get();
        HireBase hireBase = dataFromFile.toModelType();

        List<Person> persons = hireBase.getPersonList();
        for (int i = 0; i < persons.size(); i++) {
            assertEquals(new CandidateId(i + 1), persons.get(i).getCandidateId().get());
        }
        assertEquals(persons.size() + 1, hireBase.getNextCandidateId());
    }

    @Test
    public void roundTrip_preservesIdsAndCounterAfterDeletion() throws Exception {
        HireBase original = TypicalPersons.getTypicalHireBase();
        // Delete the last candidate: the counter must still remember that its ID was used.
        original.removePerson(original.getPersonList().get(original.getPersonList().size() - 1));
        int expectedNextId = original.getNextCandidateId();

        String json = JsonUtil.toJsonString(new JsonSerializableHireBase(original));
        HireBase restored = JsonUtil.fromJsonString(json, JsonSerializableHireBase.class).toModelType();

        assertEquals(original, restored);
        assertEquals(expectedNextId, restored.getNextCandidateId());
        for (int i = 0; i < original.getPersonList().size(); i++) {
            assertEquals(original.getPersonList().get(i).getCandidateId(),
                    restored.getPersonList().get(i).getCandidateId());
        }
    }

    @Test
    public void toModelType_duplicateCandidateIds_throwsIllegalValueException() {
        Person first = TypicalPersons.ALICE.withCandidateId(new CandidateId(1));
        Person second = TypicalPersons.BENSON.withCandidateId(new CandidateId(1));
        JsonSerializableHireBase data = new JsonSerializableHireBase(
                List.of(new JsonAdaptedPerson(first), new JsonAdaptedPerson(second)), List.of(), null);

        assertThrows(IllegalValueException.class, JsonSerializableHireBase.MESSAGE_DUPLICATE_CANDIDATE_ID,
                data::toModelType);
    }

    @Test
    public void toModelType_invalidNextCandidateId_throwsIllegalValueException() {
        JsonSerializableHireBase data = new JsonSerializableHireBase(List.of(), List.of(), 0);

        assertThrows(IllegalValueException.class, JsonSerializableHireBase.MESSAGE_INVALID_NEXT_CANDIDATE_ID,
                data::toModelType);
    }

    @Test
    public void toModelType_storedCounterAboveExistingIds_isRespected() throws Exception {
        Person first = TypicalPersons.ALICE.withCandidateId(new CandidateId(2));
        JsonSerializableHireBase data = new JsonSerializableHireBase(
                List.of(new JsonAdaptedPerson(first)), List.of(), 9);

        HireBase hireBase = data.toModelType();

        assertEquals(9, hireBase.getNextCandidateId());
        assertTrue(hireBase.getPersonList().get(0).getCandidateId().isPresent());
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableHireBase dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableHireBase.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableHireBase dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableHireBase.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableHireBase.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

}
