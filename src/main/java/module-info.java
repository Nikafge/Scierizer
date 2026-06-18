module org.example.summarizer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.summarizer to javafx.fxml;
    exports org.example.summarizer;
}