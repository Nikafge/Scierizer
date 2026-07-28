package org.example.summarizer.repository;

import org.example.summarizer.domain.Summary;
import org.example.summarizer.viewmodel.SummaryType;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SummaryRepository {

    void save(Summary summary) throws SQLException;
    Optional<Summary> findByID(int id);
    Optional<Summary> findByPdfLinkAndType(String pdfLink, SummaryType summaryType);
    List<Summary> findAll();
    void deleteById(int id);

}
