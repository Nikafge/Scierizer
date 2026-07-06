package org.example.summarizer.repository;

import org.example.summarizer.domain.Paper;

import java.util.List;
import java.util.Optional;

public interface PaperRepository {

    void save(Paper paper);
    Optional<Paper> findById(int id);
    List<Paper> findAll();

}
