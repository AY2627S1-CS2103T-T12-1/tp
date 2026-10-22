package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.hirebase.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
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
 * Changes the interview stage of candidates identified by their displayed indexes.
 */
public class StageCommand extends Command {

    public static final String COMMAND_WORD = "stage";
    public static final String RECORD_TYPE_CANDIDATE = "candidate";
    public static final String MESSAGE_INVALID_STAGE = "Interview stage must be one of "
            + Arrays.toString(InterviewStage.values()) + " (case-insensitive).";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates the interview stage of one or more candidates in the displayed list.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE
            + " INDEX [MORE_INDEXES]... STAGE (each INDEX must be a positive integer)\n"
            + MESSAGE_INVALID_STAGE + "\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1 3 SCREENING";
    public static final String MESSAGE_STAGE_CANDIDATE_SUCCESS =
            "Updated interview stage for candidate %1$d: %2$s (%3$s) to %4$s";

    private final List<Index> indexes;
    private final InterviewStage interviewStage;

    /**
     * Creates a command that sets the stage of the candidate at {@code index} in the displayed list.
     */
    public StageCommand(Index index, InterviewStage interviewStage) {
        this(List.of(index), interviewStage);
    }

    /**
     * Creates a command for a nonempty list of displayed indexes. Duplicate indexes are updated once,
     * in the order they first appear. Copies the indexes so subsequent caller changes have no effect.
     */
    public StageCommand(List<Index> indexes, InterviewStage interviewStage) {
        requireAllNonNull(indexes, interviewStage);
        if (indexes.isEmpty()) {
            throw new IllegalArgumentException("At least one candidate index is required.");
        }
        // Deduplicate numeric values because Index does not define hashCode.
        this.indexes = List.copyOf(indexes).stream()
                .map(Index::getZeroBased).distinct().map(Index::fromZeroBased).toList();
        this.interviewStage = interviewStage;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = List.copyOf(model.getFilteredPersonList());
        // Validate every index before changing any candidate.
        for (Index index : indexes) {
            if (index.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
        }

        List<String> messages = new ArrayList<>();
        for (Index index : indexes) {
            Person candidate = lastShownList.get(index.getZeroBased());
            Person updatedCandidate = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                    candidate.getAddress(), candidate.getNote(), candidate.getTags(),
                    candidate.getSkills(), candidate.getRole().orElse(null), interviewStage,
                    candidate.getCandidateId().orElse(null));
            model.setPerson(candidate, updatedCandidate);
            messages.add(String.format(MESSAGE_STAGE_CANDIDATE_SUCCESS, index.getOneBased(),
                    updatedCandidate.getName(), updatedCandidate.getEmail(), interviewStage));
        }
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.join("\n", messages));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof StageCommand otherStageCommand)) {
            return false;
        }
        return indexes.equals(otherStageCommand.indexes) && interviewStage == otherStageCommand.interviewStage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("indexes", indexes)
                .add("interviewStage", interviewStage)
                .toString();
    }
}
