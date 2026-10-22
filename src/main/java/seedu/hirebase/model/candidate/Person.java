package seedu.hirebase.model.candidate;

import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    private final Set<Skill> skills;
    private final Role role;

    // Data fields
    private final Address address;
    private final Note note;
    private final Set<Tag> tags = new HashSet<>();
    private final InterviewStage interviewStage;
    // Assigned by the model when the candidate is added; null until then. Not part of equals/hashCode.
    private final CandidateId candidateId;

    /**
     * Creates a candidate with recruitment details and an empty note.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Set<Skill> skills, Role role) {
        this(name, phone, email, address, new Note(""), tags, skills, role);
    }

    /**
     * Creates a candidate with optional recruitment details.
     * Null skills are stored as an empty set.
     */
    public Person(Name name, Phone phone, Email email, Address address, Note note, Set<Tag> tags,
                  Set<Skill> skills, Role role) {
        requireAllNonNull(name, phone, email, address, note, tags);
        this(name, phone, email, address, note, tags, skills, role,
                InterviewStage.SCREENING);
    }

    /**
     * Creates a candidate with optional recruitment details.
     * Null skills are stored as an empty set.
     */
    public Person(Name name, Phone phone, Email email, Address address, Note note, Set<Tag> tags,
            Set<Skill> skills, Role role, InterviewStage interviewStage) {
        this(name, phone, email, address, note, tags, skills, role, interviewStage, null);
    }

    /**
     * Creates a candidate that may already have a {@code candidateId}.
     * A null {@code candidateId} means that the model has not assigned one yet.
     */
    public Person(Name name, Phone phone, Email email, Address address, Note note, Set<Tag> tags,
            Set<Skill> skills, Role role, InterviewStage interviewStage, CandidateId candidateId) {
        requireAllNonNull(name, phone, email, address, note, tags, interviewStage);
        this.skills = skills == null ? Collections.emptySet() : Set.copyOf(skills);
        this.role = role;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.note = note;
        this.tags.addAll(tags);
        this.interviewStage = interviewStage;
        this.candidateId = candidateId;
    }

    /**
     * Returns the permanent candidate ID, or empty if the model has not assigned one yet.
     */
    public Optional<CandidateId> getCandidateId() {
        return Optional.ofNullable(candidateId);
    }

    /**
     * Returns a copy of this candidate with the given {@code candidateId}.
     */
    public Person withCandidateId(CandidateId candidateId) {
        return new Person(name, phone, email, address, note, tags, skills, role, interviewStage, candidateId);
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public Optional<Role> getRole() {
        return Optional.ofNullable(role);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public Note getNote() {
        return note;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public InterviewStage getInterviewStage() {
        return interviewStage;
    }

    /**
     * Returns true if both persons have the same email (case-insensitive).
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
            && otherPerson.getEmail().value.equalsIgnoreCase(getEmail().value);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && note.equals(otherPerson.note)
                && tags.equals(otherPerson.tags)
                && skills.equals(otherPerson.skills)
                && Objects.equals(role, otherPerson.role)
                && interviewStage.equals(otherPerson.interviewStage);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, note, tags, skills, role);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("note", note)
                .add("tags", tags)
                .add("skills", skills)
                .add("role", role)
                .add("interviewStage", interviewStage)
                .add("candidateId", candidateId)
                .toString();
    }

}
