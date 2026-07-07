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

    String buildSimpleRequest(SummaryType summaryType) {
        //Replace placeholders with real text
        String basicRequset = "You are summarizing a scientific paper. Follow these rules strictly:\n" +
                "- Use ONLY information explicitly present in the provided text. Do not add facts from general knowledge.\n" +
                "- Every number, unit, and formula you mention must appear in the source text exactly as stated. Do not round, approximate, or substitute one figure for another.\n" +
                "- If a number is associated with a specific claim or measurement in the text, keep that exact association — do not attach a number to a different claim than the one it originally supports.\n" +
                "- If information needed to answer is not present in the text, state that explicitly rather than inferring or guessing.\n" +
                "- Do not use LaTeX formatting in your output; describe formulas and symbols in plain words.";
        String specificRequest = "";
        switch (summaryType) {
            case TLDR:
                specificRequest = "{shared rules above}" +
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
                specificRequest = "{shared rules above}\n" +
                        "\n" +
                        "Write a 5-10 paragraph summary of the following paper for a technically literate but non-specialist reader. Do not follow the original section structure — instead, organize the summary around the logical flow of the argument: motivation, approach, key results, and implications.\n" +
                        "\n" +
                        "Explain technical terms and mechanisms in accessible language where possible, but do not sacrifice precision on specific values, thresholds, or quantitative results — these must remain exact.\n" +
                        "\n" +
                        "Paper text:\n" +
                        "{paper_text}";
                break;
            case STRUCTURED:
                specificRequest = "{shared rules above}\n" +
                        "\n" +
                        "Summarize the following paper section by section, preserving its original structure. For each section provided, write a concise summary that captures its main claims, methods, and results.\n" +
                        "\n" +
                        "Use the same section headings as in the source text. Do not merge sections together or reorder them. If a section is purely mathematical/derivational with little narrative content, briefly describe what is being derived and why, without reproducing the derivation itself.\n" +
                        "\n" +
                        "Paper text (organized by section):\n" +
                        "{paper_text}";
                break;
            case RESEARCH_NOTE:
                specificRequest = "{shared rules above}\n" +
                        "\n" +
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
        }
        return basicRequset + specificRequest;
    }

    String buildHardRequest(int number, SummaryType summaryType) {
        //Replace {text} with real matching data
        String reducePrompt = "";
        String mapRequest = "";

        switch (summaryType) {
            case TLDR:
                mapRequest = "{shared rules}\n" +
                        "\n" +
                        "This is part {chunk_number} of {total_chunks} of a longer scientific paper. You do not have the full paper — only this excerpt.\n" +
                        "\n" +
                        "Extract the key factual points from this excerpt as a short bullet list (not prose). For each point, keep exact numbers, units, and their associated claims intact — do not paraphrase or round figures. If this excerpt contains no significant new information (e.g., it is mostly references or acknowledgments), say so briefly instead of forcing a summary.\n" +
                        "\n" +
                        "Do not attempt to write a conclusion or final summary — this is only one part of the paper.\n" +
                        "\n" +
                        "Excerpt:\n" +
                        "{chunk_text}";

                reducePrompt = "{shared rules}\n" +
                        "\n" +
                        "Below are extracted key points from consecutive parts of a scientific paper, in order. Combine them into a single TLDR summary of 5-10 sentences covering the core question, method, key findings, and conclusion.\n" +
                        "\n" +
                        "Do not introduce new facts or numbers that are not present in the extracted points below. If the extracted points conflict on some detail, prefer the version that appears in a later part (likely results/conclusion) over an earlier part (likely introduction), and note the paper's own resolution if the conflict is addressed in the points below — otherwise, do not force a resolution: pick the most specific and clearly stated version.\n" +
                        "\n" +
                        "Extracted points from all parts:\n" +
                        "{combined_map_outputs}";
                break;

            case EXECUTIVE:
                mapRequest = "{shared rules}\n" +
                        "\n" +
                        "This is part {chunk_number} of {total_chunks} of a longer scientific paper.\n" +
                        "\n" +
                        "Write a concise paragraph (3-6 sentences) summarizing the content of this excerpt for a technically literate non-specialist reader. Preserve exact figures and their associated claims. Do not write introductory or concluding remarks — this is a middle piece that will be combined with others.\n" +
                        "\n" +
                        "Excerpt:\n" +
                        "{chunk_text}";

                reducePrompt = "{shared rules}\n" +
                        "\n" +
                        "Below are paragraph summaries of consecutive parts of a scientific paper, in order. Weave them into a cohesive 5-10 paragraph executive summary with a natural narrative flow (motivation → approach → results → implications). You may reorder or merge points for readability, but do not alter any numbers, units, or their associated claims from the source paragraphs below.\n" +
                        "\n" +
                        "Paragraph summaries in order:\n" +
                        "{combined_map_outputs}";
                break;

            case STRUCTURED:
                mapRequest = "{shared rules}\n" +
                        "\n" +
                        "This is one section of a scientific paper, titled \"{section_title}\".\n" +
                        "\n" +
                        "Summarize this section concisely, capturing its main claims, methods, and results. Preserve exact figures and their associated claims. If this section is purely mathematical/derivational with little narrative content, briefly describe what is being derived and why, without reproducing the derivation itself.\n" +
                        "\n" +
                        "Section text:\n" +
                        "{chunk_text}";

                reducePrompt = "";
                break;

            case RESEARCH_NOTE:
                mapRequest = "{shared rules}\n" +
                        "\n" +
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

                reducePrompt = "{shared rules}\n" +
                        "\n" +
                        "Below are extracted findings for six questions, gathered separately from consecutive parts of a scientific paper. For each question, combine the relevant findings (ignoring \"Nothing found\" entries) into one clear answer. If truly no part found anything relevant to a question, answer \"Not stated in the provided text\" for that question. Do not invent or infer beyond what is listed below.\n" +
                        "\n" +
                        "Extracted findings from all parts:\n" +
                        "{combined_map_outputs}";
                break;

        }

        return mapRequest + reducePrompt;
    }



    private String buildRequest(String parameters) {
        return baseUrl + parameters;
    }

    public String getOllamaSummary(String content, SummaryType summaryType) {
        //Default type for now
        summaryType = SummaryType.STRUCTURED;

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(baseUrl)).build();

        return null;
    }
    String getOllamaSummary(List<String> content, SummaryType summaryType) {

        //Default type for now
        summaryType = SummaryType.STRUCTURED;



        return null;
    }







}
