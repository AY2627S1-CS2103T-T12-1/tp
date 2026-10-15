package seedu.hirebase.logic.commands;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.model.candidate.InterviewStage;

/**
 * Shortcut for setting a candidate's interview stage to {@code REJECTED}.
 */
public class RejectCommand extends StageCommand {
    public static final String COMMAND_WORD = "reject";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets the interview stage of a candidate in the displayed list to REJECTED.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " INDEX (INDEX must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1";

    /**
     * Creates a command for the candidate at {@code index} in the displayed list.
     */
    public RejectCommand(Index index) {
        super(index, InterviewStage.REJECTED);
    }
}
