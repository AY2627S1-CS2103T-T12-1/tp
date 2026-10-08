package seedu.hirebase.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.model.candidate.Address;
import seedu.hirebase.model.candidate.Email;
import seedu.hirebase.model.candidate.Name;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.Phone;
import seedu.hirebase.model.candidate.Remark;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String remark;
    private final List<String> skills;
    private final String role;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     * Only the full constructor has JSON annotations so deserialization has one unambiguous creator.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String remark,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, remark, tags, null, null);
    }

    /**
     * Reads recruitment details while accepting older files without those fields.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("remark") String remark, @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("skills") List<String> skills,
            @JsonProperty("role") String role) {
        this.skills = skills == null ? new ArrayList<>() : new ArrayList<>(skills);
        this.role = role;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        skills = source.getSkills().stream().map(skill -> skill.value).sorted().collect(Collectors.toList());
        role = source.getRole().map(value -> value.value).orElse(null);
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        remark = source.getRemark().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's
     * {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in
     *                               the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        // Older address books have no remark field; retain those people with an empty remark.
        final Remark modelRemark = new Remark(remark == null ? "" : remark);
        final Set<Tag> modelTags = new HashSet<>(personTags);
        Set<Skill> modelSkills = new HashSet<>();
        for (String skill : skills) {
            if (skill == null || !Skill.isValidSkill(skill)) {
                throw new IllegalValueException(Skill.MESSAGE_CONSTRAINTS);
            }
            modelSkills.add(new Skill(skill));
        }
        if (role != null && !Role.isValidRole(role)) {
            throw new IllegalValueException(Role.MESSAGE_CONSTRAINTS);
        }
        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelRemark, modelTags,
                modelSkills, role == null ? null : new Role(role));
    }

}
