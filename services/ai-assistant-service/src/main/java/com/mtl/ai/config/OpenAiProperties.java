package com.mtl.ai.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "mtl.ai.openai")
public record OpenAiProperties(
    String apiKey,
    @DefaultValue("https://api.openai.com") String baseUrl,
    @DefaultValue("gpt-4.1-mini") String enrichmentModel,
    @DefaultValue("gpt-4.1-mini") String chatModel,
    @DefaultValue("5s") Duration connectTimeout,
    @DefaultValue("60s") Duration readTimeout,
    Retry retry) {

  public OpenAiProperties {
    if (retry == null) {
      retry = new Retry(3, Duration.ofMillis(500), Duration.ofSeconds(3));
    }
  }

  public record Retry(
      @DefaultValue("3") int maxAttempts,
      @DefaultValue("500ms") Duration initialBackoff,
      @DefaultValue("3s") Duration maxBackoff) {}
}
