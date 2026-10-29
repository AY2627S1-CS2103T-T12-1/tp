package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.hirebase.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.calendar.CalendarViewOptions;
import seedu.hirebase.model.calendar.CalendarViewOptions.Period;

public class ViewCalendarCommandTest {

    @Test
    public void execute_selectsCalendarAndReturnsOptions() {
        CalendarViewOptions options = new CalendarViewOptions(Period.MONTH, true, false);
        Model model = new ModelManager();

        CommandResult result = new ViewCalendarCommand(options).execute(model);

        assertEquals(ViewCalendarCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(CommandResult.ListView.CALENDAR, result.getListView());
        assertEquals(options, result.getCalendarViewOptions().orElseThrow());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        ViewCalendarCommand command = new ViewCalendarCommand(CalendarViewOptions.DEFAULT);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void constructor_nullOptions_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCalendarCommand(null));
    }
}
