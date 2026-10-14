package seedu.hirebase.model.job.exceptions;

/**
 * Signals an operation that would create duplicate job openings.
 */
public class DuplicateJobOpeningException extends RuntimeException {
    public DuplicateJobOpeningException() {
        super("Operation would result in duplicate job openings");
    }
}
