package seedu.hirebase.model.job;

import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.hirebase.commons.util.ToStringBuilder;

/**
 * An immutable job opening with validated, non-null fields.
 * Duplicate identity uses title and department without regard to case;
 * full equality includes the description and display capitalisation.
 */
public final class JobOpening {
    // Assigned on insertion. Like candidate IDs, this metadata is excluded from value equality.
    private final JobId jobId;
    private final JobTitle title;
    private final Department department;
    private final Description description;

    /**
     * Creates an opening with the supplied validated fields.
     */
    public JobOpening(JobTitle title, Department department, Description description) {
        this(title, department, description, null);
    }

    /**
     * Creates an opening with a stored ID, or null for an opening awaiting assignment.
     */
    public JobOpening(JobTitle title, Department department, Description description, JobId jobId) {
        requireAllNonNull(title, department, description);
        this.jobId = jobId;
        this.title = title;
        this.department = department;
        this.description = description;
    }

    public Optional<JobId> getJobId() {
        return Optional.ofNullable(jobId);
    }

    /**
     * Copies this opening's fields with the supplied ID.
     */
    public JobOpening withJobId(JobId id) {
        return new JobOpening(title, department, description, id);
    }

    public JobTitle getTitle() {
        return title;
    }

    public Department getDepartment() {
        return department;
    }

    public Description getDescription() {
        return description;
    }

    /**
     * Returns true when title and department match, regardless of description or case.
     */
    public boolean isSameJobOpening(JobOpening other) {
        return other == this || other != null
                && title.value.equalsIgnoreCase(other.title.value)
                && department.value.equalsIgnoreCase(other.department.value);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof JobOpening otherJob
                && title.equals(otherJob.title)
                && department.equals(otherJob.department)
                && description.equals(otherJob.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, department, description);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("jobId", jobId)
                .add("title", title)
                .add("department", department)
                .add("description", description)
                .toString();
    }
}
