module org.example.summarizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;
    requires com.fasterxml.jackson.databind;
    requires jdk.httpserver;
    opens org.example.summarizer.view to javafx.fxml;
    opens org.example.summarizer.infrastructure.ollama to com.fasterxml.jackson.databind;
    exports org.example.summarizer;
}
