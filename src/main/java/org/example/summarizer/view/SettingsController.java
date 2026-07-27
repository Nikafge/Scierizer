package org.example.summarizer.view;

import javafx.fxml.FXML;
import org.example.summarizer.service.NavigationService;

public class SettingsController {

    @FXML
    private TopNavigationController topNavigationController;

    public void setNavigationService(NavigationService navigationService) {
        topNavigationController.setNavigationService(navigationService);
    }
}
