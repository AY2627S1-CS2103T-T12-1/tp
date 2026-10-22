package seedu.hirebase.logic.commands;

import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;

/**
 * Shortcut for setting a candidate's interview stage to {@code REJECTED}.
 */
public class RejectCommand extends StageCommand {
    public static final String COMMAND_WORD = "reject";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets the interview stage of the candidate identified by their CANDIDATE_ID to REJECTED.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " CANDIDATE_ID (CANDIDATE_ID must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1";

    /**
     * Creates a command for the candidate with the given {@code candidateId}.
     */
    public RejectCommand(CandidateId candidateId) {
        super(candidateId, InterviewStage.REJECTED);
    }
}
