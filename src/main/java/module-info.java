module org.example.summarizer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;

    opens org.example.summarizer.view to javafx.fxml;
    exports org.example.summarizer;
}