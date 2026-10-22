package seedu.hirebase.model.job;

import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.model.candidate.Skill;

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
    private final Set<Skill> skills;

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
        this(title, department, description, jobId, Set.of());
    }

    /**
     * Creates an opening with an immutable snapshot of its required skills.
     */
    public JobOpening(JobTitle title, Department department, Description description, JobId jobId, Set<Skill> skills) {
        requireAllNonNull(title, department, description, skills);
        this.skills = Set.copyOf(skills);
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
        return new JobOpening(title, department, description, id, skills);
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    /**
     * Formats required skills consistently for cards and command results.
     */
    public String getSkillsText() {
        return skills.isEmpty() ? "None" : skills.stream().map(skill -> skill.value)
                .sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(", "));
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
                && description.equals(otherJob.description)
                && skills.equals(otherJob.skills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, department, description, skills);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("jobId", jobId)
                .add("title", title)
                .add("department", department)
                .add("description", description)
                .add("skills", skills)
                .toString();
    }
}
