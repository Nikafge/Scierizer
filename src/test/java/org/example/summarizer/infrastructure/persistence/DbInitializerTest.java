package org.example.summarizer.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DbInitializerTest {

    @TempDir
    Path tempRoot;

    private Path  dbDir;
    private DBInitializer dbInitializer;

    @BeforeEach
    void setUp() {
        dbDir = tempRoot.resolve("appdata");
        dbInitializer = new DBInitializer(dbDir);
    }

    @Test
    void testDirectoryCreating() {
        assertTrue(Files.exists(dbDir));
        assertTrue(Files.isDirectory(dbDir));
    }

    @Test
    void testConnectionIsOpen() throws SQLException {
        try (Connection connection = dbInitializer.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }

    @Test
    void testCreatingPaperTable() throws SQLException {
        dbInitializer.initialize();

        try (Connection connection = dbInitializer.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(paper)")) {

            var columns = new java.util.ArrayList<String>();
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }

            assertEquals(
                    List.of("id", "title", "abstract", "date_published", "date_updated", "pdf_link", "authors"),
                    columns
            );
        }
    }

    @Test
    void testCreatingSummaryTable() throws SQLException {
        dbInitializer.initialize();

        try (Connection connection = dbInitializer.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(summary)")) {

            var columns = new java.util.ArrayList<String>();
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }

            assertEquals(
                    List.of("id", "summary", "pdf_link", "title", "authors", "date_published"),
                    columns
            );
        }
    }

    @Test
    void testInitializeIdempotent() {
        dbInitializer.initialize();
        assertDoesNotThrow(() -> dbInitializer.initialize());
    }

    @Test
    void testInsertedDataPersistsBetweenConnections() throws SQLException {
        dbInitializer.initialize();

        try (Connection connection = dbInitializer.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO paper (title) VALUES ('Test paper')");
        }

        try (Connection connection = dbInitializer.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT title FROM paper WHERE id = 1")) {
            assertTrue(rs.next());
            assertEquals("Test paper", rs.getString("title"));
        }
    }
}