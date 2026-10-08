package seedu.hirebase.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.candidate.Person;

/**
 * An Immutable HireBase that is serializable to JSON format.
 */
@JsonRootName(value = "hirebase")
class JsonSerializableHireBase {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableHireBase} with the given persons.
     */
    @JsonCreator
    public JsonSerializableHireBase(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        this.persons.addAll(persons);
    }

    /**
     * Converts a given {@code ReadOnlyHireBase} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created
     *               {@code JsonSerializableHireBase}.
     */
    public JsonSerializableHireBase(ReadOnlyHireBase source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code HireBase} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public HireBase toModelType() throws IllegalValueException {
        HireBase hireBase = new HireBase();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (hireBase.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            hireBase.addPerson(person);
        }
        return hireBase;
    }

}
