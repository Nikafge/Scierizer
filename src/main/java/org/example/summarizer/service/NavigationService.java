package org.example.summarizer.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.infrastructure.persistence.DBInitializer;
import org.example.summarizer.view.MainViewController;
import org.example.summarizer.view.SavedContentController;
import org.example.summarizer.view.SettingsController;
import org.example.summarizer.viewmodel.MainViewModel;
import org.example.summarizer.viewmodel.SavedContentType;

import java.io.IOException;
import java.util.function.Consumer;

public class NavigationService {

    private final Stage stage;
    private final MainViewModel mainViewModel;
    private final DBInitializer dbInitializer;
    private Consumer<Paper> onPaperSelected;
//    private final SettingsViewModel settingsViewModel;
//    private final SavedContentViewModel savedContentViewModel;

    public NavigationService(Stage stage, MainViewModel mainViewModel, DBInitializer dbInitializer) {
        this.stage = stage;
        this.mainViewModel = mainViewModel;
        this.dbInitializer = dbInitializer;
    }

    public void setOnPaperSelected(Consumer<Paper> onPaperSelected) {
        this.onPaperSelected = onPaperSelected;
    }

    public void openPaperDetails(Paper paper) {
        if (onPaperSelected != null) {
            onPaperSelected.accept(paper);
        }
    }

    public void showHome() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/Main.fxml"));
        Parent root = fxmlLoader.load();
        MainViewController mainViewController = fxmlLoader.getController();
        mainViewController.setViewModel(mainViewModel);
        mainViewController.setNavigationService(this);
        mainViewController.setOnPaperSelected(onPaperSelected);

        stage.setScene(new Scene(root));
        stage.show();
    }

    public void showSavedContent(SavedContentType savedContentType) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/SavedContent.fxml"));
        Parent root = fxmlLoader.load();
        SavedContentController savedContentController = fxmlLoader.getController();
        savedContentController.setNavigationService(this);
        savedContentController.setContentType(savedContentType, dbInitializer);

        stage.setScene(new Scene(root));
        stage.show();
    }

    public void showSettings() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/Settings.fxml"));
        Parent root = fxmlLoader.load();
        SettingsController settingsController = fxmlLoader.getController();
        settingsController.setNavigationService(this);

        stage.setScene(new Scene(root));
        stage.show();
    }
}
