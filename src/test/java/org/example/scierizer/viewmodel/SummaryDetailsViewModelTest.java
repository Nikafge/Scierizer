//package org.example.scierizer.viewmodel;
//
//import org.example.scierizer.domain.Summary;
//import org.example.scierizer.domain.settings.AppSettings;
//import org.example.scierizer.domain.settings.StorageSettings;
//import org.example.scierizer.service.ReportExportService;
//import org.example.scierizer.service.SettingsService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class SummaryDetailsViewModelTest {
//
//    @TempDir
//    Path tempDirectory;
//
//    @Test
//    void exportSummaryUsesConfiguredDirectoryAndFormat() throws Exception {
//        Path exportDirectory = tempDirectory.resolve("exports");
//        SettingsService settingsService = new SettingsService(tempDirectory);
//        AppSettings defaults = AppSettings.defaults();
//        settingsService.saveSettings(new AppSettings(
//                defaults.version(),
//                defaults.model(),
//                defaults.processing(),
//                new StorageSettings(
//                        "",
//                        exportDirectory.toString(),
//                        "",
//                        "Text",
//                        false,
//                        false,
//                        false,
//                        false
//                )
//        ));
//        Summary summary = new Summary(
//                0,
//                "Saved Summary",
//                "Author",
//                "Summary body",
//                LocalDate.of(2026, 7, 28),
//                "paper.pdf",
//                SummaryType.TLDR
//        );
//        SummaryDetailsViewModel viewModel = new SummaryDetailsViewModel(
//                summary,
//                settingsService,
//                new ReportExportService()
//        );
//
//        viewModel.exportSummary();
//
//        Path outputPath = exportDirectory.resolve("Saved Summary - tldr.txt");
//        assertTrue(Files.exists(outputPath));
//        assertTrue(Files.readString(outputPath).contains("Summary body"));
//        assertTrue(viewModel.exportStatus().get().contains(outputPath.toString()));
//    }
//}
