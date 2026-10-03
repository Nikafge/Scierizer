package org.example.scierizer.service;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.scierizer.AppContext;
import org.example.scierizer.domain.Paper;
import org.example.scierizer.domain.Summary;
import org.example.scierizer.infrastructure.persistence.DBInitializer;
import org.example.scierizer.view.MainViewController;
import org.example.scierizer.view.PaperDetailViewController;
import org.example.scierizer.view.SavedContentController;
import org.example.scierizer.view.SettingsController;
import org.example.scierizer.view.SummaryDetailViewController;
import org.example.scierizer.view.ViewFactory;
import org.example.scierizer.viewmodel.MainViewModel;
import org.example.scierizer.viewmodel.PaperDetailsViewModel;
import org.example.scierizer.viewmodel.SavedContentType;
import org.example.scierizer.viewmodel.SummaryDetailsViewModel;

import java.io.IOException;

public class NavigationService {

    private final Stage stage;
    private final MainViewModel mainViewModel;
    private final DBInitializer dbInitializer;
    private final SettingsService settingsService;
    private final SummaryService summaryService;
    private final ReportExportService reportExportService;
    private final ViewFactory viewFactory;

    public NavigationService(Stage stage, MainViewModel mainViewModel, AppContext appContext) {
        this(
                stage,
                mainViewModel,
                appContext.dbInitializer(),
                appContext.settingsService(),
                appContext.summaryService(),
                appContext.reportExportService(),
                new ViewFactory()
        );
    }

    public NavigationService(
            Stage stage,
            MainViewModel mainViewModel,
            DBInitializer dbInitializer,
            SettingsService settingsService,
            SummaryService summaryService,
            ReportExportService reportExportService,
            ViewFactory viewFactory
    ) {
        this.stage = stage;
        this.mainViewModel = mainViewModel;
        this.dbInitializer = dbInitializer;
        this.settingsService = settingsService;
        this.summaryService = summaryService;
        this.reportExportService = reportExportService;
        this.viewFactory = viewFactory;
    }

    public void openPaperDetails(Paper paper) {
        try {
            showPaperDetailsView(paper);
        } catch (IOException e) {
            throw new RuntimeException("Could not open paper details", e);
        }
    }

    public void openSummaryDetails(Summary summary) {
        try {
            showSummaryDetailsView(summary);
        } catch (IOException e) {
            throw new RuntimeException("Could not open summary details", e);
        }
    }

    public void showHome() throws IOException {
        showMainView();
    }

    public void showMainView() throws IOException {
        ViewFactory.LoadedView<MainViewController> view = viewFactory.load("Main.fxml");
        MainViewController controller = view.controller();
        controller.setViewModel(mainViewModel);
        controller.setNavigationService(this);
        controller.setOnPaperSelected(this::openPaperDetails);

        setScenePreservingWindowState(view.root());
    }

    public void showPaperDetailsView(Paper paper) throws IOException {
        Scene previousScene = stage.getScene();
        ViewFactory.LoadedView<PaperDetailViewController> view = viewFactory.load("detailedPaper.fxml");
        PaperDetailsViewModel viewModel = new PaperDetailsViewModel(
                paper,
                summaryService,
                dbInitializer,
                settingsService,
                reportExportService
        );
        PaperDetailViewController controller = view.controller();
        controller.setPaperDetailViewModel(viewModel);
        controller.setNavigationService(this);
        controller.getBackButton().setOnAction(event -> showPreviousSceneOrHome(previousScene));

        setScenePreservingWindowState(view.root());
    }

    public void showSummaryDetailsView(Summary summary) throws IOException {
        Scene previousScene = stage.getScene();
        ViewFactory.LoadedView<SummaryDetailViewController> view = viewFactory.load("detailedSummary.fxml");
        SummaryDetailsViewModel viewModel = new SummaryDetailsViewModel(
                summary,
                settingsService,
                reportExportService
        );
        SummaryDetailViewController controller = view.controller();
        controller.setSummaryDetailsViewModel(viewModel);
        controller.setNavigationService(this);
        controller.getBackButton().setOnAction(event -> showPreviousSceneOrSavedSummaries(previousScene));

        setScenePreservingWindowState(view.root());
    }

    public void showSavedContent(SavedContentType savedContentType) throws IOException {
        ViewFactory.LoadedView<SavedContentController> view = viewFactory.load("SavedContent.fxml");
        SavedContentController savedContentController = view.controller();
        savedContentController.setNavigationService(this);
        savedContentController.setContentType(savedContentType, dbInitializer);

        setScenePreservingWindowState(view.root());
    }

    public void showSettings() throws IOException {
        ViewFactory.LoadedView<SettingsController> view = viewFactory.load("Settings.fxml");
        SettingsController settingsController = view.controller();
        settingsController.setNavigationService(this);
        settingsController.setSettingsService(settingsService);

        setScenePreservingWindowState(view.root());
    }

    private void showPreviousSceneOrHome(Scene previousScene) {
        if (previousScene != null) {
            setScenePreservingWindowState(previousScene);
            return;
        }
        try {
            showHome();
        } catch (IOException e) {
            throw new RuntimeException("Could not return to the home screen", e);
        }
    }

    private void showPreviousSceneOrSavedSummaries(Scene previousScene) {
        if (previousScene != null) {
            setScenePreservingWindowState(previousScene);
            return;
        }
        try {
            showSavedContent(SavedContentType.SUMMARIES);
        } catch (IOException e) {
            throw new RuntimeException("Could not return to saved summaries", e);
        }
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

    private void setScenePreservingWindowState(Scene scene) {
        boolean wasFullScreen = stage.isFullScreen();
        boolean wasMaximized = stage.isMaximized();

        stage.setScene(scene);
        stage.setMaximized(wasMaximized);
        stage.setFullScreen(wasFullScreen);
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
