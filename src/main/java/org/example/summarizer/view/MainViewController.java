package org.example.summarizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.service.NavigationService;
import org.example.summarizer.viewmodel.Category;
import org.example.summarizer.viewmodel.MainViewModel;
import org.example.summarizer.viewmodel.PaperViewModel;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MainViewController {

    private MainViewModel mainViewModel;
    private NavigationService navigationService;
    private final ToggleGroup toggleGroup = new ToggleGroup();

    private Consumer<Paper> onPaperSelected;

    @FXML
    private TopNavigationController topNavigationController;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;
    @FXML
    private Button uploadPdfButton;
    @FXML
    private Button loadMoreButton;

    @FXML
    private ListView<PaperViewModel> paperResultsListView;

    @FXML
    private Label errorLabel;

    @FXML
    private ToggleButton physicsButton;
    @FXML
    private ToggleButton mathButton;
    @FXML
    private ToggleButton compSciButton;
    @FXML
    private ToggleButton quantitativeBiologyButton;
    @FXML
    private ToggleButton quantitativeFinanceButton;
    @FXML
    private ToggleButton statisticsButton;
    @FXML
    private ToggleButton engineeringButton;
    @FXML
    private ToggleButton economicsButton;



    public void setViewModel (MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
        configurePaperResultsView();
        initializeCategoryButtons();    //initialize categories
        bindViewModel();                //fields binding
    }



    private void initializeCategoryButtons() {
        Map<ToggleButton, Category> map = Map.of(
                physicsButton, Category.PHYSICS,
                mathButton, Category.MATHEMATICS,
                compSciButton, Category.COMPUTER_SCIENCE,
                quantitativeBiologyButton, Category.QUANTITATIVE_BIOLOGY,
                quantitativeFinanceButton, Category.QUANTITATIVE_FINANCE,
                statisticsButton, Category.STATISTICS,
                engineeringButton, Category.ENGINEERING,
                economicsButton, Category.ECONOMICS
        );

        map.forEach((button, category) -> {
                    button.setToggleGroup(toggleGroup);
                    button.setUserData(category);
                });

    }

    private void bindViewModel() {

        searchField.textProperty().bindBidirectional(mainViewModel.searchQueryProperty());
        searchButton.disableProperty().bind(mainViewModel.loading());
        loadMoreButton.visibleProperty().bind(mainViewModel.loadMoreVisible());
        loadMoreButton.managedProperty().bind(mainViewModel.loadMoreVisible());
        loadMoreButton.disableProperty().bind(mainViewModel.loading().or(mainViewModel.loadMoreAvailable().not()));
        paperResultsListView.setItems(mainViewModel.papers());

        errorLabel.textProperty().bind(mainViewModel.errorMessage());
        searchButton.setOnAction(event -> mainViewModel.search());
        uploadPdfButton.setOnAction(event -> uploadPdf());
        loadMoreButton.setOnAction(event -> mainViewModel.loadMore());

        toggleGroup.selectedToggleProperty().addListener(
                (observable, oldToggle, newToggle) -> {
                    if (newToggle == null) {
                        mainViewModel.chosenCategoryProperty().set(null);
                        return;
                    }
                    Category chosenCategory = (Category)newToggle.getUserData();
                    mainViewModel.chosenCategoryProperty().set(chosenCategory);
        });
        paperResultsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PaperViewModel selectedPaperViewModel =
                        paperResultsListView.getSelectionModel().getSelectedItem();

                if (selectedPaperViewModel != null && onPaperSelected != null) {
                    onPaperSelected.accept(selectedPaperViewModel.getPaper());
                }
            }
        });
    }

    private void uploadPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose PDF file");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadPdfButton.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        Paper uploadedPaper = mainViewModel.createLocalPdfPaper(selectedFile.toPath());
        if (onPaperSelected != null) {
            onPaperSelected.accept(uploadedPaper);
        }
    }

    private void configurePaperResultsView() {
        paperResultsListView.setCellFactory(lv -> new ListCell<PaperViewModel>() {
            @Override
            protected void updateItem(PaperViewModel paper, boolean empty) {
                super.updateItem(paper, empty);
                if (empty || paper == null) {
                    setText(null);
                } else {
                    setText(paper.getPaper().title());
                }
            }

        });
    }

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
        topNavigationController.setNavigationService(navigationService);
    }

    public void setOnPaperSelected(Consumer<Paper> onPaperSelected) {
        this.onPaperSelected = onPaperSelected;
    }

}
