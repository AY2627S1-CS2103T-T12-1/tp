package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.ListJobsCommand;
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
        assertParseFailure(parser, "candidates r/Backend Engineer", expectedUsageFailure);
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
}
