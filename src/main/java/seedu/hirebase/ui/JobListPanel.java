package seedu.hirebase.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.hirebase.model.job.JobOpening;

/**
 * A live list of job cards with permanent IDs.
 */
public class JobListPanel extends UiPart<Region> {
    private static final String FXML = "JobListPanel.fxml";

    @FXML
    private ListView<JobOpening> jobListView;

    /**
     * Creates the job display from the supplied model data.
     */
    public JobListPanel(ObservableList<JobOpening> jobs) {
        super(FXML);
        jobListView.setItems(jobs);
        jobListView.setCellFactory(view -> new JobListCell());
    }

    /**
     * Refreshes both content and index when JavaFX reuses a cell after a deletion or scroll.
     */
    class JobListCell extends ListCell<JobOpening> {
        @Override
        protected void updateItem(JobOpening job, boolean empty) {
            super.updateItem(job, empty);
            updateCard();
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            updateCard();
        }

        private void updateCard() {
            setText(null);
            if (isEmpty() || getItem() == null || getIndex() < 0) {
                setGraphic(null);
            } else {
                Region card = new JobCard(getItem()).getRoot();
                card.prefWidthProperty().bind(widthProperty().subtract(24));
                setGraphic(card);
            }
        }
    }
}
