package org.example.summarizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.summarizer.service.NavigationService;
import org.example.summarizer.viewmodel.SavedContentType;

import java.io.IOException;
import java.util.Objects;

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
        this.navigationService = Objects.requireNonNull(navigationService);
        setNavigationButtonsDisabled(false);
    }

    @FXML
    private void initialize() {
        setNavigationButtonsDisabled(true);

        homeButton.setOnAction(event -> navigate(() -> navigationService.showHome()));
        savedSummariesButton.setOnAction(event -> navigate(
                () -> navigationService.showSavedContent(SavedContentType.SUMMARIES)
        ));
        savedArticlesButton.setOnAction(event -> navigate(
                () -> navigationService.showSavedContent(SavedContentType.ARTICLES)
        ));
        settingsButton.setOnAction(event -> navigate(() -> navigationService.showSettings()));
    }

    private void navigate(NavigationAction navigationAction) {
        if (navigationService == null) {
            return;
        }

        try {
            navigationAction.execute();
        } catch (IOException e) {
            throw new RuntimeException("Could not navigate", e);
        }
    }

    private void setNavigationButtonsDisabled(boolean disabled) {
        homeButton.setDisable(disabled);
        savedSummariesButton.setDisable(disabled);
        savedArticlesButton.setDisable(disabled);
        settingsButton.setDisable(disabled);
    }

    @FunctionalInterface
    private interface NavigationAction {
        void execute() throws IOException;
    }
}
