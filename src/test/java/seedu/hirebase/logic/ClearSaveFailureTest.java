package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.hirebase.logic.commands.ClearCommand;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.EventBuilder;

public class ClearSaveFailureTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_clearIoFailure_preservesRecordsCountersFilterObserversAndFile() throws Exception {
        assertFailedClearPreservesState(new IOException("disk full"));
    }

    @Test
    public void execute_clearPermissionFailure_preservesRecordsCountersFilterObserversAndFile() throws Exception {
        assertFailedClearPreservesState(new AccessDeniedException("hirebase.json"));
    }

    @Test
    public void execute_listAfterFailedClear_doesNotPersistDeletion() throws Exception {
        Fixture fixture = createFixture();
        HireBase before = new HireBase(fixture.model().getHireBase());
        fixture.storage().failure = new IOException("disk full");
        assertThrows(CommandException.class, () -> fixture.logic().execute("clear"));

        fixture.storage().failure = null;
        fixture.logic().execute("list candidates");

        assertEquals(before, fixture.model().getHireBase());
        assertEquals(before, fixture.storage().readHireBase().orElseThrow());
        assertCountersEqual(before, fixture.model().getHireBase());
        assertCountersEqual(before, fixture.storage().readHireBase().orElseThrow());
    }

    @Test
    public void execute_clearSuccess_removesAllRecordsAndPreservesCountersAndTab() throws Exception {
        Fixture fixture = createFixture();
        HireBase before = new HireBase(fixture.model().getHireBase());

        CommandResult result = fixture.logic().execute("clear");

        ReadOnlyHireBase cleared = fixture.model().getHireBase();
        assertTrue(cleared.getPersonList().isEmpty());
        assertTrue(cleared.getJobOpeningList().isEmpty());
        assertTrue(cleared.getEventList().isEmpty());
        assertCountersEqual(before, cleared);
        assertEquals(ClearCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(CommandResult.ListView.KEEP, result.getListView());
        ReadOnlyHireBase saved = fixture.storage().readHireBase().orElseThrow();
        assertEquals(cleared, saved);
        assertCountersEqual(before, saved);
    }

    private void assertFailedClearPreservesState(IOException failure) throws Exception {
        Fixture fixture = createFixture();
        HireBase before = new HireBase(fixture.model().getHireBase());
        List<Person> displayedBefore = List.copyOf(fixture.model().getFilteredPersonList());
        byte[] savedBefore = Files.readAllBytes(fixture.storage().getHireBaseFilePath());
        AtomicInteger observerChanges = new AtomicInteger();
        observe(fixture.model().getHireBase().getPersonList(), observerChanges);
        observe(fixture.model().getJobOpeningList(), observerChanges);
        observe(fixture.model().getEventList(), observerChanges);
        observe(fixture.model().getFilteredPersonList(), observerChanges);
        fixture.storage().failure = failure;

        CommandException error = assertThrows(CommandException.class, () -> fixture.logic().execute("clear"));

        assertSame(failure, error.getCause());
        assertEquals(before, fixture.model().getHireBase());
        assertCountersEqual(before, fixture.model().getHireBase());
        assertEquals(displayedBefore, fixture.model().getFilteredPersonList());
        assertEquals(0, observerChanges.get());
        assertArrayEquals(savedBefore, Files.readAllBytes(fixture.storage().getHireBaseFilePath()));
    }

    private Fixture createFixture() throws IOException {
        HireBase data = new HireBase();
        data.addPerson(ALICE);
        data.addPerson(BENSON);
        data.addJobOpening(new JobOpening(new JobTitle("Engineer"), new Department("Engineering"),
                new Description("Build services.")));
        data.addEvent(new EventBuilder().withCandidateIds(1, 2).build());
        data.reserveCandidateIdsUpTo(20);
        data.reserveJobIdsUpTo(30);
        data.reserveEventIdsUpTo(40);
        ModelManager model = new ModelManager(data, new UserPrefs());
        model.updateFilteredPersonList(person -> person.getName().equals(ALICE.getName()));
        FailingStorage storage = new FailingStorage(temporaryFolder.resolve("hirebase.json"));
        storage.saveHireBase(model.getHireBase());
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        return new Fixture(model, storage, logic);
    }

    private static void assertCountersEqual(ReadOnlyHireBase expected, ReadOnlyHireBase actual) {
        assertEquals(expected.getNextCandidateId(), actual.getNextCandidateId());
        assertEquals(expected.getNextJobId(), actual.getNextJobId());
        assertEquals(expected.getNextEventId(), actual.getNextEventId());
    }

    private static <T> void observe(ObservableList<T> list, AtomicInteger changes) {
        list.addListener((ListChangeListener<T>) change -> changes.incrementAndGet());
    }

    private record Fixture(ModelManager model, FailingStorage storage, LogicManager logic) { }

    private static class FailingStorage extends JsonHireBaseStorage {
        private IOException failure;

        FailingStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveHireBase(ReadOnlyHireBase hireBase) throws IOException {
            if (failure != null) {
                throw failure;
            }
            super.saveHireBase(hireBase);
        }
    }
}
