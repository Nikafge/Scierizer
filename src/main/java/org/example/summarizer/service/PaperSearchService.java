package org.example.summarizer.service;

import org.example.summarizer.domain.Paper;
import org.example.summarizer.infrastructure.arxiv.ArxivClient;
import org.example.summarizer.viewmodel.Category;

import java.io.IOException;
import java.util.List;

//Search service to link ViewModel to Logic
public class PaperSearchService {
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final ArxivClient arxivClient;

    public PaperSearchService(ArxivClient arxivClient) {
        this.arxivClient = arxivClient;
    }

    public String urlRequestBuilder(String query, Category category) {
        if (category == null) {
            return "all:" + query;
        }
        return "all:" + query + "+AND+cat:" + category.getDisplayName() + ".*";
    }



    public List<Paper> search(String query, Category category) throws IOException, InterruptedException {
        return search(query, category, 0, DEFAULT_PAGE_SIZE);
    }

    public List<Paper> search(String query, Category category, int start, int maxResults) throws IOException, InterruptedException {
        if (query == null && category == null) {
            throw new IllegalArgumentException("Both query and category cannot be empty!");
        }
        String encodeQuery = query.replace(" ", "+");
        String parameters = urlRequestBuilder(encodeQuery, category);
        return arxivClient.paperResponse(parameters, start, maxResults);
    }
}
