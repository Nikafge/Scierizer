package org.example.scierizer.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/** Loads FXML views and exposes their root node together with their controller. */
public class ViewFactory {
    public <T> LoadedView<T> load(String resourceName) throws IOException {
        FXMLLoader loader = new FXMLLoader(ViewFactory.class.getResource("/org/example/scierizer/" + resourceName));
        Parent root = loader.load();
        return new LoadedView<>(root, loader.getController());
    }

    public record LoadedView<T>(Parent root, T controller) {
    }
}
