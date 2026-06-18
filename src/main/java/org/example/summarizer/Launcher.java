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
    }
}
