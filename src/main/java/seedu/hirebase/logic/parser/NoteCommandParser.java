package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.logging.Logger;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.commands.NoteCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.Note;

/**
 * Parses input arguments and creates a new {@code NoteCommand}.
 * Syntax: {@code note INDEX [NOTE]} — the free-text remainder after the index is the note;
 * an empty remainder clears the note.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    private static final Logger logger = LogsCenter.getLogger(NoteCommandParser.class);

    /**
     * Parses the given arguments into a command that replaces or clears a candidate's note.
     * @throws ParseException if the index is missing or not a positive integer.
     */
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
        }

        String[] parts = trimmedArgs.split("\\s+", 2);
        assert parts.length >= 1 : "A non-empty trimmed string must split into at least one token";

        final Index index;
        try {
            index = ParserUtil.parseIndex(parts[0]);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE), pe);
        }

        final String noteText = parts.length > 1 ? parts[1].strip() : "";
        logger.fine(() -> "Parsed note command for index " + index.getOneBased()
                + " (" + (noteText.isEmpty() ? "clear" : "set") + ")");
        return new NoteCommand(index, new Note(noteText));
    }
}
