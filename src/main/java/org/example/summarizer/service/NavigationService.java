package org.example.summarizer.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.domain.Summary;
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
    private final SettingsService settingsService;
    private Consumer<Paper> onPaperSelected;
    private Consumer<Summary> onSummarySelected;

    public NavigationService(Stage stage, MainViewModel mainViewModel, DBInitializer dbInitializer) {
        this(stage, mainViewModel, dbInitializer, new SettingsService(dbInitializer.getAppDbPath()));
    }

    public NavigationService(
            Stage stage,
            MainViewModel mainViewModel,
            DBInitializer dbInitializer,
            SettingsService settingsService
    ) {
        this.stage = stage;
        this.mainViewModel = mainViewModel;
        this.dbInitializer = dbInitializer;
        this.settingsService = settingsService;
    }

    public void setOnPaperSelected(Consumer<Paper> onPaperSelected) {
        this.onPaperSelected = onPaperSelected;
    }

    public void setOnSummarySelected(Consumer<Summary> onSummarySelected) {
        this.onSummarySelected = onSummarySelected;
    }

    public void openPaperDetails(Paper paper) {
        if (onPaperSelected != null) {
            onPaperSelected.accept(paper);
        }
    }

    public void openSummaryDetails(Summary summary) {
        if (onSummarySelected != null) {
            onSummarySelected.accept(summary);
        }
    }

    public void showHome() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/Main.fxml"));
        Parent root = fxmlLoader.load();
        MainViewController mainViewController = fxmlLoader.getController();
        mainViewController.setViewModel(mainViewModel);
        mainViewController.setNavigationService(this);
        mainViewController.setOnPaperSelected(onPaperSelected);

        setScenePreservingWindowState(root);
    }

    public void showSavedContent(SavedContentType savedContentType) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/SavedContent.fxml"));
        Parent root = fxmlLoader.load();
        SavedContentController savedContentController = fxmlLoader.getController();
        savedContentController.setNavigationService(this);
        savedContentController.setContentType(savedContentType, dbInitializer);

        setScenePreservingWindowState(root);
    }

    public void showSettings() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/summarizer/Settings.fxml"));
        Parent root = fxmlLoader.load();
        SettingsController settingsController = fxmlLoader.getController();
        settingsController.setNavigationService(this);
        settingsController.setSettingsService(settingsService);

        setScenePreservingWindowState(root);
    }

    private void setScenePreservingWindowState(Parent root) {
        Scene currentScene = stage.getScene();
        boolean wasFullScreen = stage.isFullScreen();
        boolean wasMaximized = stage.isMaximized();
        double width = preservedDimension(
                currentScene == null ? 0 : currentScene.getWidth(),
                stage.getWidth(),
                root.prefWidth(-1),
                1120
        );
        double height = preservedDimension(
                currentScene == null ? 0 : currentScene.getHeight(),
                stage.getHeight(),
                root.prefHeight(-1),
                720
        );

        stage.setScene(new Scene(root, width, height));
        stage.setMaximized(wasMaximized);
        stage.setFullScreen(wasFullScreen);
        stage.show();
    }

    private double preservedDimension(
            double currentSceneDimension,
            double stageDimension,
            double rootPreferredDimension,
            double fallback
    ) {
        if (isUsableDimension(currentSceneDimension)) {
            return currentSceneDimension;
        }
        if (isUsableDimension(stageDimension)) {
            return stageDimension;
        }
        if (isUsableDimension(rootPreferredDimension)) {
            return rootPreferredDimension;
        }
        return fallback;
    }

    private boolean isUsableDimension(double dimension) {
        return !Double.isNaN(dimension) && dimension > 0;
    }
}
