package org.example.summarizer.infrastructure.arxiv;

import org.example.summarizer.domain.Paper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArxivClient {

    private String baseUrl = "https://export.arxiv.org/api/query?search_query=";

    public List<Paper> paperResponse(String parameters) throws IOException, InterruptedException {
        return paperResponse(parameters, 0, 20);
    }

    public List<Paper> paperResponse(String parameters, int start, int maxResults) throws IOException, InterruptedException {
        String resultingUrl = baseUrl + parameters
                + "&start=" + Math.max(0, start)
                + "&max_results=" + Math.max(1, maxResults);
        System.out.println(resultingUrl);

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(resultingUrl)).timeout(Duration.ofSeconds(10)).GET().build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException("ArXiv returned error!");
            }


            //Parse response as list of strings, write them in a separate list as Papers
            List<String> papersAsStrings = StringToListConverter(response.body(), maxResults);
            List<Paper> resultingPapers = new ArrayList<>();

            for(String paperAsString : papersAsStrings) {
                resultingPapers.add(parsePaper(paperAsString));
            }

            return resultingPapers;
        }
        catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to get response!", e);
        }
    }

    //Converts a single string to a list of strings for different papers
    public List<String> StringToListConverter (String apiResponse) {
        return StringToListConverter(apiResponse, 20);
    }

    public List<String> StringToListConverter (String apiResponse, int maxResults) {

        Pattern pattern = Pattern.compile("<entry[^>]*>(.*?)</entry>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(apiResponse);
        List<String> papersAsStrings =  new ArrayList<>();
        int i = 0;

        while(matcher.find() && i < maxResults) {
            papersAsStrings.add(matcher.group());
            i++;
        }
        return papersAsStrings;
    }

    // New Method for extraction
    public String extractField(String source, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + "[^>]*>(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Extracts metadata from API response for a single Paper
    public Paper parsePaper(String paperInfo) {

        //title and summary
        String title = extractField(paperInfo, "title");
        String summary = extractField(paperInfo, "summary");

        //authors
        List<String> authorsList = new ArrayList<>();
        Pattern pattern = Pattern.compile("<name[^>]*>(.*?)</name>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(paperInfo);
        while (matcher.find()) {
            authorsList.add(matcher.group(1));
        }
        String authors = String.join("|", authorsList);

        //link
        String pdfLink = null;
        pattern = Pattern.compile("<link[^>]*/>");
        matcher = pattern.matcher(paperInfo);
        while (matcher.find()) {
            String linkTag = matcher.group();
            if (linkTag.contains("type=\"application/pdf\"")) {
                Pattern hrefPatter = Pattern.compile("href=\"([^\"]+)\"");
                Matcher hrefMatcher = hrefPatter.matcher(linkTag);
                if (hrefMatcher.find()) {
                    pdfLink = hrefMatcher.group(1);
                }
            }
        }

        //updatedAt
        pattern = Pattern.compile("<updated[^>]*>(\\d{4}-\\d{2}-\\d{2})T", Pattern.DOTALL);
        matcher = pattern.matcher(paperInfo);
        String updatedAt = null;
        if (matcher.find()) {
            updatedAt = matcher.group(1);
        }

        //PublishedAt
        pattern = Pattern.compile("<published[^>]*>(\\d{4}-\\d{2}-\\d{2})T", Pattern.DOTALL);
        matcher = pattern.matcher(paperInfo);
        String publishedAt = null;
        if (matcher.find()) {
            publishedAt = matcher.group(1);
        }

        //TO DO - replace the hardcoded zero!!!
        return new Paper(title, 0, summary, LocalDate.parse(publishedAt), LocalDate.parse(updatedAt), authors, pdfLink);
    }
}
