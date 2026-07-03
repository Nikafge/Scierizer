package org.example.summarizer.repository;

import org.example.summarizer.domain.Summary;

import java.util.List;
import java.util.Optional;

public interface SummaryRepository {

    void save(Summary summary);
    Optional<Summary> findByID(int id);
    List<Summary> findAll();

}
