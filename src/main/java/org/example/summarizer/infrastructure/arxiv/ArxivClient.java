package org.example.summarizer.infrastructure.arxiv;

import org.example.summarizer.domain.Paper;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArxivClient {

    private String baseUrl = "http://export.arxiv.org/api/query?search_query=";
//    String parameters;

    public List<Paper> paperResponse(String parameters) throws IOException, InterruptedException {
        String resultingUrl = baseUrl + parameters;
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
            return paperCollector(StringToListConverter(response.body()));
        }
        catch (IOException e) {
            Thread.currentThread().interrupt();
            httpClient.close();
            throw new RuntimeException("Failed to get response!", e);
        }
    }

    public List<String> StringToListConverter (String apiResponse) {
        List<String> Papers = new ArrayList<>();

        Pattern pattern = Pattern.compile("<entry>(.*?)</entry>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(apiResponse);

        int i = 0;
        while (matcher.find() || i >= 19)  {
            i++;
            Papers.add(matcher.group(1));
        }

        return Papers;
    }

    public String parsePaper(String paperInfo) {

        String parsedPaperData = null;

        while (true) {
            break;



        }

        return parsedPaperData;
    }

    public List<Paper> paperCollector(List<String> entries) {

        return null;
    }



    // "http://export.arxiv.org/api/query?search_query=all:electron&start=0&max_results=1"
}
