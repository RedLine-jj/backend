package com.jj.redline.common.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "llm")
public class OpenAiProperties {

    private String apiKey;
    private String model = "llama-3.3-70b-versatile";
    private String baseUrl = "https://api.groq.com/openai/v1/chat/completions";
}
