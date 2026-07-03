package org.example.summarizer.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {

    private String dbUrl;

    public Path getDbPath() {
        Path appDbPath = Path.of(
                System.getProperty("user.home"), ".summarizer"
        );

        try {
            Files.createDirectories(appDbPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create new folder", e);
        }
        return appDbPath.resolve("db.db");
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + getDbPath().toString());
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to database", e);
        }
    }
    private void enableForeignKeys(Connection connection) throws SQLException{
        try (Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }

    public void initialize() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS paper (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    abstract TEXT,
                    date_published TEXT,
                    date_updated TEXT,
                    pdf_link TEXT,
                    authors TEXT
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS summary (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    summary TEXT NOT NULL,
                    pdf_link TEXT,
                    title TEXT,
                    authors TEXT,
                    date_published TEXT
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database", e);
        }
    }
}
