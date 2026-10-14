package seedu.hirebase.model.job.exceptions;

/**
 * Signals an operation targeting a job opening absent from the list.
 */
public class JobOpeningNotFoundException extends RuntimeException {
    public JobOpeningNotFoundException() {
        super("Job opening was not found");
    }
}
