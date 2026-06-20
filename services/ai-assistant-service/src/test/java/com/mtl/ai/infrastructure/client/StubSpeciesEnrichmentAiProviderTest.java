package com.mtl.ai.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class StubSpeciesEnrichmentAiProviderTest {

  private final StubSpeciesEnrichmentAiProvider provider =
      new StubSpeciesEnrichmentAiProvider(JsonMapper.builder().build());

  @Test
  void requestSuggestion_returnsValidJsonForValidation() throws Exception {
    var response =
        provider.requestSuggestion("prompt", "Quercus ilex", "Encina");

    assertThat(response.providerSummary()).isEqualTo("stub:Quercus ilex");
    JsonNode root = JsonMapper.builder().build().readTree(response.rawJson());
    assertThat(root.get("synonyms").isArray()).isTrue();
    assertThat(root.get("ecologicalData").get("growthRate").asText()).isEqualTo("moderate");
    assertThat(root.get("ecologicalData").get("leafType").asText()).isEqualTo("evergreen");
  }
}
