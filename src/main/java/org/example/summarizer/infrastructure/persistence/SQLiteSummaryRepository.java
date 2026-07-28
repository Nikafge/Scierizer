package org.example.summarizer.infrastructure.persistence;

import org.example.summarizer.domain.LocalDateTransformer;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.domain.Summary;
import org.example.summarizer.repository.SummaryRepository;
import org.example.summarizer.viewmodel.SummaryType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteSummaryRepository implements SummaryRepository {

    private final DBInitializer dbInitializer;

    public SQLiteSummaryRepository(DBInitializer dbInitializer) {
        this.dbInitializer = dbInitializer;
    }

    public void save(Summary summary) {

        String sqlRequest = summary.id() <= 0
                ? """
                    INSERT INTO summary (summary, pdf_link, title, authors, date_published, summary_type)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """
                : """
                    INSERT INTO summary (id, summary, pdf_link, title, authors, date_published, summary_type)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(id) DO UPDATE SET
                    summary = excluded.summary,
                    pdf_link = excluded.pdf_link,
                    title = excluded.title,
                    authors = excluded.authors,
                    date_published = excluded.date_published,
                    summary_type = excluded.summary_type
                    """;

        try (Connection connection = dbInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlRequest)) {
            int parameterIndex = 1;
            if (summary.id() > 0) {
                statement.setInt(parameterIndex++, summary.id());
            }
            statement.setString(parameterIndex++, summary.summary());
            statement.setString(parameterIndex++, summary.pdfLink());
            statement.setString(parameterIndex++, summary.title());
            statement.setString(parameterIndex++, summary.authors());
            statement.setString(parameterIndex++, LocalDateTransformer.convertToString(summary.datePublished()));
            statement.setString(parameterIndex, summary.summaryType().name());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save summary", e);
        }

    }

    public Optional<Summary> findByID(int id) {
        String sqlRequest = """
                SELECT id, summary, pdf_link, title, authors, date_published, summary_type
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

    public Optional<Summary> findByPdfLinkAndType(String pdfLink, SummaryType summaryType) {
        String sqlRequest = """
                SELECT id, summary, pdf_link, title, authors, date_published, summary_type
                FROM summary
                WHERE pdf_link = ? AND summary_type = ?
                """;
        try (Connection connection = dbInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlRequest)) {

            statement.setString(1, pdfLink);
            statement.setString(2, summaryType.getDisplayType().toUpperCase());
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
                SELECT id, summary, pdf_link, title, authors, date_published, summary_type
                FROM summary
                ORDER BY id
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

    public void deleteById(int id) {
        String sqlRequest = """
                DELETE FROM summary
                WHERE id = ?
                """;

        try (Connection connection = dbInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlRequest)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete saved summary", e);
        }
    }


    private Summary mapToSummary(ResultSet resultSet) throws SQLException {
        return new Summary(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("authors"),
                resultSet.getString("summary"),
                LocalDateTransformer.convertToLocalDate(resultSet.getString("date_published")),
                resultSet.getString("pdf_link"),
                SummaryType.valueOf(resultSet.getString("summary_type"))
        );
    }
}
