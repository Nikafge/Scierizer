package org.example.scierizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.scierizer.domain.Paper;
import org.example.scierizer.domain.Summary;
import org.example.scierizer.infrastructure.arxiv.ArxivClient;
import org.example.scierizer.infrastructure.ollama.OllamaClient;
import org.example.scierizer.infrastructure.pdf.TextExtractor;
import org.example.scierizer.infrastructure.pdf.TextShredder;
import org.example.scierizer.infrastructure.persistence.DBInitializer;
import org.example.scierizer.service.NavigationService;
import org.example.scierizer.service.PaperSearchService;
import org.example.scierizer.service.ReportExportService;
import org.example.scierizer.service.SettingsService;
import org.example.scierizer.service.SummaryService;
import org.example.scierizer.view.MainViewController;
import org.example.scierizer.view.PaperDetailViewController;
import org.example.scierizer.view.SummaryDetailViewController;
import org.example.scierizer.viewmodel.MainViewModel;
import org.example.scierizer.viewmodel.PaperDetailsViewModel;
import org.example.scierizer.viewmodel.SavedContentType;
import org.example.scierizer.viewmodel.SummaryDetailsViewModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.function.Consumer;

public class HelloApplication extends Application {

    private Stage primaryStage;
    ArxivClient arxivClient = new ArxivClient();
    PaperSearchService paperSearchService = new PaperSearchService(arxivClient);
    DBInitializer dbInitializer = new DBInitializer(Path.of(System.getProperty("user.home"), ".scierizer"));
    SettingsService settingsService = new SettingsService(dbInitializer.getAppDbPath());
    ReportExportService reportExportService = new ReportExportService();
    SummaryService summaryService = new SummaryService(new TextExtractor(), new OllamaClient(), new TextShredder(), settingsService);
    private MainViewModel mainViewModel;
    private NavigationService navigationService;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        primaryStage = stage;
        dbInitializer.initialize();
        initializeNavigation();

        showMainView();
        primaryStage.setTitle("Scierizer for arXiv");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (settingsService.loadSettings().storage().clearTemporaryFilesOnExit()) {
            clearTemporaryFiles();
        }
        super.stop();
    }

    private void showMainView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Main.fxml"));
        Parent root = fxmlLoader.load();
        MainViewController controller = fxmlLoader.getController();
        controller.setViewModel(mainViewModel);
        controller.setNavigationService(navigationService);
        controller.setOnPaperSelected(navigationServicePaperSelectionHandler());

        setScenePreservingWindowState(root);
    }

    private void initializeNavigation() {
        mainViewModel = new MainViewModel(paperSearchService);
        navigationService = new NavigationService(primaryStage, mainViewModel, dbInitializer, settingsService);
        navigationService.setOnPaperSelected(navigationServicePaperSelectionHandler());
        navigationService.setOnSummarySelected(navigationServiceSummarySelectionHandler());
    }

    private Consumer<Paper> navigationServicePaperSelectionHandler() {
        return paper -> {
            try {
                showPaperDetailsView(paper);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Consumer<Summary> navigationServiceSummarySelectionHandler() {
        return summary -> {
            try {
                showSummaryDetailsView(summary);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void showPaperDetailsView(Paper paper) throws IOException {
        Scene previousScene = primaryStage.getScene();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("detailedPaper.fxml"));

        Parent root = fxmlLoader.load();

        PaperDetailsViewModel viewModel = new PaperDetailsViewModel(
                paper,
                summaryService,
                dbInitializer,
                settingsService,
                reportExportService
        );

        PaperDetailViewController controller = fxmlLoader.getController();
        controller.setPaperDetailViewModel(viewModel);
        controller.setNavigationService(navigationService);

        controller.getBackButton().setOnAction(event -> {
            if (previousScene != null) {
                setScenePreservingWindowState(previousScene);
                return;
            }
            try {
                navigationService.showHome();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        setScenePreservingWindowState(root);
    }

    private void showSummaryDetailsView(Summary summary) throws IOException {
        Scene previousScene = primaryStage.getScene();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("detailedSummary.fxml"));

        Parent root = fxmlLoader.load();

        SummaryDetailsViewModel viewModel = new SummaryDetailsViewModel(
                summary,
                settingsService,
                reportExportService
        );

        SummaryDetailViewController controller = fxmlLoader.getController();
        controller.setSummaryDetailsViewModel(viewModel);
        controller.setNavigationService(navigationService);

        controller.getBackButton().setOnAction(event -> {
            if (previousScene != null) {
                setScenePreservingWindowState(previousScene);
                return;
            }
            try {
                navigationService.showSavedContent(SavedContentType.SUMMARIES);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        setScenePreservingWindowState(root);
    }

    private void setScenePreservingWindowState(Parent root) {
        Scene currentScene = primaryStage.getScene();
        boolean wasFullScreen = primaryStage.isFullScreen();
        boolean wasMaximized = primaryStage.isMaximized();
        double width = preservedDimension(
                currentScene == null ? 0 : currentScene.getWidth(),
                primaryStage.getWidth(),
                root.prefWidth(-1),
                1120
        );
        double height = preservedDimension(
                currentScene == null ? 0 : currentScene.getHeight(),
                primaryStage.getHeight(),
                root.prefHeight(-1),
                720
        );

        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.setMaximized(wasMaximized);
        primaryStage.setFullScreen(wasFullScreen);
    }

    private void setScenePreservingWindowState(Scene scene) {
        boolean wasFullScreen = primaryStage.isFullScreen();
        boolean wasMaximized = primaryStage.isMaximized();

        primaryStage.setScene(scene);
        primaryStage.setMaximized(wasMaximized);
        primaryStage.setFullScreen(wasFullScreen);
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

    private void clearTemporaryFiles() throws IOException {
        String configuredTemporaryDirectory = settingsService.loadSettings().storage().temporaryDirectory();
        Path temporaryDirectory = configuredTemporaryDirectory == null || configuredTemporaryDirectory.isBlank()
                ? settingsService.getSettingsDirectory().resolve("tmp")
                : Path.of(configuredTemporaryDirectory);

        if (Files.notExists(temporaryDirectory)) {
            return;
        }

        try (var paths = Files.walk(temporaryDirectory)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(temporaryDirectory))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Could not delete temporary file " + path, e);
                        }
                    });
        }
    }
}
