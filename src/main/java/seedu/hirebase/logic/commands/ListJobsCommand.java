package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.job.JobOpening;

/**
 * Lists all openings in creation order without changing records or candidate filters.
 */
public class ListJobsCommand extends Command {
    public static final String RECORD_TYPE = "jobs";
    public static final String MESSAGE_USAGE = "list jobs";
    public static final String MESSAGE_NO_JOBS = "No job openings found.";
    public static final String MESSAGE_SUCCESS = "Listed %d job opening%s.";
    public static final String MESSAGE_EXTRA_ARGUMENTS =
            "The list jobs command does not accept parameters. Usage: " + MESSAGE_USAGE;

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<JobOpening> jobs = model.getJobOpeningList();
        if (jobs.isEmpty()) {
            return new CommandResult(MESSAGE_NO_JOBS);
        }
        StringBuilder feedback = new StringBuilder(String.format(MESSAGE_SUCCESS,
                jobs.size(), jobs.size() == 1 ? "" : "s"));
        for (int i = 0; i < jobs.size(); i++) {
            JobOpening job = jobs.get(i);
            feedback.append("\n\n").append(job.getJobId().orElseThrow()).append(". ").append(job.getTitle())
                    .append(" — ").append(job.getDepartment())
                    .append("\nDescription: ").append(job.getDescription());
        }
        return new CommandResult(feedback.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ListJobsCommand;
    }

    @Override
    public int hashCode() {
        return ListJobsCommand.class.hashCode();
    }
}
