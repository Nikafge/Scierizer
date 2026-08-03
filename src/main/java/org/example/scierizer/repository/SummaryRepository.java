package org.example.scierizer.repository;

import org.example.scierizer.domain.Summary;
import org.example.scierizer.viewmodel.SummaryType;

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
