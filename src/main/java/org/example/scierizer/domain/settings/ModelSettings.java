package org.example.scierizer.domain.settings;

public record ModelSettings(
        String provider,
        String modelName,
        String ollamaBaseUrl,
        String contextMode,
        String cloudEndpoint,
        String apiKeyReference,
        boolean rememberApiKey,
        int contextTokens,
        int maxOutputTokens,
        int requestTimeoutMinutes
) {
}
