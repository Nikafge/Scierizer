package org.example.scierizer.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {

    private final Path appDbPath;

    public DBInitializer(Path appDbPath) {
        this.appDbPath = appDbPath;
        createDirectory();
    }

    public Path getAppDbPath() {
        return appDbPath;
    }

    public void createDirectory() {
        try {
            Files.createDirectories(appDbPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create new folder", e);
        }
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + appDbPath.resolve("db.db").toString());
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to database", e);
        }
    }

    //Enable this method once DB need foreign keys for tables
//    private void enableForeignKeys(Connection connection) throws SQLException{
//        try (Statement statement = connection.createStatement()){
//            statement.execute("PRAGMA foreign_keys = ON");
//        }
//    }

    public void initialize() {
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
                    date_published TEXT,
                    summary_type TEXT NOT NULL DEFAULT 'STRUCTURED'
                )
            """);

            if (!columnExists(connection, "summary", "summary_type")) {
                statement.execute("""
                    ALTER TABLE summary
                    ADD COLUMN summary_type TEXT NOT NULL DEFAULT 'STRUCTURED'
                    """);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                if (columnName.equals(resultSet.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
