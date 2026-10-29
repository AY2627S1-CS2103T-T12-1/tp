package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.hirebase.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void candidateId_defaultsToEmpty_andWithCandidateIdReturnsCopy() {
        assertFalse(ALICE.getCandidateId().isPresent());

        Person withId = ALICE.withCandidateId(new CandidateId(5));

        assertEquals(new CandidateId(5), withId.getCandidateId().get());
        assertFalse(ALICE.getCandidateId().isPresent());
    }

    @Test
    public void equals_ignoresCandidateId() {
        assertEquals(ALICE, ALICE.withCandidateId(new CandidateId(5)));
        assertEquals(ALICE.hashCode(), ALICE.withCandidateId(new CandidateId(5)).hashCode());
    }

    @Test
    public void priorityScore_legacyConstructor_defaultsToLow() {
        Person candidate = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getNote(), ALICE.getTags(), ALICE.getSkills(), ALICE.getRole().orElse(null),
                ALICE.getInterviewStage(), new CandidateId(5));

        assertEquals(PriorityScore.DEFAULT, candidate.getPriorityScore());
    }

    @Test
    public void withPriorityScore_validScore_preservesOtherDetailsAndId() {
        Person original = new PersonBuilder(ALICE).withSkills("Java").withRole("Engineer")
                .withNote("Follow up").withInterviewStage(InterviewStage.ACCEPTED).build()
                .withCandidateId(new CandidateId(5));

        Person updated = original.withPriorityScore(new PriorityScore("3"));

        assertEquals(new PersonBuilder(original).withPriorityScore("3").build(), updated);
        assertEquals(original.getCandidateId(), updated.getCandidateId());
        assertEquals(PriorityScore.DEFAULT, original.getPriorityScore());
        assertTrue(original.isSamePerson(updated));
    }

    @Test
    public void withPriorityScore_nullScore_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ALICE.withPriorityScore(null));
    }

    @Test
    public void withCandidateId_nonDefaultPriority_preservesPriorityScore() {
        Person original = new PersonBuilder(ALICE).withPriorityScore("2").build();

        Person updated = original.withCandidateId(new CandidateId(7));

        assertEquals(new PriorityScore("2"), updated.getPriorityScore());
        assertEquals(new CandidateId(7), updated.getCandidateId().get());
        assertEquals(original, updated);
    }

    @Test
    public void equals_priorityScore_distinguishesScoresWithoutChangingIdentity() {
        Person candidate = new PersonBuilder(ALICE).withPriorityScore("3").build();
        Person sameScore = new PersonBuilder(candidate).build();
        Person differentScore = new PersonBuilder(candidate).withPriorityScore("2").build();

        assertEquals(candidate, sameScore);
        assertEquals(candidate.hashCode(), sameScore.hashCode());
        assertNotEquals(candidate, differentScore);
        assertNotEquals(candidate.hashCode(), differentScore.hashCode());
        assertTrue(candidate.isSamePerson(differentScore));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same email, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same name, different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // email differs only in case, all other attributes same -> returns true
        editedAlice = new PersonBuilder(ALICE).withEmail(ALICE.getEmail().value.toUpperCase()).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // name casing does not change identity
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name whitespace does not change identity
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different note -> returns false
        editedAlice = new PersonBuilder(ALICE).withNote("Likes baseball").build();
        assertFalse(ALICE.equals(editedAlice));
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void equals_recruitmentDetails_distinguishesSkillsAndRole() {
        Person candidate = new PersonBuilder(ALICE).withSkills("Java").withRole("Engineer").build();
        assertTrue(candidate.equals(new PersonBuilder(candidate).withSkills("java").withRole("engineer").build()));
        assertFalse(candidate.equals(new PersonBuilder(candidate).withSkills("SQL").build()));
        assertFalse(candidate.equals(new PersonBuilder(candidate).withRole("Developer").build()));
        assertEquals(candidate.hashCode(), new PersonBuilder(candidate).build().hashCode());
    }

    @Test
    public void equals_interviewStage_distinguishesStagesWithoutChangingIdentity() {
        Person candidate = new PersonBuilder(ALICE).withInterviewStage(InterviewStage.ACCEPTED).build();
        Person sameStage = new PersonBuilder(candidate).build();
        Person differentStage = new PersonBuilder(candidate).withInterviewStage(InterviewStage.REJECTED).build();

        assertTrue(candidate.equals(sameStage));
        assertFalse(candidate.equals(differentStage));
        assertFalse(differentStage.equals(candidate));
        assertTrue(candidate.isSamePerson(differentStage));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", note=" + ALICE.getNote()
                + ", tags=" + ALICE.getTags() + ", skills=[], role=null, interviewStage=SCREENING, candidateId=null"
                + ", priorityScore=1 (Low)}";
        assertEquals(expected, ALICE.toString());
    }
}
