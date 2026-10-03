package org.example.scierizer;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.scierizer.service.NavigationService;
import org.example.scierizer.viewmodel.MainViewModel;

import java.io.IOException;
import java.sql.SQLException;

public class HelloApplication extends Application {

    private Stage primaryStage;
    private final AppContext appContext = new AppContext();
    private MainViewModel mainViewModel;
    private NavigationService navigationService;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        primaryStage = stage;
        appContext.initialize();
        initializeNavigation();

        navigationService.showMainView();
        primaryStage.setTitle("Scierizer for arXiv");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        appContext.temporaryFilesCleaner().clearIfConfigured();
        super.stop();
    }

    private void initializeNavigation() {
        mainViewModel = new MainViewModel(appContext.paperSearchService());
        navigationService = new NavigationService(primaryStage, mainViewModel, appContext);
    }
}
