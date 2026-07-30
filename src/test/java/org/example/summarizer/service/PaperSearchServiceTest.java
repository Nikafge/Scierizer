package org.example.summarizer.service;

import org.example.summarizer.domain.Paper;
import org.example.summarizer.infrastructure.arxiv.ArxivClient;
import org.example.summarizer.viewmodel.Category;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperSearchServiceTest {

    @Test
    void searchPassesPaginationToArxivClient() throws IOException, InterruptedException {
        FakeArxivClient arxivClient = new FakeArxivClient();
        PaperSearchService paperSearchService = new PaperSearchService(arxivClient);

        paperSearchService.search("quantum gravity", Category.PHYSICS, 20, 10);

        assertEquals("all:quantum+gravity+AND+cat:physics.*", arxivClient.parameters);
        assertEquals(20, arxivClient.start);
        assertEquals(10, arxivClient.maxResults);
    }

    private static class FakeArxivClient extends ArxivClient {
        private String parameters;
        private int start;
        private int maxResults;

        @Override
        public List<Paper> paperResponse(String parameters, int start, int maxResults) {
            this.parameters = parameters;
            this.start = start;
            this.maxResults = maxResults;
            return List.of();
        }
    }
}
