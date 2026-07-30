package org.example.summarizer.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import org.example.summarizer.infrastructure.persistence.DBInitializer;
import org.example.summarizer.infrastructure.persistence.SQLitePaperRepository;
import org.example.summarizer.infrastructure.persistence.SQLiteSummaryRepository;
import org.example.summarizer.service.NavigationService;
import org.example.summarizer.viewmodel.PaperViewModel;
import org.example.summarizer.viewmodel.SavedContentType;
import org.example.summarizer.viewmodel.SavedListItem;
import org.example.summarizer.viewmodel.SummaryViewModel;

import java.util.List;

public class SavedContentController {

    private final ObservableList<SavedListItem> savedItems = FXCollections.observableArrayList();
    private NavigationService navigationService;
    private SavedContentType savedContentType;
    private DBInitializer dbInitializer;

    @FXML
    private TopNavigationController topNavigationController;

    @FXML
    private Label titleLabel;

    @FXML
    private ListView<SavedListItem> savedItemsListView;

    @FXML
    private Label emptyLabel;

    @FXML
    private Button deleteSelectedButton;

    @FXML
    private void initialize() {
        savedItemsListView.setItems(savedItems);
        savedItemsListView.setCellFactory(listView -> {
            ListCell<SavedListItem> cell = new ListCell<>() {
                @Override
                protected void updateItem(SavedListItem item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.displayTitle() + "\n" + item.displaySubtitle());
                }
            };
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2
                        && !cell.isEmpty()
                        && savedContentType == SavedContentType.ARTICLES
                        && navigationService != null
                        && cell.getItem() instanceof PaperViewModel paperViewModel) {
                    navigationService.openPaperDetails(paperViewModel.getPaper());
                }
                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2
                        && !cell.isEmpty()
                        && savedContentType == SavedContentType.SUMMARIES
                        && navigationService != null
                        && cell.getItem() instanceof SummaryViewModel summaryViewModel) {
                    navigationService.openSummaryDetails(summaryViewModel.getSummary());
                }
            });
            return cell;
        });
        deleteSelectedButton.disableProperty().bind(
                savedItemsListView.getSelectionModel().selectedItemProperty().isNull()
        );
        deleteSelectedButton.setOnAction(event -> deleteSelectedItem());
    }

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
        topNavigationController.setNavigationService(navigationService);
    }

    public void setContentType(SavedContentType savedContentType, DBInitializer dbInitializer) {
        this.savedContentType = savedContentType;
        this.dbInitializer = dbInitializer;
        titleLabel.setText(savedContentType == SavedContentType.ARTICLES ? "Saved Articles" : "Saved Summaries");
        reloadItems();
    }

    private List<? extends SavedListItem> loadItems(SavedContentType savedContentType, DBInitializer dbInitializer) {
        if (savedContentType == SavedContentType.ARTICLES) {
            SQLitePaperRepository paperRepository = new SQLitePaperRepository(dbInitializer);
            return paperRepository.findAll().stream()
                    .map(PaperViewModel::new)
                    .toList();
        }

        SQLiteSummaryRepository summaryRepository = new SQLiteSummaryRepository(dbInitializer);
        return summaryRepository.findAll().stream()
                .map(SummaryViewModel::new)
                .toList();
    }

    private void deleteSelectedItem() {
        SavedListItem selectedItem = savedItemsListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || dbInitializer == null) {
            return;
        }

        if (selectedItem instanceof PaperViewModel paperViewModel) {
            new SQLitePaperRepository(dbInitializer).deleteById(paperViewModel.getPaper().id());
        } else if (selectedItem instanceof SummaryViewModel summaryViewModel) {
            new SQLiteSummaryRepository(dbInitializer).deleteById(summaryViewModel.getSummary().id());
        }

        reloadItems();
    }

    private void reloadItems() {
        savedItems.setAll(loadItems(savedContentType, dbInitializer));
        boolean hasNoItems = savedItems.isEmpty();
        emptyLabel.setVisible(hasNoItems);
        emptyLabel.setManaged(hasNoItems);
    }
}
