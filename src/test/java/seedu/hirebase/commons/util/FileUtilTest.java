package seedu.hirebase.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
