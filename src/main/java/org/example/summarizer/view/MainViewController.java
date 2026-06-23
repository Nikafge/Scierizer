package org.example.summarizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.summarizer.viewmodel.Category;
import org.example.summarizer.viewmodel.MainViewModel;
import org.example.summarizer.viewmodel.PaperViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainViewController {

    private MainViewModel mainViewModel;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

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
        initializeCategoryButtons();    //initialize categories
        bindViewModel();                //fields binding
    }


    private void initializeCategoryButtons() {
        List<ToggleButton> categoryButtons = List.of(physicsButton, mathButton, compSciButton,
                quantitativeFinanceButton, quantitativeBiologyButton, statisticsButton, engineeringButton, economicsButton);
        categoryButtons.forEach(button -> button.setToggleGroup(toggleGroup));

        Map<ToggleButton, Category> map = new HashMap<>();
        map.forEach((button, category) -> ToggleButton.setUserData(Category.values()));


// TO DO LIST
        physicsButton.setUserData(Category.Physics);
        mathButton.setUserData(Category.Mathematics);
        compSciButton.setUserData(Category.Computer_Science);
        quantitativeFinanceButton.setUserData(Category.Quantitative_Finance);
        quantitativeBiologyButton.setUserData(Category.Quantitative_Biology);
        statisticsButton.setUserData(Category.Statistics);
        engineeringButton.setUserData(Category.Engineering);
        economicsButton.setUserData(Category.Economics);

    }

    private void bindViewModel() {

        searchField.textProperty().bindBidirectional(mainViewModel.searchQueryProperty());
        searchButton.disableProperty().bind(mainViewModel.loading());
        paperResultsListView.setItems(mainViewModel.papers());

        errorLabel.textProperty().bind(mainViewModel.errorMessage());
        searchButton.setOnAction(event -> mainViewModel.search());

        toggleGroup.selectedToggleProperty().addListener(
                (observable, oldToggle, newToggle) -> {
                    if (newToggle == null) {
                        mainViewModel.chosenCategoryProperty().set(null);
                        return;
                    }
                    Category chosenCategory = (Category)newToggle.getUserData();
                    mainViewModel.chosenCategoryProperty().set(chosenCategory);
        });

    }
}
