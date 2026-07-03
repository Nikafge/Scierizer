module org.example.summarizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires org.apache.pdfbox;

    opens org.example.summarizer.view to javafx.fxml;
    exports org.example.summarizer;
}
