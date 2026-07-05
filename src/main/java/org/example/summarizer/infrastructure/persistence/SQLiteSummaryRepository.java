package org.example.summarizer.infrastructure.persistence;

import org.example.summarizer.domain.LocalDateTransformer;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.domain.Summary;
import org.example.summarizer.repository.SummaryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteSummaryRepository implements SummaryRepository {

    private final DBInitializer dbInitializer;

    public SQLiteSummaryRepository(DBInitializer dbInitializer) {
        this.dbInitializer = dbInitializer;
    }

    public void save(Summary summary) throws SQLException {
        String sqlRequest = """
                INSERT INTO summary (id, summary, pdf_link, title, authors, date_published)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                summary = excluded.summary,
                excluded.pdf_link,
                excluded.title,
                excluded.authors,
                excluded.date_published
                
                """;

        try (Connection connection = dbInitializer.getConnection();
        PreparedStatement statement = connection.prepareStatement(sqlRequest)) {
            statement.setInt(1, summary.id());
            statement.setString(2, summary.summary());
            statement.setString(3, summary.pdf_link());
            statement.setString(4, summary.title());
            statement.setString(5, summary.authors());
            statement.setString(6, LocalDateTransformer.convertToString(summary.date_published()));
        } catch (SQLException e) {
            throw new RuntimeException("Could not save summary", e);
        }
    }

    public Optional<Summary> findByID(int id) {
        String sqlRequest = """
                SELECT id, summary, pdf_link, title, authors, date_published
                FROM summary
                WHERE id = ?
                """;
        try (Connection connection = dbInitializer.getConnection();
        PreparedStatement statement = connection.prepareStatement(sqlRequest)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapToSummary(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not find summary", e);
        }
    }

    public List<Summary> findAll() {

        String sqlRequest = """
                SELECT id, summary, pdf_link, title, authors, date_published
                FROM summary
                """;
        try (Connection connection = dbInitializer.getConnection();
        PreparedStatement statement = connection.prepareStatement(sqlRequest);
        ResultSet resultSet = statement.executeQuery()) {

            List<Summary> summaries = new ArrayList<>();

            while(resultSet.next()) {
                summaries.add(mapToSummary(resultSet));
            }
            return summaries;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load saved summaries", e);
        }
    }


    private Summary mapToSummary(ResultSet resultSet) throws SQLException {
        return new Summary(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("authors"),
                resultSet.getString("summary"),
                LocalDateTransformer.convertToLocalDate(resultSet.getString("date_published")),
                resultSet.getString("pdf_link")
        );
    }
}
