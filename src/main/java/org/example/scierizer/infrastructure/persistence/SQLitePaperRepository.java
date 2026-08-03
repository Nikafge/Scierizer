package org.example.scierizer.infrastructure.persistence;

import org.example.scierizer.domain.LocalDateTransformer;
import org.example.scierizer.domain.Paper;
import org.example.scierizer.repository.PaperRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLitePaperRepository implements PaperRepository {

    private final DBInitializer dbInitializer;

    public SQLitePaperRepository(DBInitializer dbInitializer) {
        this.dbInitializer = dbInitializer;
    }

    @Override
    public void save(Paper paper) {
        String sqlRequest = paper.id() <= 0
                ? """
                    INSERT INTO paper (title, pdf_link, authors, date_published, date_updated, abstract)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """
                : """
                    INSERT INTO paper (id, title, pdf_link, authors, date_published, date_updated, abstract)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(id) DO UPDATE SET
                    title = excluded.title,
                    pdf_link = excluded.pdf_link,
                    authors = excluded.authors,
                    date_published = excluded.date_published,
                    date_updated = excluded.date_updated,
                    abstract = excluded.abstract
                    """;

        try(Connection connection = dbInitializer.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlRequest)) {

            int parameterIndex = 1;
            if (paper.id() > 0) {
                statement.setInt(parameterIndex++, paper.id());
            }
            statement.setString(parameterIndex++, paper.title());
            statement.setString(parameterIndex++, paper.pdfLink());
            statement.setString(parameterIndex++, paper.authors());
            statement.setString(parameterIndex++, LocalDateTransformer.convertToString(paper.datePublished()));
            statement.setString(parameterIndex++, LocalDateTransformer.convertToString(paper.dateUpdated()));
            statement.setString(parameterIndex, paper.abstractText());
            statement.executeUpdate();


        } catch(SQLException e) {
            throw new RuntimeException("Could not save to database", e);
        }
    }

    @Override
    public Optional<Paper> findById(int id) {
        String sqlRequest = """
                SELECT id, title, pdf_link, authors, date_published, date_updated, abstract
                FROM paper
                WHERE id = ?
                """;
        try (Connection connection = dbInitializer.getConnection();
        PreparedStatement statement = connection.prepareStatement(sqlRequest)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapToPaper(resultSet));
            }
        } catch(SQLException e) {
            throw new RuntimeException("Could not find saved paper in database", e);
        }
    }

    @Override
    public List<Paper> findAll() {
        String sqlRequest = """
                SELECT id, title, pdf_link, authors, date_published, date_updated, abstract
                FROM paper
                ORDER BY id
                """;
        try (Connection connection = dbInitializer.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlRequest);
            ResultSet resultSet = statement.executeQuery();
        ) {
            List<Paper> papers = new ArrayList<>();
            while(resultSet.next()) {
                papers.add(mapToPaper(resultSet));
            }
            return papers;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load saved papers", e);
        }
    }

    @Override
    public void deleteById(int id) {
        String sqlRequest = """
                DELETE FROM paper
                WHERE id = ?
                """;

        try (Connection connection = dbInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlRequest)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete saved paper", e);
        }
    }

    private Paper mapToPaper(ResultSet resultSet) throws SQLException {
        return new Paper(
                resultSet.getString("title"),
                resultSet.getInt("id"),
                resultSet.getString("abstract"),
                LocalDateTransformer.convertToLocalDate(resultSet.getString("date_published")),
                LocalDateTransformer.convertToLocalDate(resultSet.getString("date_updated")),
                resultSet.getString("authors"),
                resultSet.getString("pdf_link")
                );
    }

}
