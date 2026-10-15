package seedu.hirebase.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.hirebase.commons.core.GuiSettings;
import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.logic.commands.AddJobCommand;
import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.DeleteJobCommand;
import seedu.hirebase.logic.commands.ListJobsCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.HireBaseParser;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.candidate.Person;
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
        if (command instanceof DeleteJobCommand deleteJobCommand) {
            return executeDeleteJob(deleteJobCommand);
        }
        commandResult = command.execute(model);
        if (command instanceof ListJobsCommand) {
            return commandResult;
        }

        try {
            storage.saveHireBase(model.getHireBase());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
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
        return result;
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
        return result;
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
