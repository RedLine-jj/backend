package com.jj.redline.common.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import com.jj.redline.common.ai.AiModelMatcher.MatchStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AiModelMatcherIntegrationTest {

    static AiModelMatcher matcher;
    static boolean enabled;

    @BeforeAll
    static void setUp() {
        enabled = "true".equalsIgnoreCase(System.getProperty("it"))
                || "true".equalsIgnoreCase(System.getenv("IT"));

        String apiKey = System.getenv("GROQ_API_KEY");

        if (enabled && apiKey != null && !apiKey.isBlank()) {
            OpenAiProperties props = new OpenAiProperties();
            props.setApiKey(apiKey);
            props.setModel("llama-3.3-70b-versatile");
            props.setBaseUrl("https://api.groq.com/openai/v1/chat/completions");

            OpenAiClient client = new OpenAiClient(props, new ObjectMapper());
            matcher = new AiModelMatcher(client, new ObjectMapper());
        }
    }

    @Test
    void sugarcane_same_product_different_name() {
        assumeTrue(enabled && matcher != null, "skipped: add -Dit=true and GROQ_API_KEY env");

        var result = matcher.findMatch(
                "SUGARCANE",
                "13oz. DENIM BLOUSE 1936 MODEL (One Wash)",
                List.of(
                        "SC11936 13oz Denim Blouse 1936 Model A.Navy",
                        "SC41947 DENIM JACKET 1953 Model",
                        "SC42966 14oz DENIM PANTS 1947 Model"
                )
        );

        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
        assertThat(result.matchedName()).isEqualTo("SC11936 13oz Denim Blouse 1936 Model A.Navy");
        assertThat(result.confidence()).isGreaterThanOrEqualTo(85);
    }

    @Test
    void fullcount_same_product_abbreviated() {
        assumeTrue(enabled && matcher != null, "skipped: add -Dit=true and GROQ_API_KEY env");

        var result = matcher.findMatch(
                "FULLCOUNT",
                "0105W Wide Denim",
                List.of(
                        "0105W WIDE STRAIGHT DENIM",
                        "1101 USED WASH LIGHT INDIGO",
                        "1101W ORIGINAL STRAIGHT DENIM"
                )
        );

        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
        assertThat(result.matchedName()).isEqualTo("0105W WIDE STRAIGHT DENIM");
        assertThat(result.confidence()).isGreaterThanOrEqualTo(85);
    }

    @Test
    void fullcount_different_model_code_should_not_match() {
        assumeTrue(enabled && matcher != null, "skipped: add -Dit=true and GROQ_API_KEY env");

        var result = matcher.findMatch(
                "FULLCOUNT",
                "1101BK STRAIGHT DENIM BLACK x WHITE",
                List.of(
                        "1101 USED WASH LIGHT INDIGO",
                        "1101W ORIGINAL STRAIGHT DENIM",
                        "1101SSW SUPER SMOOTH STRAIGHT DENIM",
                        "0105W WIDE STRAIGHT DENIM"
                )
        );

        assertThat(result.status()).isEqualTo(MatchStatus.NO_MATCH);
    }

    @Test
    void warehouse_same_product_lot_prefix() {
        assumeTrue(enabled && matcher != null, "skipped: add -Dit=true and GROQ_API_KEY env");

        var result = matcher.findMatch(
                "WAREHOUSE",
                "Lot.1001XX Denim (One Wash)",
                List.of(
                        "1001XX ORIGINAL STRAIGHT JEANS",
                        "800XX STANDARD JEANS",
                        "900XX SLIM FIT JEANS"
                )
        );

        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
        assertThat(result.matchedName()).isEqualTo("1001XX ORIGINAL STRAIGHT JEANS");
        assertThat(result.confidence()).isGreaterThanOrEqualTo(85);
    }

    @Test
    void sugarcane_different_code_SC15708_vs_SC15655_should_not_match() {
        assumeTrue(enabled && matcher != null, "skipped: add -Dit=true and GROQ_API_KEY env");

        var result = matcher.findMatch(
                "SUGARCANE",
                "[ SC15708 ] 11oz. Blue Denim Work Coat Aged Model",
                List.of(
                        "SC15655 A.Navy 11oz Blue Denim Work Coat",
                        "SC14966 11oz Blue Denim Chore Coat",
                        "SC41947 DENIM JACKET 1953 Model",
                        "[ SC15655 ] 11oz. Blue Denim Work Coat"
                )
        );

        assertThat(result.status()).isEqualTo(MatchStatus.NO_MATCH);
    }

    @Test
    void sugarcane_aged_model_without_code_should_not_match() {
        assumeTrue(enabled && matcher != null, "skipped: add -Dit=true and GROQ_API_KEY env");

        var result = matcher.findMatch(
                "SUGARCANE",
                "[ SC15708 ] 11oz. Blue Denim Work Coat Aged Model",
                List.of(
                        "SC15655 A.Navy 11oz Blue Denim Work Coat",
                        "SC14966 11oz Blue Denim Chore Coat",
                        "SC41947 DENIM JACKET 1953 Model"
                )
        );

        assertThat(result.status()).isEqualTo(MatchStatus.NO_MATCH);
    }

    @Test
    void fullcount_completely_different_should_not_match() {
        assumeTrue(enabled && matcher != null, "skipped: add -Dit=true and GROQ_API_KEY env");

        var result = matcher.findMatch(
                "FULLCOUNT",
                "2680 Type 1 Denim Jacket",
                List.of(
                        "0105W WIDE STRAIGHT DENIM",
                        "1101 USED WASH LIGHT INDIGO",
                        "S407XX 1942 WPB L-181 War Model"
                )
        );

        assertThat(result.status()).isEqualTo(MatchStatus.NO_MATCH);
    }
}
