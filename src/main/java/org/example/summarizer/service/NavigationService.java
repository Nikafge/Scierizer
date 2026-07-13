package org.example.summarizer.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.summarizer.view.MainViewController;
import org.example.summarizer.view.PaperDetailViewController;
import org.example.summarizer.viewmodel.MainViewModel;
import org.example.summarizer.viewmodel.PaperDetailsViewModel;
import org.example.summarizer.viewmodel.SavedContentType;
import org.example.summarizer.viewmodel.SavedContentViewModel;

import java.io.IOException;

public class NavigationService {

    private final Stage stage;
    private final MainViewModel mainViewModel;
//    private final SettingsViewModel settingsViewModel;
//    private final SavedContentViewModel savedContentViewModel;

    public NavigationService(Stage stage, MainViewModel mainViewModel) {
        this.stage = stage;
        this.mainViewModel = mainViewModel;
    }

    public void showHome() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/Main.fxml"));
        Parent root = fxmlLoader.load();
        MainViewController mainViewController = fxmlLoader.getController();
        mainViewController.setViewModel(mainViewModel);
        mainViewController.setNavigationService(this);

        stage.setScene(new Scene(root));
        stage.show();
    }

    public void showSavedContent(SavedContentType savedContentType) throws IOException {

//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/SavedContent.fxml"));
//        Parent root = fxmlLoader.load();
//
//        if (savedContentType.getDisplayType().equals("articles")) {
//
//        }
//
//        PaperDetailViewController paperDetailViewController = fxmlLoader.getController();
//        paperDetailViewController.setPaperDetailViewModel(mainViewModel);
//
//        stage.setScene(new Scene(root));
//        stage.show();
    }

    //    public void showSavedSummary(PaperDetailsViewModel paperDetailsViewModel) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/Main.fxml"));
//        Parent root = fxmlLoader.load();
//        PaperDetailViewController paperDetailViewController = fxmlLoader.getController();
//        paperDetailViewController.setPaperDetailViewModel(paperDetailsViewModel);
//
//        stage.setScene(new Scene(root));
//        stage.show();
//    }
    public void showSettings() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/Main.fxml"));
        Parent root = fxmlLoader.load();
        MainViewController mainViewController = fxmlLoader.getController();
        mainViewController.setViewModel(mainViewModel);
        mainViewController.setNavigationService(this);

        stage.setScene(new Scene(root));
        stage.show();
    }
}