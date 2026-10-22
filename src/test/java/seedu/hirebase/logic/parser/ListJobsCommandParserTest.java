package seedu.hirebase.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.ListJobsCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;

public class ListJobsCommandParserTest {
    private final HireBaseParser parser = new HireBaseParser();

    @Test
    public void parse_jobs_acceptsWhitespace() throws Exception {
        for (String input : new String[] {"list jobs", "  list   jobs  ", "list\tjobs\t"}) {
            assertEquals(new ListJobsCommand(), parser.parseCommand(input));
        }
    }

    @Test
    public void parse_extraArguments_rejected() {
        for (String input : new String[] {"list jobs 2", "list jobs t/Engineer", "list jobs extra words",
            "list jobs\nextra", "list jobs\textra"}) {
            assertEquals(ListJobsCommand.MESSAGE_EXTRA_ARGUMENTS,
                    assertThrows(ParseException.class, () -> parser.parseCommand(input)).getMessage());
        }
    }

    @Test
    public void parse_wrongCaseOrRecordType_rejected() {
        for (String input : new String[] {"list job", "list Jobs", "List jobs", "list jobs2", "list", "list people"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parse_candidates_routesToCandidateListAndRejectsExtraText() throws Exception {
        assertEquals(new ListCommand(), parser.parseCommand("list candidates"));
        assertThrows(ParseException.class, () -> parser.parseCommand("list candidates ignored arguments"));
    }
}
