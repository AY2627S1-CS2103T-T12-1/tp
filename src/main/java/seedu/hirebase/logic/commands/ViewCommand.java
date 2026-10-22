package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.SamePersonPredicate;

/**
 * Views the full details of a candidate identified by the index in the displayed candidate list.
 * Shows the details in the result display and collapses the list to only that candidate.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String RECORD_TYPE_CANDIDATE = "candidate";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Views the full details of the candidate identified by the index number "
            + "used in the displayed candidate list, and shows only that candidate.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " INDEX (INDEX must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1";

    private static final String EMPTY_PLACEHOLDER = "—";

    private static final Logger logger = LogsCenter.getLogger(ViewCommand.class);

    private final Index targetIndex;

    /**
     * Creates a {@code ViewCommand} to view the candidate at the specified {@code targetIndex}.
     */
    public ViewCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            logger.warning("View command rejected: index " + targetIndex.getOneBased()
                    + " out of bounds for displayed list of size " + lastShownList.size());
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        // The bounds check above guarantees an in-range index at this point.
        assert targetIndex.getZeroBased() >= 0 && targetIndex.getZeroBased() < lastShownList.size()
                : "View target index must be within the displayed candidate list";

        Person personToView = lastShownList.get(targetIndex.getZeroBased());
        model.updateFilteredPersonList(new SamePersonPredicate(personToView));

        logger.info("Viewing candidate at index " + targetIndex.getOneBased());
        return new CommandResult(format(personToView)).withListView(CommandResult.ListView.CANDIDATES);
    }

    /**
     * Returns a multi-line, human-readable summary of {@code person} for the result display.
     * Empty optional fields are shown as a placeholder.
     */
    private static String format(Person person) {
        String stage = switch (person.getInterviewStage()) {
            case SCREENING -> "Screening";
            case ACCEPTED -> "Accepted";
            case REJECTED -> "Rejected";
        };
        String role = person.getRole().map(value -> value.value).orElse(EMPTY_PLACEHOLDER);
        String skills = person.getSkills().isEmpty()
                ? EMPTY_PLACEHOLDER
                : person.getSkills().stream().map(skill -> skill.value).sorted().collect(Collectors.joining(", "));
        String note = person.getNote().value.isEmpty() ? EMPTY_PLACEHOLDER : person.getNote().value;
        return "Name:    " + person.getName().fullName + "\n"
                + "Stage:   " + stage + "\n"
                + "Phone:   " + person.getPhone().value + "\n"
                + "Email:   " + person.getEmail().value + "\n"
                + "Address: " + person.getAddress().value + "\n"
                + "Role:    " + role + "\n"
                + "Skills:  " + skills + "\n"
                + "Notes:   " + note;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }

        return targetIndex.equals(otherViewCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
