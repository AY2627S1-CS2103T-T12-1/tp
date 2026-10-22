package seedu.hirebase.logic.commands;

import java.util.List;

import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;

/**
 * Shortcut for setting candidates' interview stages to {@code ACCEPTED}.
 */
public class AcceptCommand extends StageCommand {
    public static final String COMMAND_WORD = "accept";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets the interview stage of one or more candidates identified by their CANDIDATE_IDs to ACCEPTED.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE
            + " CANDIDATE_ID [MORE_CANDIDATE_IDS]... (each CANDIDATE_ID must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1 3";

    /**
     * Creates a command for the candidate with the given {@code candidateId}.
     */
    public AcceptCommand(CandidateId candidateId) {
        this(List.of(candidateId));
    }

    /**
     * Creates a command for the candidates with the given permanent {@code candidateIds}.
     */
    public AcceptCommand(List<CandidateId> candidateIds) {
        super(candidateIds, InterviewStage.ACCEPTED);
    }
}
