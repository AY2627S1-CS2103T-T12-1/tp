package seedu.hirebase.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.exceptions.DuplicatePersonException;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.testutil.PersonBuilder;



public class HireBaseTest {

    private final HireBase addressBook = new HireBase();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
    }

    @Test
    public void addPerson_withoutId_assignsIncreasingIds() {
        addressBook.addPerson(ALICE);
        addressBook.addPerson(new PersonBuilder(ALICE).withEmail("second@example.com").build());

        assertEquals(new CandidateId(1), addressBook.getPersonList().get(0).getCandidateId().get());
        assertEquals(new CandidateId(2), addressBook.getPersonList().get(1).getCandidateId().get());
        assertEquals(3, addressBook.getNextCandidateId());
    }

    @Test
    public void addPerson_afterDeletingLatestCandidate_doesNotReuseId() {
        addressBook.addPerson(ALICE);
        Person second = new PersonBuilder(ALICE).withEmail("second@example.com").build();
        addressBook.addPerson(second);
        addressBook.removePerson(addressBook.getPersonList().get(1));

        Person third = new PersonBuilder(ALICE).withEmail("third@example.com").build();
        addressBook.addPerson(third);

        assertEquals(new CandidateId(3), addressBook.getPersonList().get(1).getCandidateId().get());
    }

    @Test
    public void addPerson_withExistingId_keepsIdAndAdvancesCounter() {
        addressBook.addPerson(ALICE.withCandidateId(new CandidateId(7)));

        assertEquals(new CandidateId(7), addressBook.getPersonList().get(0).getCandidateId().get());
        assertEquals(8, addressBook.getNextCandidateId());
    }

    @Test
    public void setPerson_editedPersonWithoutId_keepsTargetId() {
        addressBook.addPerson(ALICE);
        Person target = addressBook.getPersonList().get(0);
        Person edited = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();

        addressBook.setPerson(target, edited);

        assertEquals(target.getCandidateId(), addressBook.getPersonList().get(0).getCandidateId());
        assertEquals(VALID_ADDRESS_BOB, addressBook.getPersonList().get(0).getAddress().value);
    }

    @Test
    public void resetData_personsWithoutIds_assignsIdsInOrder() {
        addressBook.resetData(getTypicalHireBase());

        List<Person> persons = addressBook.getPersonList();
        for (int i = 0; i < persons.size(); i++) {
            assertEquals(new CandidateId(i + 1), persons.get(i).getCandidateId().get());
        }
        assertEquals(persons.size() + 1, addressBook.getNextCandidateId());
    }

    @Test
    public void resetData_doesNotMoveCounterBackwards() {
        addressBook.addPerson(ALICE);
        addressBook.addPerson(new PersonBuilder(ALICE).withEmail("second@example.com").build());

        addressBook.resetData(new HireBase());

        assertEquals(3, addressBook.getNextCandidateId());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyHireBase_replacesData() {
        HireBase newData = getTypicalHireBase();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        HireBaseStub newData = new HireBaseStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInHireBase_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInHireBase_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInHireBase_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonByCandidateId_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.getPersonByCandidateId(null));
    }

    @Test
    public void getPersonByCandidateId_idNotPresent_returnsEmpty() {
        addressBook.addPerson(ALICE); // assigned ID 1
        assertTrue(addressBook.getPersonByCandidateId(new CandidateId(2)).isEmpty());
    }

    @Test
    public void getPersonByCandidateId_idPresent_returnsPerson() {
        addressBook.addPerson(ALICE); // assigned ID 1
        assertEquals(addressBook.getPersonList().get(0),
                addressBook.getPersonByCandidateId(new CandidateId(1)).orElseThrow());
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = HireBase.class.getCanonicalName() + "{persons=" + addressBook.getPersonList()
                + ", jobOpenings=" + addressBook.getJobOpeningList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    @Test
    public void equals() {
        HireBase typicalHireBase = getTypicalHireBase();
        HireBase sameHireBase = getTypicalHireBase();
        HireBase emptyHireBase = new HireBase();

        // same object -> returns true
        assertTrue(typicalHireBase.equals(typicalHireBase));

        // same persons -> returns true, and hash codes match
        assertTrue(typicalHireBase.equals(sameHireBase));
        assertEquals(typicalHireBase.hashCode(), sameHireBase.hashCode());

        // null -> returns false
        assertFalse(typicalHireBase.equals(null));

        // different type -> returns false
        assertFalse(typicalHireBase.equals(5));

        // different persons -> returns false
        assertFalse(typicalHireBase.equals(emptyHireBase));
    }

    /**
     * A stub ReadOnlyHireBase whose persons list can violate interface
     * constraints.
     */
    private static class HireBaseStub implements ReadOnlyHireBase {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        public HireBaseStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<JobOpening> getJobOpeningList() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }
    }

}
