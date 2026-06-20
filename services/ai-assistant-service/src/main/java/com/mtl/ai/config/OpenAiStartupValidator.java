package com.mtl.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Falla al arranque si falta configuración imprescindible para OpenAI en producción. */
@Component
@ConditionalOnProperty(prefix = "mtl.ai.provider", name = "mode", havingValue = "openai")
public class OpenAiStartupValidator {

  private final OpenAiProperties openAiProperties;

  public OpenAiStartupValidator(OpenAiProperties openAiProperties) {
    this.openAiProperties = openAiProperties;
  }

  @EventListener(ApplicationReadyEvent.class)
  void validateApiKeyPresent() {
    if (openAiProperties.apiKey() == null || openAiProperties.apiKey().isBlank()) {
      throw new IllegalStateException(
          "mtl.ai.provider.mode=openai requiere MTL_OPENAI_API_KEY (o OPENAI_API_KEY) configurada.");
    }
  }
}
