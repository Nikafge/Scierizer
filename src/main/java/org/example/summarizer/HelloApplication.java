package org.example.summarizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.summarizer.domain.Paper;
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
import org.example.summarizer.viewmodel.MainViewModel;
import org.example.summarizer.viewmodel.PaperDetailsViewModel;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public class HelloApplication extends Application {

    private Stage primaryStage;
    ArxivClient arxivClient = new ArxivClient();
    PaperSearchService paperSearchService = new PaperSearchService(arxivClient);
    SummaryService summaryService = new SummaryService(new TextExtractor(), new OllamaClient(), new TextShredder());
    DBInitializer dbInitializer = new DBInitializer(Path.of(System.getProperty("user.home"), ".summarizer"));

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        primaryStage = stage;
        dbInitializer.initialize();
        //Initialize dependencies


        showMainView();
        primaryStage.setTitle("Arxiv Summarizer");
        primaryStage.show();
    }

    private void showMainView() throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Main.fxml"));
        Parent root = fxmlLoader.load();
        MainViewModel mainViewModel = new MainViewModel(paperSearchService);
        MainViewController controller = fxmlLoader.getController();
        NavigationService navigationService = new NavigationService(primaryStage, mainViewModel);
        controller.setViewModel(mainViewModel);
        controller.setOnPaperSelected(paper -> {

            try {
                showPaperDetailsView(paper);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        primaryStage.setScene(new Scene(root));
    }

    private void showPaperDetailsView(Paper paper) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("detailedPaper.fxml"));

        Parent root = fxmlLoader.load();

        PaperDetailsViewModel viewModel = new PaperDetailsViewModel(paper, summaryService, dbInitializer);

        PaperDetailViewController controller = fxmlLoader.getController();
        controller.setPaperDetailViewModel(viewModel);

        controller.getBackButton().setOnAction(event -> {
            try {
                showMainView();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        primaryStage.setScene(new Scene(root));

    }



}




//public class HelloApplication extends Application {
//
//    private Stage primaryStage;
//
//    private PaperSearchService paperSearchService;
//
//    @Override
//    public void start(Stage stage) throws IOException {
//        this.primaryStage = stage;
//
//        ArxivClient arxivClient = new ArxivClient();
//        this.paperSearchService = new PaperSearchService(arxivClient);
//
//        showMainView();
//
//        primaryStage.setTitle("arXiv Summarizer");
//        primaryStage.show();
//    }
//
//    private void showMainView() throws IOException {
//        FXMLLoader loader = new FXMLLoader(
//                getClass().getResource("/org/example/summarizer/view/MainView.fxml")
//        );
//
//        Parent root = loader.load();
//
//        MainViewModel mainViewModel = new MainViewModel(paperSearchService);
//
//        MainViewController controller = loader.getController();
//        controller.setViewModel(mainViewModel);
//
//        controller.setOnPaperSelected(paper -> {
//            try {
//                showPaperDetailsView(paper);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        primaryStage.setScene(new Scene(root));
//    }
//
//    private void showPaperDetailsView(Paper paper) throws IOException {
//        FXMLLoader loader = new FXMLLoader(
//                getClass().getResource("/org/example/summarizer/view/PaperDetailsView.fxml")
//        );
//
//        Parent root = loader.load();
//
//        PaperDetailsViewModel viewModel = new PaperDetailsViewModel(paper);
//
//        PaperDetailViewController controller = loader.getController();
//        controller.setPaperDetailViewModel(viewModel);
//
//        controller.getBackButton().setOnAction(event -> {
//            try {
//                showMainView();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        primaryStage.setScene(new Scene(root));
//    }
//}