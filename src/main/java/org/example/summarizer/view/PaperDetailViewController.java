package org.example.summarizer.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.summarizer.viewmodel.PaperDetailsViewModel;
import org.example.summarizer.viewmodel.PaperViewModel;

public class PaperDetailViewController {

    private PaperDetailsViewModel paperDetailsViewModel;

    @FXML
    private Button backButton;
    @FXML
    private Label titleLabel;
    @FXML
    private TextField authorsField;
    @FXML
    private TextField publishedField;
    @FXML
    private TextField updatedField;
    @FXML
    private TextField sourceField;
    @FXML
    private Button savePaperButton;
    @FXML
    private TextArea abstractArea;

    @FXML
    private ToggleButton structured;
    @FXML
    private ToggleButton tldr;
    @FXML
    private ToggleButton executive;
    @FXML
    private ToggleButton research;

    @FXML
    private Button generateSummaryButton;
    @FXML
    private TextArea generatedSummary;
    @FXML
    private Button saveSummary;



    public void setPaperDetailViewModel(PaperDetailsViewModel paperDetailsViewModel) {
        this.paperDetailsViewModel = paperDetailsViewModel;
        bindPaperViewModel();
    }

    public void bindPaperViewModel() {
        titleLabel.textProperty().bind(paperDetailsViewModel.titleProperty());
    }

    public Button getBackButton() {
        return backButton;
    }


}
