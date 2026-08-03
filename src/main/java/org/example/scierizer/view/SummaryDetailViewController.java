package org.example.scierizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.scierizer.service.NavigationService;
import org.example.scierizer.viewmodel.SummaryDetailsViewModel;

public class SummaryDetailViewController {

    private SummaryDetailsViewModel summaryDetailsViewModel;

    @FXML
    private TopNavigationController topNavigationController;

    @FXML
    private Button backButton;

    @FXML
    private Label titleLabel;

    @FXML
    private TextField summaryTypeField;

    @FXML
    private TextField publishedField;

    @FXML
    private TextField sourceField;

    @FXML
    private TextArea summaryArea;

    @FXML
    private Button exportSummaryButton;

    @FXML
    private Label exportStatusLabel;

    @FXML
    private void initialize() {
        summaryTypeField.setEditable(false);
        publishedField.setEditable(false);
        sourceField.setEditable(false);
        summaryArea.setEditable(false);
        summaryArea.setWrapText(true);
    }

    public void setSummaryDetailsViewModel(SummaryDetailsViewModel summaryDetailsViewModel) {
        this.summaryDetailsViewModel = summaryDetailsViewModel;
        bindSummaryDetailsViewModel();
    }

    private void bindSummaryDetailsViewModel() {
        titleLabel.textProperty().bind(summaryDetailsViewModel.PaperTitle());
        summaryTypeField.textProperty().bind(summaryDetailsViewModel.summaryType());
        publishedField.textProperty().bind(summaryDetailsViewModel.datePublished());
        sourceField.textProperty().bind(summaryDetailsViewModel.sourcePdfLink());
        summaryArea.textProperty().bind(summaryDetailsViewModel.summary());
        exportStatusLabel.textProperty().bind(summaryDetailsViewModel.exportStatus());
        exportSummaryButton.setOnAction(event -> summaryDetailsViewModel.exportSummary());
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setNavigationService(NavigationService navigationService) {
        topNavigationController.setNavigationService(navigationService);
    }
}
