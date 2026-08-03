module org.example.scierizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;
    requires com.fasterxml.jackson.databind;
    requires jdk.httpserver;
    opens org.example.scierizer.view to javafx.fxml;
    opens org.example.scierizer.infrastructure.ollama to com.fasterxml.jackson.databind;
    opens org.example.scierizer.domain.settings to com.fasterxml.jackson.databind;
    opens org.example.scierizer.viewmodel to com.fasterxml.jackson.databind;
    exports org.example.scierizer;
}
