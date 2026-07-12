package org.example.summarizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.summarizer.service.NavigationService;
import org.example.summarizer.viewmodel.SavedContentType;
//import org.example.summarizer.view.NavigationService;
//import org.example.summarizer.view.SavedContentType;

public class TopNavigationController {

    @FXML
    private Button homeButton;

    @FXML
    private Button savedSummariesButton;

    @FXML
    private Button savedArticlesButton;

    @FXML
    private Button settingsButton;

    private NavigationService navigationService;

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;

        homeButton.setOnAction(event ->
                navigationService.showHome()
        );

        savedSummariesButton.setOnAction(event ->
                navigationService.showSavedContent(
                        SavedContentType.SUMMARIES
                )
        );

        savedArticlesButton.setOnAction(event ->
                navigationService.showSavedContent(
                        SavedContentType.ARTICLES
                )
        );

        settingsButton.setOnAction(event ->
                navigationService.showSettings()
        );
    }
}