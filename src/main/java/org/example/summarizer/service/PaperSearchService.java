package org.example.summarizer.service;

import org.example.summarizer.domain.Paper;
import org.example.summarizer.viewmodel.Category;

import java.time.LocalDate;
import java.util.List;

//Temporary solution for development
public class PaperSearchService {
    public List<Paper> search(String query, Category category) {
        return List.of(new Paper("Photon entanglement", 1, LocalDate.of(2026, 6, 19), LocalDate.of(2026, 06, 20), "Author1, Author 2", "https://www.w3schools.com/JAVA/java_date.asp"),
                new Paper("Neutrino detection", 4, LocalDate.of(2026, 6, 17), LocalDate.of(2026, 06, 18), "Author3, Author 1", "https://stackoverflow.com/questions/68605437/how-to-pass-localdate-as-an-argument"));
    }
}
