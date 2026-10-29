package seedu.hirebase.ui;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.time.Clock;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.hirebase.commons.core.GuiSettings;
import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.logic.Logic;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.Person;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final String MESSAGE_EVENT_NOT_VISIBLE = "Saved successfully, but not shown in the current "
            + "calendar view. Check the selected period and filters.";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;
    private final Clock clock;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private PersonDetailWindow personDetailWindow;
    private JobListPanel jobListPanel;
    private CalendarPanel calendarPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private Person displayedPerson;
    private boolean isExecutingCommand;

    @FXML
    private TabPane recordTabs;
    @FXML
    private Tab candidatesTab;
    @FXML
    private Tab jobsTab;
    @FXML
    private Tab calendarTab;
    @FXML
    private StackPane calendarPanelPlaceholder;
    @FXML
    private StackPane jobListPanelPlaceholder;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        this(primaryStage, logic, dataFilePath, Clock.systemDefaultZone());
    }

    /**
     * Creates a main window using the supplied clock for repeatable calendar views.
     */
    MainWindow(Stage primaryStage, Logic logic, Path dataFilePath, Clock clock) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        this.clock = requireNonNull(clock);

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
        personDetailWindow = new PersonDetailWindow();
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
            if (calendarPanel != null) {
                calendarPanel.start();
            }
        });
        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            if (calendarPanel != null) {
                calendarPanel.stop();
            }
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     *
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and
         * open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        personListPanel.setSelectionListener(person -> {
            if (isExecutingCommand) {
                return;
            }
            displayedPerson = person;
            if (person != null) {
                personDetailWindow.showPerson(person);
            } else {
                personDetailWindow.setPerson(null);
                personDetailWindow.hide();
            }
        });
        // Clearing the selection when the popup closes lets re-clicking the same
        // candidate count as a fresh selection, so the popup reopens.
        personDetailWindow.getRoot().setOnHidden(event -> personListPanel.clearSelection());

        jobListPanel = new JobListPanel(logic.getJobOpeningList());
        jobListPanelPlaceholder.getChildren().add(jobListPanel.getRoot());

        calendarPanel = new CalendarPanel(logic.getEventList(), clock);
        calendarPanelPlaceholder.getChildren().add(calendarPanel.getRoot());
        recordTabs.getSelectionModel().selectedItemProperty().addListener((unused, oldTab, selectedTab) -> {
            if (selectedTab == calendarTab) {
                calendarPanel.refresh();
            }
        });
        if (primaryStage.isShowing()) {
            calendarPanel.start();
        }

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        ObjectBinding<CommandResult.ListView> selectedView = Bindings.createObjectBinding(() -> {
            Tab selected = recordTabs.getSelectionModel().getSelectedItem();
            if (selected == calendarTab) {
                return CommandResult.ListView.CALENDAR;
            }
            return selected == jobsTab ? CommandResult.ListView.JOBS : CommandResult.ListView.CANDIDATES;
        }, recordTabs.getSelectionModel().selectedItemProperty());
        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath,
                logic.getFilteredPersonList(), logic.getJobOpeningList(), selectedView,
                calendarPanel.visibleEventCountProperty());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        personDetailWindow.hide();
        if (calendarPanel != null) {
            calendarPanel.stop();
        }
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    public CalendarPanel getCalendarPanel() {
        return calendarPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.hirebase.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        isExecutingCommand = true;
        try {
            CommandResult commandResult = logic.execute(commandText);

            switch (commandResult.getListView()) {
                case CALENDAR:
                    commandResult.getCalendarViewOptions().ifPresent(calendarPanel::setOptions);
                    calendarPanel.refresh();
                    recordTabs.getSelectionModel().select(calendarTab);
                    break;
                case JOBS:
                    recordTabs.getSelectionModel().select(jobsTab);
                    break;
                case CANDIDATES:
                    recordTabs.getSelectionModel().select(candidatesTab);
                    break;
                default:
                    break;
            }

            String feedback = commandResult.getFeedbackToUser();
            if (commandResult.getListView() == CommandResult.ListView.CALENDAR
                    && commandResult.getAddedEvent().filter(event -> !calendarPanel.isEventVisible(event))
                            .isPresent()) {
                feedback += "\n" + MESSAGE_EVENT_NOT_VISIBLE;
            }
            logger.info("Result: " + feedback);
            resultDisplay.setFeedbackToUser(feedback);
            resultDisplay.setStyleToIndicateSuccess();

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            resultDisplay.setStyleToIndicateError();
            throw e;
        } finally {
            isExecutingCommand = false;
            refreshPersonDetailWindow();
        }
    }

    private void refreshPersonDetailWindow() {
        if (displayedPerson == null || !personDetailWindow.isShowing()) {
            displayedPerson = null;
            return;
        }
        Person currentPerson = logic.getFilteredPersonList().stream()
                .filter(person -> person.getCandidateId().equals(displayedPerson.getCandidateId()))
                .findFirst().orElse(null);
        if (currentPerson == displayedPerson) {
            return;
        }
        displayedPerson = currentPerson;
        personDetailWindow.setPerson(currentPerson);
        if (currentPerson == null) {
            personDetailWindow.hide();
            personListPanel.clearSelection();
        }
    }
}
