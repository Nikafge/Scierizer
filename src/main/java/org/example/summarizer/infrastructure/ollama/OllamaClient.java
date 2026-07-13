package org.example.summarizer.infrastructure.ollama;


import org.example.summarizer.viewmodel.SummaryType;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class OllamaClient {

    private record OllamaGenerateRequest(
            String model,
            String prompt,
            String system,
            boolean stream
    ) {}
@JsonIgnoreProperties(ignoreUnknown = true)
private record OllamaGenerateResponse(
        String response,
        boolean done
) {}
    private final String basicRequest = "You are summarizing a scientific paper. Follow these rules strictly:\n" +
            "- Use ONLY information explicitly present in the provided text. Do not add facts from general knowledge.\n" +
            "- Do NOT try to include formulas from paper. Only describe them\n" +
            "- If a number is associated with a specific claim or measurement in the text, keep that exact association — do not attach a number to a different claim than the one it originally supports.\n" +
            "- If information needed to answer is not present in the text, state that explicitly rather than inferring or guessing.\n" +
            "- Do not use LaTeX formatting in your output; describe formulas and symbols in plain words.";
    private static final String MODEL = "gemma4:e4b";

    //Comment these three lines (37-39) and uncomment lines 41-54 for testing!
    private final String baseUrl = "http://localhost:11434/api/generate";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

//For test purposes only!
//    private final String baseUrl;
//    private final ObjectMapper objectMapper;
//    private final HttpClient httpClient;
//
//    public OllamaClient() {
//        this(
//                "http://localhost:11434/api/generate",
//                HttpClient.newBuilder()
//                        .connectTimeout(Duration.ofSeconds(10))
//                        .build(),
//                new ObjectMapper()
//        );
//    }

    //Method to send request to Ollama
    private String getOllamaResponse (String request) {
        OllamaGenerateRequest ollamaRequest = new OllamaGenerateRequest(MODEL, request, basicRequest, false);
        try {
            String requestBody = objectMapper.writeValueAsString(ollamaRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofMinutes(100))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException(
                        "Ollama returned status " + httpResponse.statusCode()
                                + ": " + httpResponse.body()
                );
            }

            OllamaGenerateResponse ollamaResponse =
                    objectMapper.readValue(httpResponse.body(), OllamaGenerateResponse.class);

            return ollamaResponse.response().trim();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to establish connection with Ollama");
        }
    }

    //create a request for AI to get summary
    public String generateSimpleSummary(SummaryType summaryType, String content) {

        String specificRequest = "";
        switch (summaryType) {
            case TLDR:
                specificRequest =
                        "Summarize the following paper in 5-10 sentences. Include:\n" +
                        "- The core research question or problem being addressed.\n" +
                        "- The main method or approach used.\n" +
                        "- The key finding(s), stated precisely as in the text.\n" +
                        "- The main conclusion or implication of the work.\n" +
                        "\n" +
                        "Write it as a dense, self-contained summary — someone reading only this should understand what the paper found and why it matters, without needing to read further.\n" +
                        "\n" +
                        "Paper text:\n" +
                        "{paper_text}";
                break;
            case EXECUTIVE:
                specificRequest =
                        "Write a 5-10 paragraph summary of the following paper for a technically literate but non-specialist reader. Do not follow the original section structure — instead, organize the summary around the logical flow of the argument: motivation, approach, key results, and implications.\n" +
                        "\n" +
                        "Explain technical terms and mechanisms in accessible language where possible, but do not sacrifice precision on specific values, thresholds, or quantitative results — these must remain exact.\n" +
                        "\n" +
                        "Paper text:\n" +
                        "{paper_text}";
                break;
            case STRUCTURED:
                specificRequest =
                        "Summarize the following paper section by section, preserving its original structure. For each section provided, write a concise summary that captures its main claims, methods, and results.\n" +
                        "\n" +
                        "Use the same section headings as in the source text. Do not merge sections together or reorder them. If a section is purely mathematical/derivational with little narrative content, briefly describe what is being derived and why, without reproducing the derivation itself.\n" +
                        "\n" +
                        "Paper text (organized by section):\n" +
                        "{paper_text}";
                break;
            case RESEARCH_NOTE:
                specificRequest =
                        "Additionally, for this task specifically:\n" +
                        "- Only describe the paper's contribution, novelty, or relation to prior work if the text EXPLICITLY states it (e.g. \"unlike previous studies...\", \"we extend the work of...\", \"in contrast to X, we...\"). Do not infer novelty or comparisons that are not directly stated in the text.\n" +
                        "- If the paper does not explicitly discuss how it differs from prior work, say so rather than guessing.\n" +
                        "\n" +
                        "Analyze the following paper and answer these questions, citing the relevant part of the text for each:\n" +
                        "1. What is the specific contribution of this work?\n" +
                        "2. How does the paper position itself relative to prior work (only if explicitly stated)?\n" +
                        "3. What are the stated strengths of the approach?\n" +
                        "4. What assumptions does the paper explicitly rely on?\n" +
                        "5. What limitations does the paper acknowledge (explicitly stated only — do not invent limitations)?\n" +
                        "6. What future research directions does the paper suggest, if any?\n" +
                        "\n" +
                        "If any of these questions cannot be answered from the given text, explicitly write \"Not stated in the provided text\" for that question instead of guessing.\n" +
                        "\n" +
                        "Paper text:\n" +
                        "{paper_text}";
                break;
            default:
                throw new IllegalArgumentException("Unsupported summary type");
        }
        return getOllamaResponse(specificRequest.replace("{paper_text}", content));
//        return getOllamaResponse(basicRequest + specificRequest.replace("{paper_text}", content));
    }

    //create and send requests for every paper chunk. Return list of mini-summaries
    private List<String> createMapSummaries (SummaryType summaryType, List<String> paperChapters) {

        String mapRequest = "";
        List<String> chunks = new ArrayList<>();

        switch (summaryType) {
            case TLDR:
                mapRequest =
                        "This is part {chunk_number} of {total_chunks} of a longer scientific paper. You do not have the full paper — only this excerpt.\n" +
                                "\n" +
                                "Extract the key factual points from this excerpt as a short bullet list (not prose). For each point, keep exact numbers, units, and their associated claims intact — do not paraphrase or round figures. If this excerpt contains no significant new information (e.g., it is mostly references or acknowledgments), say so briefly instead of forcing a summary.\n" +
                                "\n" +
                                "Do not attempt to write a conclusion or final summary — this is only one part of the paper.\n" +
                                "\n" +
                                "Excerpt:\n" +
                                "{chunk_text}";

                break;

            case EXECUTIVE:
                mapRequest =
                        "This is part {chunk_number} of {total_chunks} of a longer scientific paper.\n" +
                        "\n" +
                        "Write a concise paragraph (3-6 sentences) summarizing the content of this excerpt for a technically literate non-specialist reader. Preserve exact figures and their associated claims. Do not write introductory or concluding remarks — this is a middle piece that will be combined with others.\n" +
                        "\n" +
                        "Excerpt:\n" +
                        "{chunk_text}";

                break;

            case STRUCTURED:
                mapRequest =
                        "Summarize this section concisely, capturing its main claims, methods, and results. Preserve exact figures and their associated claims. If this section is purely mathematical/derivational with little narrative content, briefly describe what is being derived and why, without reproducing the derivation itself.\n" +
                        "\n" +
                        "Section text:\n" +
                        "{chunk_text}";

                break;

            case RESEARCH_NOTE:
                mapRequest =
                        "This is part {chunk_number} of {total_chunks} of a scientific paper. For each of the following questions, extract any relevant statement found explicitly in this excerpt. If nothing relevant to a question appears here, write \"Nothing found in this excerpt\" for that question — do not guess or infer.\n" +
                        "\n" +
                        "1. Contribution of this work (if explicitly stated here)\n" +
                        "2. Relation to prior work (only if explicitly stated here)\n" +
                        "3. Strengths of the approach (if explicitly stated here)\n" +
                        "4. Assumptions relied upon (if explicitly stated here)\n" +
                        "5. Limitations acknowledged (if explicitly stated here)\n" +
                        "6. Future research directions suggested (if explicitly stated here)\n" +
                        "\n" +
                        "Excerpt:\n" +
                        "{chunk_text}";


                break;

            default:
                throw new IllegalArgumentException("Unsupported summary type");
        }

                mapRequest = mapRequest.replace("{total_chunks}", String.valueOf(paperChapters.size()));

                for (int i = 1; i <= paperChapters.size(); i++) {
                    chunks.add(getOllamaResponse((mapRequest.replace("{chunk_number}", String.valueOf(i))).replace("{chunk_text}", paperChapters.get(i-1))));
                }

        return chunks;
    }

    //create a request for API to get summary from chunks
    public String generateChunkedSummary(SummaryType summaryType, List<String> chapters) {
        String reducePrompt = "";
        List<String> chunks = createMapSummaries(summaryType, chapters);

        switch (summaryType) {
            case TLDR:
                reducePrompt =
                        "Below are extracted key points from consecutive parts of a scientific paper, in order. Combine them into a single TLDR summary of 5-10 sentences covering the core question, method, key findings, and conclusion.\n" +
                        "\n" +
                        "Do not introduce new facts or numbers that are not present in the extracted points below. If the extracted points conflict on some detail, prefer the version that appears in a later part (likely results/conclusion) over an earlier part (likely introduction), and note the paper's own resolution if the conflict is addressed in the points below — otherwise, do not force a resolution: pick the most specific and clearly stated version.\n" +
                        "\n" +
                        "Extracted points from all parts:\n" +
                        "{combined_map_outputs}";


                break;

            case EXECUTIVE:
                reducePrompt =
                        "Below are paragraph summaries of consecutive parts of a scientific paper, in order. Weave them into a cohesive 5-10 paragraph executive summary with a natural narrative flow (motivation → approach → results → implications). You may reorder or merge points for readability, but do not alter any numbers, units, or their associated claims from the source paragraphs below.\n" +
                        "\n" +
                        "Paragraph summaries in order:\n" +
                        "{combined_map_outputs}";

                break;

            case STRUCTURED:
                reducePrompt = "";
                break;

            case RESEARCH_NOTE:
                reducePrompt =
                        "Below are extracted findings for six questions, gathered separately from consecutive parts of a scientific paper. For each question, combine the relevant findings (ignoring \"Nothing found\" entries) into one clear answer. If truly no part found anything relevant to a question, answer \"Not stated in the provided text\" for that question. Do not invent or infer beyond what is listed below.\n" +
                        "\n" +
                        "Extracted findings from all parts:\n" +
                        "{combined_map_outputs}";
                break;

            default:
                throw new IllegalArgumentException("Unsupported summary type");
        }

        if (reducePrompt.isEmpty()) {
            return String.join("\n", chunks);
        }
        return getOllamaResponse(reducePrompt.replace("{combined_map_outputs}", String.join("\n", chunks)));
//        return getOllamaResponse(basicRequest + reducePrompt.replace("{combined_map_outputs}", String.join("\n", chunks)));
    }

}
