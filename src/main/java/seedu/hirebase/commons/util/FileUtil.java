package seedu.hirebase.commons.util;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes given string to a file.
     * Will create the file if it does not exist yet.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(CHARSET));
    }

    /**
     * Copies {@code file} next to itself under a new name that includes {@code timestamp}, for example
     * {@code hirebase.json} becomes {@code hirebase-invalid-20261027-183000.json}. A number is appended if that
     * name is already taken, so an earlier backup is never overwritten. The original file is left in place.
     *
     * @return the path of the backup copy.
     * @throws IOException if the file cannot be copied.
     */
    public static Path backUpInvalidFile(Path file, LocalDateTime timestamp) throws IOException {
        requireNonNull(file);
        requireNonNull(timestamp);

        String fileName = file.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot > 0 ? fileName.substring(0, dot) : fileName;
        String extension = dot > 0 ? fileName.substring(dot) : "";
        String stamp = timestamp.format(BACKUP_TIMESTAMP_FORMAT);

        Path backup = file.resolveSibling(baseName + "-invalid-" + stamp + extension);
        for (int copy = 1; Files.exists(backup); copy++) {
            backup = file.resolveSibling(baseName + "-invalid-" + stamp + "-" + copy + extension);
        }
        Files.copy(file, backup);
        return backup;
    }

}
