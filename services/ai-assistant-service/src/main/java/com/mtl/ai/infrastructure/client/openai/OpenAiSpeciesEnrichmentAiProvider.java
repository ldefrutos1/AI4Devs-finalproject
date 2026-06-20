package com.mtl.ai.infrastructure.client.openai;

import com.mtl.ai.application.AiResponseErrorMessages;
import com.mtl.ai.application.SpeciesEnrichmentAiProvider;
import com.mtl.ai.config.OpenAiProperties;
import com.mtl.ai.exception.AiAssistantException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "mtl.ai.provider", name = "mode", havingValue = "openai")
public class OpenAiSpeciesEnrichmentAiProvider implements SpeciesEnrichmentAiProvider {

  private final OpenAiResponsesClient openAiResponsesClient;
  private final OpenAiProperties openAiProperties;

  public OpenAiSpeciesEnrichmentAiProvider(
      OpenAiResponsesClient openAiResponsesClient, OpenAiProperties openAiProperties) {
    this.openAiResponsesClient = openAiResponsesClient;
    this.openAiProperties = openAiProperties;
  }

  @Override
  public ProviderResponse requestSuggestion(
      String prompt, String scientificName, String commonName) {
    String model = openAiProperties.enrichmentModel();
    String rawJson = openAiResponsesClient.createJsonObjectResponse(model, prompt);
    if (rawJson == null || rawJson.isBlank()) {
      throw new AiAssistantException(
          HttpStatus.NOT_FOUND,
          AiResponseErrorMessages.TITLE_NOT_FOUND,
          AiResponseErrorMessages.DETAIL_NOT_FOUND);
    }
    return new ProviderResponse(
        rawJson,
        "openai:%s:%s".formatted(model, scientificName.trim()));
  }
}
