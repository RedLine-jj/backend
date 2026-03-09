package com.jj.redline.common.config;

import com.jj.redline.crawling.modeman.list.ModeManListParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public ModeManListParser modeManListParser() {
        return new ModeManListParser();
    }
}
