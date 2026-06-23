package org.example.summarizer.service;

import org.example.summarizer.domain.Paper;
import org.example.summarizer.infrastructure.arxiv.ArxivClient;
import org.example.summarizer.viewmodel.Category;

import java.io.IOException;
import java.util.List;

//Search service to link ViewModel to Logic
public class PaperSearchService {

    private final ArxivClient arxivClient;
    private final String baseUrl = "https://export.arxiv.org/api/query?search_query=";


    public PaperSearchService(ArxivClient arxivClient) {
        this.arxivClient = arxivClient;
    }

    public String urlRequestBuilder(String query, Category category) {
        if (category == null) {
            return query + "all:" + query;
        }
        return "all:" + query + "+AND+cat:" + category.getDisplayName();
    }



    public List<Paper> search(String query, Category category) throws IOException, InterruptedException {
        if (query == null && category == null) {
            throw new IllegalArgumentException("Both query and category cannot be empty!");
        }

        String url = baseUrl + urlRequestBuilder(query, category);

        return arxivClient.paperResponse(url);
    }
}
