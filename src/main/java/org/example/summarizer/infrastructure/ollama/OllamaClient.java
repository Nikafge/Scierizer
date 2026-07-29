package org.example.summarizer.infrastructure.ollama;


import org.example.summarizer.viewmodel.SummaryType;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.summarizer.domain.settings.AppSettings;
import org.example.summarizer.domain.settings.ModelSettings;
import org.example.summarizer.domain.settings.ProcessingSettings;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    private record ChatMessage(
            String role,
            String content
    ) {}
    private record OpenAiChatRequest(
            String model,
            List<ChatMessage> messages,
            int max_completion_tokens,
            boolean stream
    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiChatResponse(
            List<OpenAiChoice> choices
    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiChoice(
            OpenAiMessage message
    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiMessage(
            String content
    ) {}
    private record AnthropicMessageRequest(
            String model,
            String system,
            List<ChatMessage> messages,
            int max_tokens,
            boolean stream
    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AnthropicMessageResponse(
            List<AnthropicContentBlock> content
    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AnthropicContentBlock(
            String type,
            String text
    ) {}
    private final String basicRequest = "You are summarizing a scientific paper. Follow these rules strictly:\n" +
            "- Use ONLY information explicitly present in the provided text. Do not add facts from general knowledge.\n" +
            "- Do NOT try to include formulas from paper. Only describe them\n" +
            "- If a number is associated with a specific claim or measurement in the text, keep that exact association — do not attach a number to a different claim than the one it originally supports.\n" +
            "- If information needed to answer is not present in the text, state that explicitly rather than inferring or guessing.\n" +
            "- Do not use LaTeX formatting in your output; describe formulas and symbols in plain words.";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaClient() {
        this(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build(),
                new ObjectMapper()
        );
    }

    public OllamaClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    private String getModelResponse(String request, ModelSettings modelSettings) {
        return switch (normalizedProvider(modelSettings)) {
            case "ollama" -> getOllamaResponse(request, modelSettings);
            case "openai" -> getOpenAiCompatibleResponse(request, modelSettings, false);
            case "custom" -> getOpenAiCompatibleResponse(request, modelSettings, true);
            case "anthropic" -> getAnthropicResponse(request, modelSettings);
            default -> throw new IllegalArgumentException("Unsupported model provider: " + modelSettings.provider());
        };
    }

    //Method to send request to Ollama
    private String getOllamaResponse(String request, ModelSettings modelSettings) {
        OllamaGenerateRequest ollamaRequest = new OllamaGenerateRequest(
                modelSettings.modelName(),
                request,
                basicRequest,
                false
        );
        try {
            String requestBody = objectMapper.writeValueAsString(ollamaRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(generateEndpoint(modelSettings.ollamaBaseUrl())))
                    .timeout(Duration.ofMinutes(Math.max(1, modelSettings.requestTimeoutMinutes())))
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to establish connection with Ollama", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to establish connection with Ollama", e);
        }
    }

    private String getOpenAiCompatibleResponse(String request, ModelSettings modelSettings, boolean customProvider) {
        String providerName = customProvider ? "custom cloud provider" : "OpenAI";
        OpenAiChatRequest chatRequest = new OpenAiChatRequest(
                modelSettings.modelName(),
                List.of(
                        new ChatMessage("developer", basicRequest),
                        new ChatMessage("user", request)
                ),
                Math.max(1, modelSettings.maxOutputTokens()),
                false
        );

        try {
            String requestBody = objectMapper.writeValueAsString(chatRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(generateOpenAiCompatibleEndpoint(modelSettings, customProvider)))
                    .timeout(Duration.ofMinutes(Math.max(1, modelSettings.requestTimeoutMinutes())))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey(
                            modelSettings,
                            customProvider ? "CLOUD_LLM_API_KEY" : "OPENAI_API_KEY",
                            providerName
                    ))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
                throw new RuntimeException(
                        providerName + " returned status " + httpResponse.statusCode()
                                + ": " + httpResponse.body()
                );
            }

            OpenAiChatResponse chatResponse =
                    objectMapper.readValue(httpResponse.body(), OpenAiChatResponse.class);

            if (chatResponse.choices() == null
                    || chatResponse.choices().isEmpty()
                    || chatResponse.choices().get(0).message() == null
                    || chatResponse.choices().get(0).message().content() == null) {
                throw new RuntimeException(providerName + " response did not include message content");
            }

            return chatResponse.choices().get(0).message().content().trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to establish connection with " + providerName, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to establish connection with " + providerName, e);
        }
    }

    private String getAnthropicResponse(String request, ModelSettings modelSettings) {
        AnthropicMessageRequest messageRequest = new AnthropicMessageRequest(
                modelSettings.modelName(),
                basicRequest,
                List.of(new ChatMessage("user", request)),
                Math.max(1, modelSettings.maxOutputTokens()),
                false
        );

        try {
            String requestBody = objectMapper.writeValueAsString(messageRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(generateAnthropicEndpoint(modelSettings)))
                    .timeout(Duration.ofMinutes(Math.max(1, modelSettings.requestTimeoutMinutes())))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey(modelSettings, "ANTHROPIC_API_KEY", "Anthropic"))
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
                throw new RuntimeException(
                        "Anthropic returned status " + httpResponse.statusCode()
                                + ": " + httpResponse.body()
                );
            }

            AnthropicMessageResponse messageResponse =
                    objectMapper.readValue(httpResponse.body(), AnthropicMessageResponse.class);

            if (messageResponse.content() == null || messageResponse.content().isEmpty()) {
                throw new RuntimeException("Anthropic response did not include message content");
            }

            return messageResponse.content().stream()
                    .filter(block -> block.text() != null)
                    .map(AnthropicContentBlock::text)
                    .collect(Collectors.joining("\n"))
                    .trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to establish connection with Anthropic", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to establish connection with Anthropic", e);
        }
    }

    //create a request for AI to get summary
    public String generateSimpleSummary(SummaryType summaryType, String content) {
        AppSettings defaults = AppSettings.defaults();
        return generateSimpleSummary(summaryType, content, defaults.model(), defaults.processing());
    }

    public String generateSimpleSummary(SummaryType summaryType, String content, ModelSettings modelSettings) {
        return generateSimpleSummary(summaryType, content, modelSettings, AppSettings.defaults().processing());
    }

    public String generateSimpleSummary(
            SummaryType summaryType,
            String content,
            ModelSettings modelSettings,
            ProcessingSettings processingSettings
    ) {

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
        return getModelResponse(
                appendProcessingInstructions(specificRequest, processingSettings).replace("{paper_text}", content),
                modelSettings
        );
//        return getOllamaResponse(basicRequest + specificRequest.replace("{paper_text}", content));
    }

    //create and send requests for every paper chunk. Return list of mini-summaries
    private List<String> createMapSummaries(
            SummaryType summaryType,
            List<String> paperChapters,
            ModelSettings modelSettings,
            ProcessingSettings processingSettings
    ) {

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
                mapRequest = appendProcessingInstructions(mapRequest, processingSettings);

                for (int i = 1; i <= paperChapters.size(); i++) {
                    chunks.add(getModelResponse((mapRequest.replace("{chunk_number}", String.valueOf(i))).replace("{chunk_text}", paperChapters.get(i-1)), modelSettings));
                }

        return chunks;
    }

    //create a request for API to get summary from chunks
    public String generateChunkedSummary(SummaryType summaryType, List<String> chapters) {
        AppSettings defaults = AppSettings.defaults();
        return generateChunkedSummary(summaryType, chapters, defaults.model(), defaults.processing());
    }

    public String generateChunkedSummary(SummaryType summaryType, List<String> chapters, ModelSettings modelSettings) {
        return generateChunkedSummary(summaryType, chapters, modelSettings, AppSettings.defaults().processing());
    }

    public String generateChunkedSummary(
            SummaryType summaryType,
            List<String> chapters,
            ModelSettings modelSettings,
            ProcessingSettings processingSettings
    ) {
        String reducePrompt = "";
        List<String> chunks = createMapSummaries(summaryType, chapters, modelSettings, processingSettings);

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
        return getModelResponse(
                appendProcessingInstructions(reducePrompt, processingSettings)
                        .replace("{combined_map_outputs}", String.join("\n", chunks)),
                modelSettings
        );
//        return getOllamaResponse(basicRequest + reducePrompt.replace("{combined_map_outputs}", String.join("\n", chunks)));
    }

    private String generateEndpoint(String baseUrl) {
        String effectiveBaseUrl = baseUrl == null || baseUrl.isBlank()
                ? AppSettings.defaults().model().ollamaBaseUrl()
                : baseUrl.trim();
        String withoutTrailingSlash = effectiveBaseUrl.replaceAll("/+$", "");

        if (withoutTrailingSlash.endsWith("/api/generate")) {
            return withoutTrailingSlash;
        }

        return withoutTrailingSlash + "/api/generate";
    }

    private String generateOpenAiCompatibleEndpoint(ModelSettings modelSettings, boolean customProvider) {
        String configuredEndpoint = modelSettings.cloudEndpoint();
        if (configuredEndpoint == null || configuredEndpoint.isBlank()) {
            if (customProvider) {
                throw new IllegalArgumentException("Cloud endpoint is required for Custom provider");
            }
            configuredEndpoint = "https://api.openai.com/v1/chat/completions";
        }

        String withoutTrailingSlash = configuredEndpoint.trim().replaceAll("/+$", "");
        if (withoutTrailingSlash.endsWith("/chat/completions")) {
            return withoutTrailingSlash;
        }
        if (withoutTrailingSlash.endsWith("/v1")) {
            return withoutTrailingSlash + "/chat/completions";
        }
        return withoutTrailingSlash + "/v1/chat/completions";
    }

    private String generateAnthropicEndpoint(ModelSettings modelSettings) {
        String configuredEndpoint = modelSettings.cloudEndpoint();
        if (configuredEndpoint == null || configuredEndpoint.isBlank()) {
            configuredEndpoint = "https://api.anthropic.com/v1/messages";
        }

        String withoutTrailingSlash = configuredEndpoint.trim().replaceAll("/+$", "");
        if (withoutTrailingSlash.endsWith("/messages")) {
            return withoutTrailingSlash;
        }
        if (withoutTrailingSlash.endsWith("/v1")) {
            return withoutTrailingSlash + "/messages";
        }
        return withoutTrailingSlash + "/v1/messages";
    }

    private String apiKey(ModelSettings modelSettings, String environmentVariableName, String providerName) {
        String configuredKey = modelSettings.apiKeyReference();
        if (configuredKey != null && !configuredKey.isBlank()) {
            return configuredKey.trim();
        }

        String environmentKey = System.getenv(environmentVariableName);
        if (environmentKey != null && !environmentKey.isBlank()) {
            return environmentKey.trim();
        }

        throw new IllegalArgumentException(providerName + " API key is required. Enter it in Settings or set "
                + environmentVariableName + ".");
    }

    private String normalizedProvider(ModelSettings modelSettings) {
        String provider = modelSettings.provider();
        return provider == null || provider.isBlank()
                ? "ollama"
                : provider.trim().toLowerCase(Locale.ROOT);
    }

    private String appendProcessingInstructions(String prompt, ProcessingSettings processingSettings) {
        StringBuilder instructions = new StringBuilder(prompt);
        instructions.append("\n\nAdditional output settings:\n");
        instructions.append("- Write the summary in ")
                .append(processingSettings.outputLanguage())
                .append(".\n");

        if (processingSettings.preserveNumbers()) {
            instructions.append("- Preserve important numerical values and keep them attached to their original claims.\n");
        }
        if (processingSettings.includeEquations()) {
            instructions.append("- Mention important equations in prose when they are explicitly present.\n");
        }
        if (processingSettings.includeReferences()) {
            instructions.append("- Include important references when they are explicitly discussed in the text.\n");
        }
        if (processingSettings.includeFigures()) {
            instructions.append("- Mention important figures and tables when they are relevant to the findings.\n");
        }

        return instructions.toString();
    }

}
