package org.example.summarizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.summarizer.infrastructure.arxiv.ArxivClient;
import org.example.summarizer.infrastructure.persistence.DBInitializer;
import org.example.summarizer.service.PaperSearchService;
import org.example.summarizer.view.MainViewController;
import org.example.summarizer.viewmodel.MainViewModel;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, SQLException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Main.fxml"));
        Parent root = fxmlLoader.load();

        DBInitializer dbInitializer = new DBInitializer(Path.of(System.getProperty("user.home"), ".summarizer"));
        dbInitializer.initialize();
        ArxivClient arxivClient = new ArxivClient();
        PaperSearchService paperSearchService = new PaperSearchService(arxivClient);
        MainViewModel mainViewModel = new MainViewModel(paperSearchService);

        MainViewController controller = fxmlLoader.getController();
        controller.setViewModel(mainViewModel);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Arxiv Summarizer");
        stage.show();
    }
}
