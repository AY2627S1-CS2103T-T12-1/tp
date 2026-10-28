package seedu.hirebase.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 10, 27, 9, 5, 7);

    @TempDir
    public Path testFolder;

    @Test
    public void backUpInvalidFile_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                FileUtil.backUpInvalidFile(null, TIMESTAMP));
        assertThrows(NullPointerException.class, () ->
                FileUtil.backUpInvalidFile(testFolder.resolve("a.json"), null));
    }

    @Test
    public void backUpInvalidFile_validFile_copiesContentAndKeepsOriginal() throws IOException {
        Path file = testFolder.resolve("hirebase.json");
        Files.writeString(file, "not json");

        Path backup = FileUtil.backUpInvalidFile(file, TIMESTAMP);

        assertEquals(testFolder.resolve("hirebase-invalid-20261027-090507.json"), backup);
        assertEquals("not json", Files.readString(backup));
        assertTrue(Files.exists(file));
        assertEquals("not json", Files.readString(file));
    }

    @Test
    public void backUpInvalidFile_existingBackup_doesNotOverwrite() throws IOException {
        Path file = testFolder.resolve("hirebase.json");
        Files.writeString(file, "second");
        Path first = testFolder.resolve("hirebase-invalid-20261027-090507.json");
        Files.writeString(first, "first");

        Path backup = FileUtil.backUpInvalidFile(file, TIMESTAMP);

        assertEquals(testFolder.resolve("hirebase-invalid-20261027-090507-1.json"), backup);
        assertEquals("first", Files.readString(first));
        assertEquals("second", Files.readString(backup));

        Path third = FileUtil.backUpInvalidFile(file, TIMESTAMP);
        assertEquals(testFolder.resolve("hirebase-invalid-20261027-090507-2.json"), third);
    }

    @Test
    public void backUpInvalidFile_noExtension_appendsSuffixOnly() throws IOException {
        Path file = testFolder.resolve("hirebase");
        Files.writeString(file, "x");

        Path backup = FileUtil.backUpInvalidFile(file, TIMESTAMP);

        assertEquals(testFolder.resolve("hirebase-invalid-20261027-090507"), backup);
    }

    @Test
    public void tryBackUpInvalidFile_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.tryBackUpInvalidFile(null));
    }

    @Test
    public void tryBackUpInvalidFile_missingFile_returnsEmpty() {
        assertFalse(FileUtil.tryBackUpInvalidFile(testFolder.resolve("hirebase.json")).isPresent());
    }

    @Test
    public void tryBackUpInvalidFile_existingFile_copiesFile() throws IOException {
        Path file = testFolder.resolve("hirebase.json");
        Files.writeString(file, "bad");

        Path backup = FileUtil.tryBackUpInvalidFile(file).orElseThrow();

        assertEquals("bad", Files.readString(backup));
        assertTrue(backup.getFileName().toString().startsWith("hirebase-invalid-"));
        assertTrue(Files.exists(file));
    }

    @Test
    public void tryBackUpInvalidFile_copyFails_returnsEmpty() throws IOException {
        // the backup name would exceed the file system's file name length limit
        Path file = testFolder.resolve("a".repeat(250) + ".json");
        Files.writeString(file, "bad");

        assertFalse(FileUtil.tryBackUpInvalidFile(file).isPresent());
        assertTrue(Files.exists(file));
    }

}
