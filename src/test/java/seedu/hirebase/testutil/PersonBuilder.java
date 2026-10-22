package seedu.hirebase.testutil;

import java.util.HashSet;
import java.util.Set;

import seedu.hirebase.model.candidate.Address;
import seedu.hirebase.model.candidate.Email;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Name;
import seedu.hirebase.model.candidate.Note;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.Phone;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.tag.Tag;
import seedu.hirebase.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";
    public static final String DEFAULT_NOTE = "";

    private Name name;
    private Phone phone;
    private Email email;
    private Address address;
    private Note note;
    private Set<Tag> tags;
    private Set<Skill> skills = new HashSet<>();
    private Role role;
    private InterviewStage interviewStage;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        note = new Note(DEFAULT_NOTE);
        tags = new HashSet<>();
        interviewStage = InterviewStage.SCREENING;
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        skills = new HashSet<>(personToCopy.getSkills());
        role = personToCopy.getRole().orElse(null);
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        note = personToCopy.getNote();
        tags = new HashSet<>(personToCopy.getTags());
        interviewStage = personToCopy.getInterviewStage();
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the
     * {@code Person} that we are building.
     */
    public PersonBuilder withTags(String... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the replacement technical skills.
     */
    public PersonBuilder withSkills(String... values) {
        skills = new HashSet<>();
        for (String value : values) {
            skills.add(new Skill(value));
        }
        return this;
    }

    /**
     * Sets the target role.
     */
    public PersonBuilder withRole(String value) {
        role = new Role(value);
        return this;
    }

    public Person build() {
        return new Person(name, phone, email, address, note, tags, skills, role, interviewStage);
    }

    /**
     * Sets the {@code Note} of the {@code Person} that we are building.
     */
    public PersonBuilder withNote(String note) {
        this.note = new Note(note);
        return this;
    }

    /**
     * Sets the {@code InterviewStage} of the {@code Person} that we are building.
     */
    public PersonBuilder withInterviewStage(InterviewStage interviewStage) {
        this.interviewStage = interviewStage;
        return this;
    }
}
