package seedu.hirebase.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Set;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.hirebase.commons.core.GuiSettings;
import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.logic.commands.AddCommand;
import seedu.hirebase.logic.commands.AddEventCommand;
import seedu.hirebase.logic.commands.AddJobCommand;
import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.DeleteCommand;
import seedu.hirebase.logic.commands.DeleteJobCommand;
import seedu.hirebase.logic.commands.EditCommand;
import seedu.hirebase.logic.commands.EditJobCommand;
import seedu.hirebase.logic.commands.FindCommand;
import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.ListJobsCommand;
import seedu.hirebase.logic.commands.NoteCommand;
import seedu.hirebase.logic.commands.StageCommand;
import seedu.hirebase.logic.commands.ViewCalendarCommand;
import seedu.hirebase.logic.commands.ViewEventCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.HireBaseParser;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
        "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final HireBaseParser hireBaseParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and
     * {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        this.hireBaseParser = new HireBaseParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = hireBaseParser.parseCommand(commandText);
        if (command instanceof AddJobCommand addJobCommand) {
            return executeAddJob(addJobCommand);
        }
        if (command instanceof AddEventCommand addEventCommand) {
            return executeAddEvent(addEventCommand);
        }
        if (command instanceof DeleteJobCommand deleteJobCommand) {
            return executeDeleteJob(deleteJobCommand);
        }
        if (command instanceof EditJobCommand editJobCommand) {
            return executeEditJob(editJobCommand);
        }
        if (command instanceof AddCommand || command instanceof DeleteCommand || command instanceof EditCommand
                || command instanceof NoteCommand || command instanceof StageCommand) {
            return executeCandidateChange(command);
        }
        commandResult = command.execute(model);
        if (command instanceof ListJobsCommand) {
            return commandResult.withListView(CommandResult.ListView.JOBS);
        }
        if (command instanceof ViewEventCommand || command instanceof ViewCalendarCommand) {
            // Viewing changes no data, so it skips saving and a failed save cannot hide the details.
            return commandResult;
        }

        try {
            storage.saveHireBase(model.getHireBase());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        if (command instanceof FindCommand || command instanceof ListCommand) {
            return commandResult.withListView(CommandResult.ListView.CANDIDATES);
        }
        return commandResult;
    }

    /**
     * Saves candidate changes before publishing them, preserving records, filters and IDs on a failed save.
     */
    private CommandResult executeCandidateChange(Command command) throws CommandException {
        Model pending = new ModelManager(model.getHireBase(), model.getUserPrefs());
        CommandResult result = command.execute(pending);
        try {
            storage.saveHireBase(pending.getHireBase());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException e) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, e.getMessage()), e);
        }
        // Replay only after saving so the live model keeps each command's existing filter behaviour.
        command.execute(model);
        return result.withListView(CommandResult.ListView.CANDIDATES);
    }

    private CommandResult executeAddJob(AddJobCommand command) throws CommandException {
        // Save a detached copy first: failed persistence must not publish a new opening to observers.
        Model pending = new ModelManager(model.getHireBase(), model.getUserPrefs());
        CommandResult result = command.execute(pending);
        try {
            storage.saveHireBase(pending.getHireBase());
        } catch (IOException e) {
            throw new CommandException(AddJobCommand.MESSAGE_SAVE_FAILURE, e);
        }
        model.addJobOpening(command.getJobOpening());
        Set<Skill> requiredSkills = command.getJobOpening().getSkills();
        if (!requiredSkills.isEmpty()) {
            model.updateFilteredPersonList(person -> person.getInterviewStage() == InterviewStage.REJECTED
                    && person.getSkills().stream().anyMatch(requiredSkills::contains));
            int matchingCandidates = model.getFilteredPersonList().size();
            String recommendationMessage = matchingCandidates == 0
                    ? "No previously rejected candidates have matching skills."
                    : String.format("Found %d previously rejected candidate%s with matching skills.",
                            matchingCandidates, matchingCandidates == 1 ? "" : "s");
            return new CommandResult(result.getFeedbackToUser() + "\n" + recommendationMessage)
                    .withListView(CommandResult.ListView.CANDIDATES);
        }
        return result.withListView(CommandResult.ListView.JOBS);
    }

    /**
     * Adds the event to a copy of the model and saves that copy before changing the real model,
     * so a failed save leaves no event behind and uses up no event ID.
     */
    private CommandResult executeAddEvent(AddEventCommand command) throws CommandException {
        Model pending = new ModelManager(model.getHireBase(), model.getUserPrefs());
        CommandResult result = command.execute(pending);
        // The copy started from the model's ID counter, so the new event got exactly that ID.
        // The command has already rejected an exhausted counter, so the value fits in an int.
        EventId newId = new EventId((int) model.getHireBase().getNextEventId());
        Event saved = pending.findEvent(newId).orElseThrow();
        try {
            storage.saveHireBase(pending.getHireBase());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException e) {
            logger.warning("Could not save new event " + newId + ": " + e.getMessage());
            throw new CommandException(AddEventCommand.MESSAGE_SAVE_FAILURE, e);
        }
        // Publish the exact event that was saved; it keeps its ID, so the app and the file agree.
        model.addEvent(saved);
        return result.withAddedEvent(saved).withListView(CommandResult.ListView.CALENDAR);
    }

    private CommandResult executeDeleteJob(DeleteJobCommand command) throws CommandException {
        Model pending = new ModelManager(model.getHireBase(), model.getUserPrefs());
        CommandResult result = command.execute(pending);
        try {
            storage.saveHireBase(pending.getHireBase());
        } catch (IOException e) {
            throw new CommandException(DeleteJobCommand.MESSAGE_SAVE_FAILURE, e);
        }
        // Publish the deletion only after persistence succeeds, preserving candidate filters and job observers.
        command.execute(model);
        return result.withListView(CommandResult.ListView.JOBS);
    }

    private CommandResult executeEditJob(EditJobCommand command) throws CommandException {
        Model pending = new ModelManager(model.getHireBase(), model.getUserPrefs());
        CommandResult result = command.execute(pending);
        try {
            storage.saveHireBase(pending.getHireBase());
        } catch (IOException e) {
            throw new CommandException(EditJobCommand.MESSAGE_SAVE_FAILURE, e);
        }
        // Publish only after saving, preserving candidate filters and job observers on failure.
        command.execute(model);
        return result.withListView(CommandResult.ListView.JOBS);
    }

    @Override
    public ObservableList<JobOpening> getJobOpeningList() {
        return model.getJobOpeningList();
    }

    @Override
    public ObservableList<Event> getEventList() {
        return model.getEventList();
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
