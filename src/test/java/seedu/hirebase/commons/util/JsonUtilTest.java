package seedu.hirebase.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.testutil.SerializableTestClass;
import seedu.hirebase.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void fromJsonString_trailingContent_throwsIoException() {
        String json = "{\"name\":\"Alice\"}";
        for (String suffix : new String[] {"{}", "[]", "true", "42", "\"extra\"", "invalid"}) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json + suffix, JsonNode.class), suffix);
        }
    }

    @Test
    public void fromJsonString_duplicateFields_throwsIoException() {
        for (String json : new String[] {
            "{\"name\":\"Alice\",\"name\":\"Bob\"}",
            "{\"profile\":{\"name\":\"Alice\",\"name\":\"Bob\"}}"}) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, JsonNode.class), json);
        }
    }

    @Test
    public void fromJsonString_trailingWhitespace_readsCompleteValue() throws IOException {
        SerializableTestClass actual = JsonUtil.fromJsonString(
                SerializableTestClass.JSON_STRING_REPRESENTATION + " \t\r\n", SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), actual.getName());
        assertEquals(SerializableTestClass.getListTestValues(), actual.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), actual.getMapOfIntegerToString());
    }

    @Test
    public void readJsonFile_invalidDocument_preservesOriginalBytes(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("invalid.json");
        for (String json : new String[] {"{\"name\":\"Alice\"} {}", "{\"name\":\"Alice\",\"name\":\"Bob\"}"}) {
            Files.writeString(file, json);

            assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, JsonNode.class));
            assertEquals(json, Files.readString(file));
        }
    }
}
