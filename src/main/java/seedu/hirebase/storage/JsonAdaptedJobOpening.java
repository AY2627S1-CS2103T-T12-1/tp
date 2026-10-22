package seedu.hirebase.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;

/**
 * Jackson-friendly representation of a job opening.
 */
class JsonAdaptedJobOpening {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Job opening's %s field is missing!";

    private final Long id;
    private final String title;
    private final String department;
    private final String description;

    /**
     * Reads the stored fields, deferring validation until conversion to the model.
     */
    public JsonAdaptedJobOpening(String title, String department, String description) {
        this(title, department, description, null);
    }

    /**
     * Accepts legacy openings without IDs and rejects non-integer IDs.
     */
    @JsonCreator
    public JsonAdaptedJobOpening(@JsonProperty("title") String title,
            @JsonProperty("department") String department, @JsonProperty("description") String description,
            @JsonProperty("id") JsonNode id) {
        if (id != null && !id.isNull() && (!id.isIntegralNumber() || !id.canConvertToLong())) {
            throw new IllegalArgumentException(JobId.MESSAGE_CONSTRAINTS);
        }
        this.id = id == null || id.isNull() ? null : id.longValue();
        this.title = title;
        this.department = department;
        this.description = description;
    }

    /**
     * Copies the opening's display values for serialization.
     */
    public JsonAdaptedJobOpening(JobOpening source) {
        id = source.getJobId().map(value -> (long) value.getValue()).orElse(null);
        title = source.getTitle().value;
        department = source.getDepartment().value;
        description = source.getDescription().value;
    }

    /**
     * Validates every field before constructing an opening.
     *
     * @throws IllegalValueException if a required field is absent or invalid.
     */
    public JobOpening toModelType() throws IllegalValueException {
        if (id != null && (id < 1 || id > Integer.MAX_VALUE)) {
            throw new IllegalValueException(JobId.MESSAGE_CONSTRAINTS);
        }
        if (title == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "title"));
        }
        if (!JobTitle.isValidJobTitle(title)) {
            throw new IllegalValueException(JobTitle.MESSAGE_CONSTRAINTS);
        }
        if (department == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "department"));
        }
        if (!Department.isValidDepartment(department)) {
            throw new IllegalValueException(Department.MESSAGE_CONSTRAINTS);
        }
        if (description == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "description"));
        }
        if (!Description.isValidDescription(description)) {
            throw new IllegalValueException(Description.MESSAGE_CONSTRAINTS);
        }
        return new JobOpening(new JobTitle(title), new Department(department), new Description(description),
                id == null ? null : new JobId(id.intValue()));
    }
}
