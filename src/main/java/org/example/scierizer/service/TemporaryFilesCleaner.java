package org.example.scierizer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** Removes temporary application files when that behavior is enabled in settings. */
public class TemporaryFilesCleaner {
    private final SettingsService settingsService;

    public TemporaryFilesCleaner(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void clearIfConfigured() throws IOException {
        var storageSettings = settingsService.loadSettings().storage();
        if (!storageSettings.clearTemporaryFilesOnExit()) {
            return;
        }

        String configuredDirectory = storageSettings.temporaryDirectory();
        Path temporaryDirectory = configuredDirectory == null || configuredDirectory.isBlank()
                ? settingsService.getSettingsDirectory().resolve("tmp")
                : Path.of(configuredDirectory);

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
