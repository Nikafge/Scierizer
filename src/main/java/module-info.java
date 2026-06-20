module org.example.summarizer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.summarizer.view to javafx.fxml;
    exports org.example.summarizer;
}