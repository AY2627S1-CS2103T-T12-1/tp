package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.hirebase.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Arrays;
import java.util.List;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;

/**
 * Changes the interview stage of a candidate identified by their displayed index.
 */
public class StageCommand extends Command {

    public static final String COMMAND_WORD = "stage";
    public static final String RECORD_TYPE_CANDIDATE = "candidate";
    public static final String MESSAGE_INVALID_STAGE = "Interview stage must be one of "
            + Arrays.toString(InterviewStage.values()) + " (case-insensitive).";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates the interview stage of a candidate in the displayed list.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " INDEX STAGE (INDEX must be a positive integer)\n"
            + MESSAGE_INVALID_STAGE + "\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1 SCREENING";
    public static final String MESSAGE_STAGE_CANDIDATE_SUCCESS =
            "Updated interview stage for candidate %1$d: %2$s (%3$s) to %4$s";

    private final Index index;
    private final InterviewStage interviewStage;

    /**
     * Creates a command that sets the stage of the candidate at {@code index} in the displayed list.
     */
    public StageCommand(Index index, InterviewStage interviewStage) {
        requireAllNonNull(index, interviewStage);
        this.index = index;
        this.interviewStage = interviewStage;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person candidate = lastShownList.get(index.getZeroBased());
        Person updatedCandidate = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                candidate.getAddress(), candidate.getNote(), candidate.getTags(),
                candidate.getSkills(), candidate.getRole().orElse(null), interviewStage,
                candidate.getCandidateId().orElse(null));
        model.setPerson(candidate, updatedCandidate);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.format(MESSAGE_STAGE_CANDIDATE_SUCCESS, index.getOneBased(),
                updatedCandidate.getName(), updatedCandidate.getEmail(), interviewStage));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof StageCommand otherStageCommand)) {
            return false;
        }
        return index.equals(otherStageCommand.index) && interviewStage == otherStageCommand.interviewStage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("interviewStage", interviewStage)
                .toString();
    }
}
