package org.example.summarizer.infrastructure.arxiv;

import org.example.summarizer.domain.Paper;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
//    String parameters;

    public List<Paper> paperResponse(String parameters) throws IOException, InterruptedException {
        String resultingUrl = baseUrl + parameters;
        System.out.println(resultingUrl);
        URL searchUrl = new URL(resultingUrl);

        BufferedReader br = null;
        StringBuilder str = new StringBuilder();
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(resultingUrl)).timeout(Duration.ofSeconds(10)).GET().build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException("ArXiv returned error!");
            }


            //Make that shi
            List<String> papersAsStrings = StringToListConverter(response.body());
            List<Paper> resultingPapers = new ArrayList<>();

            for(String paperAsString : papersAsStrings) {
                resultingPapers.add(parsePaper(paperAsString));
            }

//            return paperCollector(StringToListConverter(response.body()));
            return resultingPapers;
        }
        catch (IOException e) {
            Thread.currentThread().interrupt();
//            httpClient.close();
            throw new RuntimeException("Failed to get response!", e);
        }
    }

    //Converts a single string to a list of strings for different papers
    public List<String> StringToListConverter (String apiResponse) {

        Pattern pattern = Pattern.compile("<entry[^>]*>(.*?)</entry>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(apiResponse);
        List<String> papersAsStrings =  new ArrayList<>();
        int i = 0;

        while(matcher.find() && i < 20) {
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

    // Extracts metadata from API response
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
        return new Paper(title, String.valueOf(0), summary, LocalDate.parse(publishedAt), LocalDate.parse(updatedAt), authors, pdfLink);
    }
}
