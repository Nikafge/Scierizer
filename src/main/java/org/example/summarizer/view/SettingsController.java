package org.example.summarizer.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.example.summarizer.domain.settings.AppSettings;
import org.example.summarizer.domain.settings.ModelSettings;
import org.example.summarizer.domain.settings.ProcessingSettings;
import org.example.summarizer.domain.settings.StorageSettings;
import org.example.summarizer.service.NavigationService;
import org.example.summarizer.service.SettingsService;
import org.example.summarizer.viewmodel.SummaryType;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class SettingsController {

    private SettingsService settingsService;

    @FXML
    private TopNavigationController topNavigationController;

    @FXML
    private TabPane settingsTabPane;
    @FXML
    private ComboBox<String> providerComboBox;
    @FXML
    private ComboBox<String> modelComboBox;
    @FXML
    private Button refreshModelsButton;
    @FXML
    private Spinner<Integer> requestTimeoutMinutesSpinner;
    @FXML
    private TitledPane ollamaSettingsPane;
    @FXML
    private TextField ollamaBaseUrlField;
    @FXML
    private ComboBox<String> contextModeComboBox;
    @FXML
    private Label detectedVramLabel;
    @FXML
    private Button detectVramButton;
    @FXML
    private Spinner<Integer> contextTokensSpinner;
    @FXML
    private Spinner<Integer> maxOutputTokensSpinner;
    @FXML
    private TitledPane cloudSettingsPane;
    @FXML
    private TextField cloudEndpointField;
    @FXML
    private PasswordField apiKeyField;
    @FXML
    private Button clearApiKeyButton;
    @FXML
    private CheckBox rememberApiKeyCheckBox;
    @FXML
    private Button testConnectionButton;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private ComboBox<String> chunkingModeComboBox;
    @FXML
    private Spinner<Integer> chunkSizeTokensSpinner;
    @FXML
    private Spinner<Integer> chunkOverlapTokensSpinner;
    @FXML
    private ComboBox<SummaryType> defaultSummaryTypeComboBox;
    @FXML
    private ComboBox<String> outputLanguageComboBox;
    @FXML
    private CheckBox preserveNumbersCheckBox;
    @FXML
    private CheckBox includeEquationsCheckBox;
    @FXML
    private CheckBox includeReferencesCheckBox;
    @FXML
    private CheckBox includeFiguresCheckBox;
    @FXML
    private TextField papersDirectoryField;
    @FXML
    private Button browsePapersDirectoryButton;
    @FXML
    private TextField summariesDirectoryField;
    @FXML
    private Button browseSummariesDirectoryButton;
    @FXML
    private TextField temporaryDirectoryField;
    @FXML
    private Button browseTemporaryDirectoryButton;
    @FXML
    private ComboBox<String> summaryExportFormatComboBox;
    @FXML
    private CheckBox downloadPdfWhenSavingCheckBox;
    @FXML
    private CheckBox exportSummaryWhenSavingCheckBox;
    @FXML
    private CheckBox keepExtractedTextCheckBox;
    @FXML
    private CheckBox clearTemporaryFilesOnExitCheckBox;
    @FXML
    private Label saveStatusLabel;
    @FXML
    private Button resetDefaultsButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    @FXML
    private void initialize() {
        configureChoiceControls();
        configureSpinners();
        configureActions();
        applySettings(AppSettings.defaults());
    }

    public void setNavigationService(NavigationService navigationService) {
        topNavigationController.setNavigationService(navigationService);
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = Objects.requireNonNull(settingsService);
        loadSettings();
    }

    private void configureChoiceControls() {
        providerComboBox.setItems(FXCollections.observableArrayList("Ollama", "OpenAI", "Anthropic", "Custom"));
        modelComboBox.setItems(FXCollections.observableArrayList("qwen3:8b", "gemma3:4b", "llama3.1:8b"));
        contextModeComboBox.setItems(FXCollections.observableArrayList("Auto", "Manual"));
        chunkingModeComboBox.setItems(FXCollections.observableArrayList("Auto", "Manual"));
        defaultSummaryTypeComboBox.setItems(FXCollections.observableArrayList(Arrays.asList(SummaryType.values())));
        outputLanguageComboBox.setItems(FXCollections.observableArrayList("English", "German", "French", "Spanish"));
        summaryExportFormatComboBox.setItems(FXCollections.observableArrayList("Markdown", "Text"));
    }

    private void configureSpinners() {
        requestTimeoutMinutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 240, 10));
        contextTokensSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 262144, 8192, 512));
        maxOutputTokensSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(128, 32768, 1024, 128));
        chunkSizeTokensSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(512, 262144, 6000, 256));
        chunkOverlapTokensSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65536, 500, 128));
    }

    private void configureActions() {
        saveButton.setOnAction(event -> saveSettings());
        resetDefaultsButton.setOnAction(event -> {
            applySettings(AppSettings.defaults());
            setSaveStatus("Defaults restored. Press Save to persist them.");
        });
        cancelButton.setOnAction(event -> loadSettings());
        clearApiKeyButton.setOnAction(event -> apiKeyField.clear());

        browsePapersDirectoryButton.setOnAction(event -> chooseDirectory(papersDirectoryField));
        browseSummariesDirectoryButton.setOnAction(event -> chooseDirectory(summariesDirectoryField));
        browseTemporaryDirectoryButton.setOnAction(event -> chooseDirectory(temporaryDirectoryField));

        refreshModelsButton.setOnAction(event -> setConnectionStatus("Model refresh is not available yet."));
        detectVramButton.setOnAction(event -> detectedVramLabel.setText("Not detected"));
        testConnectionButton.setOnAction(event -> setConnectionStatus("Connection test is not available yet."));
    }

    private void loadSettings() {
        if (settingsService == null) {
            applySettings(AppSettings.defaults());
            return;
        }

        applySettings(settingsService.loadSettings());
        setSaveStatus("Loaded settings from " + settingsService.getSettingsFilePath());
    }

    private void saveSettings() {
        if (settingsService == null) {
            setSaveStatus("Settings service is not available.");
            return;
        }

        settingsService.saveSettings(readSettingsFromControls());
        setSaveStatus("Saved settings to " + settingsService.getSettingsFilePath());
    }

    private AppSettings readSettingsFromControls() {
        return new AppSettings(
                AppSettings.defaults().version(),
                new ModelSettings(
                        comboValue(providerComboBox, "Ollama"),
                        comboValue(modelComboBox, "qwen3:8b"),
                        textValue(ollamaBaseUrlField, "http://localhost:11434"),
                        comboValue(contextModeComboBox, "Auto"),
                        textValue(cloudEndpointField, ""),
                        rememberApiKeyCheckBox.isSelected() ? textValue(apiKeyField, "") : "",
                        rememberApiKeyCheckBox.isSelected(),
                        spinnerValue(contextTokensSpinner),
                        spinnerValue(maxOutputTokensSpinner),
                        spinnerValue(requestTimeoutMinutesSpinner)
                ),
                new ProcessingSettings(
                        comboValue(chunkingModeComboBox, "Auto"),
                        spinnerValue(chunkSizeTokensSpinner),
                        spinnerValue(chunkOverlapTokensSpinner),
                        defaultSummaryTypeComboBox.getValue() == null
                                ? SummaryType.STRUCTURED
                                : defaultSummaryTypeComboBox.getValue(),
                        comboValue(outputLanguageComboBox, "English"),
                        preserveNumbersCheckBox.isSelected(),
                        includeEquationsCheckBox.isSelected(),
                        includeReferencesCheckBox.isSelected(),
                        includeFiguresCheckBox.isSelected()
                ),
                new StorageSettings(
                        textValue(papersDirectoryField, ""),
                        textValue(summariesDirectoryField, ""),
                        textValue(temporaryDirectoryField, ""),
                        comboValue(summaryExportFormatComboBox, "Markdown"),
                        downloadPdfWhenSavingCheckBox.isSelected(),
                        exportSummaryWhenSavingCheckBox.isSelected(),
                        keepExtractedTextCheckBox.isSelected(),
                        clearTemporaryFilesOnExitCheckBox.isSelected()
                )
        );
    }

    private void applySettings(AppSettings settings) {
        ModelSettings model = settings.model();
        providerComboBox.setValue(model.provider());
        modelComboBox.setValue(model.modelName());
        ollamaBaseUrlField.setText(model.ollamaBaseUrl());
        contextModeComboBox.setValue(model.contextMode());
        cloudEndpointField.setText(model.cloudEndpoint());
        apiKeyField.setText(model.rememberApiKey() ? model.apiKeyReference() : "");
        rememberApiKeyCheckBox.setSelected(model.rememberApiKey());
        setSpinnerValue(contextTokensSpinner, model.contextTokens());
        setSpinnerValue(maxOutputTokensSpinner, model.maxOutputTokens());
        setSpinnerValue(requestTimeoutMinutesSpinner, model.requestTimeoutMinutes());

        ProcessingSettings processing = settings.processing();
        chunkingModeComboBox.setValue(processing.chunkingMode());
        setSpinnerValue(chunkSizeTokensSpinner, processing.chunkSizeTokens());
        setSpinnerValue(chunkOverlapTokensSpinner, processing.chunkOverlapTokens());
        defaultSummaryTypeComboBox.setValue(processing.defaultSummaryType());
        outputLanguageComboBox.setValue(processing.outputLanguage());
        preserveNumbersCheckBox.setSelected(processing.preserveNumbers());
        includeEquationsCheckBox.setSelected(processing.includeEquations());
        includeReferencesCheckBox.setSelected(processing.includeReferences());
        includeFiguresCheckBox.setSelected(processing.includeFigures());

        StorageSettings storage = settings.storage();
        papersDirectoryField.setText(storage.papersDirectory());
        summariesDirectoryField.setText(storage.summariesDirectory());
        temporaryDirectoryField.setText(storage.temporaryDirectory());
        summaryExportFormatComboBox.setValue(storage.summaryExportFormat());
        downloadPdfWhenSavingCheckBox.setSelected(storage.downloadPdfWhenSaving());
        exportSummaryWhenSavingCheckBox.setSelected(storage.exportSummaryWhenSaving());
        keepExtractedTextCheckBox.setSelected(storage.keepExtractedText());
        clearTemporaryFilesOnExitCheckBox.setSelected(storage.clearTemporaryFilesOnExit());
    }

    private void chooseDirectory(TextField targetField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select directory");

        File currentDirectory = new File(targetField.getText());
        if (currentDirectory.isDirectory()) {
            directoryChooser.setInitialDirectory(currentDirectory);
        }

        File selectedDirectory = directoryChooser.showDialog(ownerWindow());
        if (selectedDirectory != null) {
            targetField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private Window ownerWindow() {
        if (saveButton.getScene() == null) {
            return null;
        }
        return saveButton.getScene().getWindow();
    }

    private String comboValue(ComboBox<String> comboBox, String fallback) {
        String value = comboBox.isEditable() ? comboBox.getEditor().getText() : comboBox.getValue();
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String textValue(TextField textField, String fallback) {
        String value = textField.getText();
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private int spinnerValue(Spinner<Integer> spinner) {
        try {
            Integer editorValue = spinner.getValueFactory()
                    .getConverter()
                    .fromString(spinner.getEditor().getText());
            spinner.getValueFactory().setValue(editorValue);
        } catch (RuntimeException ignored) {
            spinner.getEditor().setText(String.valueOf(spinner.getValue()));
        }

        return spinner.getValue();
    }

    private void setSpinnerValue(Spinner<Integer> spinner, int value) {
        spinner.getValueFactory().setValue(value);
    }

    private void setConnectionStatus(String status) {
        connectionStatusLabel.setText(status);
    }

    private void setSaveStatus(String status) {
        saveStatusLabel.setText(status);
    }
}
