package seedu.hirebase;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.commons.util.StringUtil;
import seedu.hirebase.logic.Logic;
import seedu.hirebase.logic.LogicManager;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.ReadOnlyUserPrefs;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.util.SampleDataUtil;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.Storage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.ui.Ui;
import seedu.hirebase.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.5.1";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path HIRE_BASE_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing HireBase ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonHireBaseStorage hireBaseStorage = new JsonHireBaseStorage(HIRE_BASE_FILE_PATH);
        storage = new StorageManager(hireBaseStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getHireBaseFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address
     * book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if
     * {@code storage}'s address book is not found,
     * or an empty address book will be used instead if errors occur when reading
     * {@code storage}'s address book.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getHireBaseFilePath());

        Optional<ReadOnlyHireBase> hireBaseOptional;
        ReadOnlyHireBase initialData;
        try {
            hireBaseOptional = storage.readHireBase();
            if (hireBaseOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getHireBaseFilePath()
                        + " populated with a sample HireBase.");
            }
            initialData = hireBaseOptional.orElseGet(SampleDataUtil::getSampleHireBase);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getHireBaseFilePath() + " could not be loaded."
                    + " Will be starting with an empty HireBase.");
            initialData = new HireBase();
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs
     * file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        // Update prefs file in case it was missing to begin with or there are
        // new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting HireBase " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping HireBase ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
