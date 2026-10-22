package seedu.hirebase.logic.commands;

import java.util.List;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.model.candidate.InterviewStage;

/**
 * Shortcut for setting candidates' interview stages to {@code ACCEPTED}.
 */
public class AcceptCommand extends StageCommand {
    public static final String COMMAND_WORD = "accept";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets the interview stage of one or more candidates in the displayed list to ACCEPTED.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE
            + " INDEX [MORE_INDEXES]... (each INDEX must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1 3";

    /**
     * Creates a command for the candidates at {@code indexes} in the displayed list.
     */
    public AcceptCommand(List<Index> indexes) {
        super(indexes, InterviewStage.ACCEPTED);
    }
}
