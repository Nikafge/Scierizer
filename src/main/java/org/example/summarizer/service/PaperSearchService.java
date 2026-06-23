package org.example.summarizer.service;

import org.example.summarizer.domain.Paper;
import org.example.summarizer.infrastructure.arxiv.ArxivClient;
import org.example.summarizer.viewmodel.Category;

import java.time.LocalDate;
import java.util.List;

//Temporary solution for development
public class PaperSearchService {

    private final ArxivClient arxivClient;
    private final String baseUrl = "https://export.arxiv.org/api/query?search_query=";


    public PaperSearchService(ArxivClient arxivClient) {
        this.arxivClient = arxivClient;
    }



    public List<Paper> search(String query, Category category) {
        if (query == null && category == null) {
            throw new IllegalArgumentException("Both query and category cannot be empty!");
        }

        

        String parameters = ;
        return arxivClient.paperResponse(parameters);
    }
}
