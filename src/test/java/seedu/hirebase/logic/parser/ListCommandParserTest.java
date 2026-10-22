package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.ListJobsCommand;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;

public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    private final String expectedUsageFailure =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);

    @Test
    public void parse_validPreamble_success() {
        assertParseSuccess(parser, ListCommand.RECORD_TYPE_CANDIDATE, new ListCommand());
        assertParseSuccess(parser, ListCommand.RECORD_TYPE_CANDIDATE + " ", new ListCommand());
        assertParseSuccess(parser, "\t candidates\n", new ListCommand());
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, ListCommand.RECORD_TYPE_CANDIDATE + "1", expectedUsageFailure);
        assertParseFailure(parser, "invalid preamble", expectedUsageFailure);
        assertParseFailure(parser, "candidates additional arguments", expectedUsageFailure);
        assertParseFailure(parser, "candidates stage/SCREENING", expectedUsageFailure);
    }

    @Test
    public void parse_singleTechnicalSkill_success() {
        for (String skill : new String[] {"Java", "Spring", "SQL", "C++", "C#", "Build CI/CD", "a".repeat(30)}) {
            assertParseSuccess(parser, "candidates s/" + skill, new ListCommand(new Skill(skill)));
        }
        assertParseSuccess(parser, "candidates s/jAvA", new ListCommand(new Skill("Java")));
    }

    @Test
    public void parse_multiWordSkillWithWhitespace_success() {
        assertParseSuccess(parser, " \t candidates\ts/  Machine\n Learning  ",
                new ListCommand(new Skill("Machine Learning")));
    }

    @Test
    public void parse_emptyOrInvalidSkill_failure() {
        for (String skill : new String[] {"", "   ", "@", "a".repeat(31)}) {
            assertParseFailure(parser, "candidates s/" + skill, Skill.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_repeatedSkillPrefix_failure() {
        String expected = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_SKILL);
        assertParseFailure(parser, "candidates s/Java s/SQL", expected);
        assertParseFailure(parser, "candidates s/Java s/java", expected);
    }

    @Test
    public void parse_jobListRouting_preservesJobSyntax() {
        assertParseSuccess(parser, "\t jobs \n", new ListJobsCommand());
        assertParseFailure(parser, "jobs s/Java", ListJobsCommand.MESSAGE_EXTRA_ARGUMENTS);
    }
    @Test
    public void parse_roleFilter_normalizesAndValidatesRole() {
        for (String role : new String[] {"Backend Engineer", "Full-Stack Developer", "DevOps/SRE", "a".repeat(100)}) {
            assertParseSuccess(parser, "candidates r/" + role, new ListCommand(null, new Role(role)));
        }
        assertParseSuccess(parser, "candidates r/ backend\t engineer  ",
                new ListCommand(null, new Role("Backend Engineer")));
        for (String role : new String[] {"", "  ", "@", "a".repeat(101)}) {
            assertParseFailure(parser, "candidates r/" + role, Role.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_skillAndRole_acceptsEitherOrder() {
        ListCommand expected = new ListCommand(new Skill("Java"), new Role("Backend Engineer"));
        assertParseSuccess(parser, "candidates s/Java r/Backend Engineer", expected);
        assertParseSuccess(parser, "candidates r/Backend Engineer s/Java", expected);
        assertParseFailure(parser, "candidates s/@ r/Backend Engineer", Skill.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedRoleOrSkill_rejectsDuplicates() {
        assertParseFailure(parser, "candidates r/Backend Engineer r/Backend Engineer",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE));
        assertParseFailure(parser, "candidates r/Backend Engineer r/QA Engineer s/Java",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE));
        assertParseFailure(parser, "candidates s/Java s/SQL r/Backend Engineer",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_SKILL));
        assertParseFailure(parser, "jobs r/Backend Engineer", ListJobsCommand.MESSAGE_EXTRA_ARGUMENTS);
    }

}
