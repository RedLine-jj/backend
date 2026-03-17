package com.jj.redline.common.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiModelMatcher {

    private static final String SYSTEM_PROMPT = """
            You are a Japanese denim product matching engine for Korean retail stores.
            Given a brand name, a NEW product name, and a list of EXISTING product names, determine if the new product is the same as any existing one.

            Rules:
            - Color/wash variations (e.g. \"A.Navy\" vs \"One Wash\", \"Indigo\" vs \"Black\") = SAME product
            - Different model codes (e.g. \"1101\" vs \"1101BK\" vs \"1101W\") = DIFFERENT products
            - Abbreviated vs full names (e.g. \"Wide Denim\" vs \"WIDE STRAIGHT DENIM\") = SAME product
            - Model code presence vs absence doesn't matter if other details match (e.g. \"SC11936 13oz Denim Blouse 1936 Model\" = \"13oz. DENIM BLOUSE 1936 MODEL\")

            Respond ONLY with JSON: {\"match\": \"exact existing name or null\", \"confidence\": 0-100}
            No explanation needed.
            """;

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public MatchResult findMatch(String brandName, String newModelName, List<String> existingModelNames) {
        if (existingModelNames == null || existingModelNames.isEmpty()) {
            return MatchResult.noMatch();
        }

        String userPrompt = buildUserPrompt(brandName, newModelName, existingModelNames);
        String rawResponse = openAiClient.chat(SYSTEM_PROMPT, userPrompt);

        if (rawResponse == null || rawResponse.isBlank()) {
            return MatchResult.error();
        }

        try {
            String jsonResponse = rawResponse.strip();
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").strip();
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            int confidence = root.path("confidence").asInt(0);
            if (confidence < 85) {
                return MatchResult.noMatch();
            }

            JsonNode matchNode = root.get("match");
            String matchedName = null;
            if (matchNode != null && !matchNode.isNull()) {
                String candidate = matchNode.asText();
                if (candidate != null && !candidate.isBlank() && existingModelNames.contains(candidate)) {
                    matchedName = candidate;
                }
            }

            return matchedName != null
                    ? MatchResult.matched(matchedName, confidence)
                    : MatchResult.noMatch();
        } catch (Exception e) {
            log.warn("[AiModelMatcher] response parse failed: {}", e.getMessage());
            return MatchResult.error();
        }
    }

    private String buildUserPrompt(String brandName, String newModelName, List<String> existingModelNames) {
        StringBuilder builder = new StringBuilder();
        builder.append("Brand: ").append(brandName).append('\n');
        builder.append("New model: ").append(newModelName).append('\n');
        builder.append("Existing models:\n");
        for (String existingModelName : existingModelNames) {
            builder.append("- ").append(existingModelName).append('\n');
        }
        return builder.toString();
    }

    public enum MatchStatus { MATCHED, NO_MATCH, ERROR }

    public record MatchResult(MatchStatus status, String matchedName, int confidence) {
        public static MatchResult matched(String name, int confidence) {
            return new MatchResult(MatchStatus.MATCHED, name, confidence);
        }
        public static MatchResult noMatch() {
            return new MatchResult(MatchStatus.NO_MATCH, null, 0);
        }
        public static MatchResult error() {
            return new MatchResult(MatchStatus.ERROR, null, 0);
        }
    }
}
