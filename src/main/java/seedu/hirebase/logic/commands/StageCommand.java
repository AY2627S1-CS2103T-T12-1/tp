package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.hirebase.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Arrays;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;

/**
 * Changes the interview stage of a candidate identified by their CANDIDATE_ID.
 */
public class StageCommand extends Command {

    public static final String COMMAND_WORD = "stage";
    public static final String RECORD_TYPE_CANDIDATE = "candidate";
    public static final String MESSAGE_INVALID_STAGE = "Interview stage must be one of "
            + Arrays.toString(InterviewStage.values()) + " (case-insensitive).";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates the interview stage of the candidate identified by their CANDIDATE_ID.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE
            + " CANDIDATE_ID STAGE (CANDIDATE_ID must be a positive integer)\n"
            + MESSAGE_INVALID_STAGE + "\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1 SCREENING";
    public static final String MESSAGE_STAGE_CANDIDATE_SUCCESS =
            "Updated interview stage for candidate %1$d: %2$s (%3$s) to %4$s";

    private final CandidateId candidateId;
    private final InterviewStage interviewStage;

    /**
     * Creates a command that sets the stage of the candidate with the given {@code candidateId}.
     */
    public StageCommand(CandidateId candidateId, InterviewStage interviewStage) {
        requireAllNonNull(candidateId, interviewStage);
        this.candidateId = candidateId;
        this.interviewStage = interviewStage;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person candidate = model.getHireBase().getPersonByCandidateId(candidateId)
                .orElseThrow(() -> new CommandException(
                        String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, candidateId.getValue())));
        Person updatedCandidate = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                candidate.getAddress(), candidate.getNote(), candidate.getTags(),
                candidate.getSkills(), candidate.getRole().orElse(null), interviewStage,
                candidate.getCandidateId().orElse(null));
        model.setPerson(candidate, updatedCandidate);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.format(MESSAGE_STAGE_CANDIDATE_SUCCESS, candidateId.getValue(),
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
        return candidateId.equals(otherStageCommand.candidateId)
                && interviewStage == otherStageCommand.interviewStage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("candidateId", candidateId)
                .add("interviewStage", interviewStage)
                .toString();
    }
}
