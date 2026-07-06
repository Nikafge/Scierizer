package org.example.summarizer.infrastructure.ollama;

import org.example.summarizer.viewmodel.SummaryType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

public class OllamaClient {

    private String baseUrl = "http://localhost:11434/api/generate";

    //TODO Make this part.
    private String buildRequest(String parameters) {
        return baseUrl + parameters;
    }

    public String getOllamaSummary(String content, SummaryType summaryType) {

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(baseUrl)).build();




        return null;
    }
    String sendOllamaSummary(List<String> content) {

        return null;
    }

}
