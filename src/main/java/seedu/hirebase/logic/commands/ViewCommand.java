package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.CandidateId;
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
            + ": Views the full details of the candidate identified by their CANDIDATE_ID, "
            + "and shows only that candidate.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " CANDIDATE_ID (CANDIDATE_ID must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1\n"
            + "To view an event, use: " + ViewEventCommand.MESSAGE_USAGE;

    private static final String EMPTY_PLACEHOLDER = "—";

    private static final Logger logger = LogsCenter.getLogger(ViewCommand.class);

    private final CandidateId targetId;

    /**
     * Creates a {@code ViewCommand} to view the candidate with the specified {@code targetId}.
     */
    public ViewCommand(CandidateId targetId) {
        requireNonNull(targetId);
        this.targetId = targetId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToView = model.getHireBase().getPersonByCandidateId(targetId)
                .orElseThrow(() -> {
                    logger.warning("View command rejected: no candidate with ID " + targetId);
                    return new CommandException(
                            String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, targetId.getValue()));
                });
        model.updateFilteredPersonList(new SamePersonPredicate(personToView));

        logger.info("Viewing candidate with ID " + targetId);
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

        return targetId.equals(otherViewCommand.targetId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetId", targetId).toString();
    }
}
