package org.example.summarizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.summarizer.service.NavigationService;
import org.example.summarizer.viewmodel.MainViewModel;
import org.example.summarizer.viewmodel.SavedContentType;

import java.io.IOException;
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
    private final MainViewModel mainViewModel;

    public TopNavigationController(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;

        homeButton.setOnAction(event ->
                {
                    try {
                        navigationService.showHome(mainViewModel);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

//        savedSummariesButton.setOnAction(event ->
//                navigationService.showSavedContent(
//                        SavedContentType.SUMMARIES
//                )
//        );
//
//        savedArticlesButton.setOnAction(event ->
//                navigationService.showSavedPaper(
//                        SavedContentType.ARTICLES, //Do dis
//                )
//        );

        settingsButton.setOnAction(event ->
                navigationService.showSettings()
        );
    }
}