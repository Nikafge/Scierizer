package org.example.summarizer;

import javafx.application.Application;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Launcher {
    public static void main(String[] args) throws IOException {
        Application.launch(HelloApplication.class, args);
//        URL url = new URL("https://export.arxiv.org/api/query?search_query=all:electron&start=0&max_results=1");
//        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.connect();
//        int response = connection.getResponseCode();
//
//        if (response == 200) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String inputLine;
//            StringBuilder responseBuilder = new StringBuilder();
//            while ((inputLine = reader.readLine())!= null) {
//                responseBuilder.append(inputLine).append("\n");
//            }
//            reader.close();
//            System.out.println("---------/Data/--------\n");
//            System.out.println(responseBuilder.toString());
//        }
//        else {
//            System.out.println("Something went wrong");
//        }
    }
}
