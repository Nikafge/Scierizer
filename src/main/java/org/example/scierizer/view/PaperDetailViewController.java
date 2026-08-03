package org.example.scierizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.scierizer.service.NavigationService;
import org.example.scierizer.viewmodel.PaperDetailsViewModel;
import org.example.scierizer.viewmodel.SummaryType;

public class PaperDetailViewController {

    private PaperDetailsViewModel paperDetailsViewModel;
    private final ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private TopNavigationController topNavigationController;

    //Navigation
    @FXML
    private Button backButton;

    //Paper metadata
    @FXML
    private Label titleLabel;
    @FXML
    private TextField authorsField;
    @FXML
    private TextField publishedField;
    @FXML
    private TextField updatedField;
    @FXML
    private TextField sourceField;
    @FXML
    private TextArea abstractArea;

    //Summary controls
    @FXML
    private ToggleButton structured;
    @FXML
    private ToggleButton tldr;
    @FXML
    private ToggleButton executive;
    @FXML
    private ToggleButton research;
    @FXML
    private Label errorLabel;

    //Summaries
    @FXML
    private TextArea generatedSummary;

    //Action elements
    @FXML
    private Button savePaperButton;
    @FXML
    private Button generateSummaryButton;
    @FXML
    private Button saveSummary;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private void initialize() {
        //configure staff
        configureReadOnlyFields();
        configureSummaryTypes();
    }

    private void configureSummaryTypes() {
        structured.setToggleGroup(toggleGroup);
        tldr.setToggleGroup(toggleGroup);
        executive.setToggleGroup(toggleGroup);
        research.setToggleGroup(toggleGroup);

        structured.setUserData(SummaryType.STRUCTURED);
        tldr.setUserData(SummaryType.TLDR);
        executive.setUserData(SummaryType.EXECUTIVE);
        research.setUserData(SummaryType.RESEARCH_NOTE);

        toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (paperDetailsViewModel == null) {
                return;
            }
            if (newToggle == null) {
                paperDetailsViewModel.selectedSummaryTypeProperty().set(null);
                return;
            }
            SummaryType selectedSummaryType = (SummaryType) newToggle.getUserData();
            paperDetailsViewModel.selectedSummaryTypeProperty().set(selectedSummaryType);

        });

    }

    private void configureReadOnlyFields() {
        authorsField.setEditable(false);
        publishedField.setEditable(false);
        updatedField.setEditable(false);
        sourceField.setEditable(false);
        abstractArea.setEditable(false);
        abstractArea.setWrapText(true);
        generatedSummary.setEditable(false);
        generatedSummary.setWrapText(true);
    }

    public void setPaperDetailViewModel(PaperDetailsViewModel paperDetailsViewModel) {
        this.paperDetailsViewModel = paperDetailsViewModel;
        bindPaperViewModel();
        selectSummaryType(paperDetailsViewModel.selectedSummaryTypeProperty().get());
        syncSelectedSummaryTypeWithViewModel();
    }

    private void bindPaperViewModel() {
        titleLabel.textProperty().bind(paperDetailsViewModel.titleProperty());
        authorsField.textProperty().bind(paperDetailsViewModel.authorsProperty());
        publishedField.textProperty().bind(paperDetailsViewModel.publishedDateProperty());
        updatedField.textProperty().bind(paperDetailsViewModel.updatedDateProperty());
        sourceField.textProperty().bind(paperDetailsViewModel.pdfLinkProperty());
        abstractArea.textProperty().bind(paperDetailsViewModel.abstractTextProperty());

        generatedSummary.textProperty().bind(paperDetailsViewModel.generatedSummaryProperty());

        generateSummaryButton.disableProperty().bind(
                paperDetailsViewModel.isLoadingProperty()
        );

        saveSummary.disableProperty().bind(
                paperDetailsViewModel.generatedSummaryProperty().isEmpty()
                        .or(paperDetailsViewModel.isLoadingProperty())
        );

        progressIndicator.visibleProperty().bind(
                paperDetailsViewModel.isLoadingProperty()
        );

        progressIndicator.managedProperty().bind(
                progressIndicator.visibleProperty()
        );

        errorLabel.textProperty().bind(paperDetailsViewModel.errorMessageProperty());

        errorLabel.visibleProperty().bind(
                paperDetailsViewModel.errorMessageProperty().isNotEmpty()
        );

        errorLabel.managedProperty().bind(
                errorLabel.visibleProperty()
        );

        generateSummaryButton.setOnAction(event -> paperDetailsViewModel.generateSummary());

        savePaperButton.setOnAction(event -> paperDetailsViewModel.savePaper());

        saveSummary.setOnAction(event -> paperDetailsViewModel.saveSummary());
    }

    private void syncSelectedSummaryTypeWithViewModel() {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();

        if (selectedToggle != null) {
            SummaryType selectedSummaryType = (SummaryType) selectedToggle.getUserData();
            paperDetailsViewModel.selectedSummaryTypeProperty().set(selectedSummaryType);
        }
    }

    private void selectSummaryType(SummaryType summaryType) {
        if (summaryType == null) {
            toggleGroup.selectToggle(null);
            return;
        }

        for (Toggle toggle : toggleGroup.getToggles()) {
            if (summaryType == toggle.getUserData()) {
                toggleGroup.selectToggle(toggle);
                return;
            }
        }
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setNavigationService(NavigationService navigationService) {
        topNavigationController.setNavigationService(navigationService);
    }

}
