package org.example.scierizer.repository;

import org.example.scierizer.domain.Paper;

import java.util.List;
import java.util.Optional;

public interface PaperRepository {

    void save(Paper paper);
    Optional<Paper> findById(int id);
    List<Paper> findAll();
    void deleteById(int id);

}
