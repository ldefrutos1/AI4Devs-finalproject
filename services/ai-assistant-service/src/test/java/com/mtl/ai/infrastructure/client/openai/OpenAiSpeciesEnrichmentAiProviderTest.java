package com.mtl.ai.infrastructure.client.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.ai.config.OpenAiProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenAiSpeciesEnrichmentAiProviderTest {

  @Mock private OpenAiResponsesClient openAiResponsesClient;
  @Mock private OpenAiProperties openAiProperties;

  @InjectMocks private OpenAiSpeciesEnrichmentAiProvider provider;

  @Test
  void requestSuggestion_delegatesToOpenAiClient() {
    when(openAiProperties.enrichmentModel()).thenReturn("gpt-4.1-mini");
    when(openAiResponsesClient.createJsonObjectResponse(eq("gpt-4.1-mini"), eq("prompt")))
        .thenReturn("{\"synonyms\":[\"Encina\"]}");

    var response = provider.requestSuggestion("prompt", "Quercus ilex", "Encina");

    assertThat(response.rawJson()).isEqualTo("{\"synonyms\":[\"Encina\"]}");
    assertThat(response.providerSummary()).isEqualTo("openai:gpt-4.1-mini:Quercus ilex");
    verify(openAiResponsesClient).createJsonObjectResponse("gpt-4.1-mini", "prompt");
  }
}
