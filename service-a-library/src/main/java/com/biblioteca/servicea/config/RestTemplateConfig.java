package com.biblioteca.servicea.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Value("${loans.connect-timeout-ms}")
    private int connectTimeoutMs;

    @Value("${loans.read-timeout-ms}")
    private int readTimeoutMs;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                    .setReadTimeout(Duration.ofMillis(connectTimeoutMs))
                    .build();
    }

}
