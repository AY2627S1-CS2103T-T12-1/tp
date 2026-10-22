package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.hirebase.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;

/**
 * Changes the interview stage of candidates identified by their permanent candidate IDs.
 */
public class StageCommand extends Command {

    public static final String COMMAND_WORD = "stage";
    public static final String RECORD_TYPE_CANDIDATE = "candidate";
    public static final String MESSAGE_INVALID_STAGE = "Interview stage must be one of "
            + Arrays.toString(InterviewStage.values()) + " (case-insensitive).";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates the interview stage of one or more candidates identified by their CANDIDATE_IDs.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE
            + " CANDIDATE_ID [MORE_CANDIDATE_IDS]... STAGE (each CANDIDATE_ID must be a positive integer)\n"
            + MESSAGE_INVALID_STAGE + "\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1 3 SCREENING";
    public static final String MESSAGE_STAGE_CANDIDATE_SUCCESS =
            "Updated interview stage for candidate %1$d: %2$s (%3$s) to %4$s";

    private final List<CandidateId> candidateIds;
    private final InterviewStage interviewStage;

    /**
     * Creates a command that sets the stage of the candidate with the given {@code candidateId}.
     */
    public StageCommand(CandidateId candidateId, InterviewStage interviewStage) {
        this(List.of(candidateId), interviewStage);
    }

    /**
     * Creates a command for a nonempty list of permanent candidate IDs. Duplicate IDs are updated once,
     * in the order they first appear. Copies the IDs so subsequent caller changes have no effect.
     */
    public StageCommand(List<CandidateId> candidateIds, InterviewStage interviewStage) {
        requireAllNonNull(candidateIds, interviewStage);
        if (candidateIds.isEmpty()) {
            throw new IllegalArgumentException("At least one candidate ID is required.");
        }
        this.candidateIds = List.copyOf(candidateIds).stream().distinct().toList();
        this.interviewStage = interviewStage;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> candidates = new ArrayList<>();
        // Resolve every ID across all candidates before changing any records.
        for (CandidateId candidateId : candidateIds) {
            candidates.add(model.getHireBase().getPersonByCandidateId(candidateId)
                    .orElseThrow(() -> new CommandException(
                            String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, candidateId.getValue()))));
        }

        List<String> messages = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Person candidate = candidates.get(i);
            Person updatedCandidate = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                    candidate.getAddress(), candidate.getNote(), candidate.getTags(),
                    candidate.getSkills(), candidate.getRole().orElse(null), interviewStage,
                    candidate.getCandidateId().orElse(null));
            model.setPerson(candidate, updatedCandidate);
            messages.add(String.format(MESSAGE_STAGE_CANDIDATE_SUCCESS, candidateIds.get(i).getValue(),
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
        return candidateIds.equals(otherStageCommand.candidateIds)
                && interviewStage == otherStageCommand.interviewStage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("candidateIds", candidateIds)
                .add("interviewStage", interviewStage)
                .toString();
    }
}
