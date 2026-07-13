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

import java.io.IOException;

public class NavigationService {

    private final Stage stage;

    public NavigationService(Stage stage) {
        this.stage = stage;
    }

    public void showHome(MainViewModel mainViewModel) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/Main.fxml"));
        Parent root = fxmlLoader.load();
        MainViewController mainViewController = fxmlLoader.getController();
        mainViewController.setViewModel(mainViewModel);

        stage.setScene(new Scene(root));
        stage.show();
    }

    public void showSavedContent(SavedContentType savedContentType, PaperDetailsViewModel paperDetailsViewModel) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/SavedContent.fxml"));
        Parent root = fxmlLoader.load();

        if (savedContentType.getDisplayType().equals("articles")) {

        }

        PaperDetailViewController paperDetailViewController = fxmlLoader.getController();
        paperDetailViewController.setPaperDetailViewModel(paperDetailsViewModel);

        stage.setScene(new Scene(root));
        stage.show();
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
    public void showSettings() {

    }
}