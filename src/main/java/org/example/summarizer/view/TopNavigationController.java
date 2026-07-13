package org.example.summarizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.summarizer.service.NavigationService;
import org.example.summarizer.viewmodel.SavedContentType;

import java.io.IOException;

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
            initialize();
        }

        @FXML
        public void initialize() {
            homeButton.setOnAction(event ->
                    {
                        try {
                            navigationService.showHome();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            savedSummariesButton.setOnAction(event ->
                    {
                        try {
                            navigationService.showSavedContent(
                                    SavedContentType.SUMMARIES
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            savedArticlesButton.setOnAction(event ->
                    {
                        try {
                            navigationService.showSavedContent(
                                    SavedContentType.ARTICLES
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            settingsButton.setOnAction(event ->
                    {
                        try {
                            navigationService.showSettings();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

    }