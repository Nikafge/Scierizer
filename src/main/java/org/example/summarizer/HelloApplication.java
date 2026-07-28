package org.example.summarizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.domain.Summary;
import org.example.summarizer.infrastructure.arxiv.ArxivClient;
import org.example.summarizer.infrastructure.ollama.OllamaClient;
import org.example.summarizer.infrastructure.pdf.TextExtractor;
import org.example.summarizer.infrastructure.pdf.TextShredder;
import org.example.summarizer.infrastructure.persistence.DBInitializer;
import org.example.summarizer.service.NavigationService;
import org.example.summarizer.service.PaperSearchService;
import org.example.summarizer.service.SummaryService;
import org.example.summarizer.view.MainViewController;
import org.example.summarizer.view.PaperDetailViewController;
import org.example.summarizer.view.SummaryDetailViewController;
import org.example.summarizer.viewmodel.MainViewModel;
import org.example.summarizer.viewmodel.PaperDetailsViewModel;
import org.example.summarizer.viewmodel.SavedContentType;
import org.example.summarizer.viewmodel.SummaryDetailsViewModel;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.function.Consumer;

public class HelloApplication extends Application {

    private Stage primaryStage;
    ArxivClient arxivClient = new ArxivClient();
    PaperSearchService paperSearchService = new PaperSearchService(arxivClient);
    SummaryService summaryService = new SummaryService(new TextExtractor(), new OllamaClient(), new TextShredder());
    DBInitializer dbInitializer = new DBInitializer(Path.of(System.getProperty("user.home"), ".summarizer"));
    private MainViewModel mainViewModel;
    private NavigationService navigationService;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        primaryStage = stage;
        dbInitializer.initialize();
        initializeNavigation();

        showMainView();
        primaryStage.setTitle("Arxiv Summarizer");
        primaryStage.show();
    }

    private void showMainView() throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Main.fxml"));
        Parent root = fxmlLoader.load();
        MainViewController controller = fxmlLoader.getController();
        controller.setViewModel(mainViewModel);
        controller.setNavigationService(navigationService);
        controller.setOnPaperSelected(navigationServicePaperSelectionHandler());

        primaryStage.setScene(new Scene(root));
    }

    private void initializeNavigation() {
        mainViewModel = new MainViewModel(paperSearchService);
        navigationService = new NavigationService(primaryStage, mainViewModel, dbInitializer);
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

        PaperDetailsViewModel viewModel = new PaperDetailsViewModel(paper, summaryService, dbInitializer);

        PaperDetailViewController controller = fxmlLoader.getController();
        controller.setPaperDetailViewModel(viewModel);
        controller.setNavigationService(navigationService);

        controller.getBackButton().setOnAction(event -> {
            if (previousScene != null) {
                primaryStage.setScene(previousScene);
                return;
            }
            try {
                navigationService.showHome();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        primaryStage.setScene(new Scene(root));

    }

    private void showSummaryDetailsView(Summary summary) throws IOException {
        Scene previousScene = primaryStage.getScene();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("detailedSummary.fxml"));

        Parent root = fxmlLoader.load();

        SummaryDetailsViewModel viewModel = new SummaryDetailsViewModel(summary);

        SummaryDetailViewController controller = fxmlLoader.getController();
        controller.setSummaryDetailsViewModel(viewModel);
        controller.setNavigationService(navigationService);

        controller.getBackButton().setOnAction(event -> {
            if (previousScene != null) {
                primaryStage.setScene(previousScene);
                return;
            }
            try {
                navigationService.showSavedContent(SavedContentType.SUMMARIES);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        primaryStage.setScene(new Scene(root));
    }

}
