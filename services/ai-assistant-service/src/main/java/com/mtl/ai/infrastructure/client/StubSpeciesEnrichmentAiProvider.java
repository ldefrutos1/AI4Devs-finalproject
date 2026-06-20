package com.mtl.ai.infrastructure.client;

import com.mtl.ai.application.SpeciesEnrichmentAiProvider;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionResponse;
import com.mtl.ai.dto.BibliographicReferenceDto;
import com.mtl.ai.dto.SpeciesDistributionDto;
import java.time.Year;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** Implementación local sin dependencia externa, útil para desarrollo y tests. */
@Component
@ConditionalOnProperty(
    prefix = "mtl.ai.provider",
    name = "mode",
    havingValue = "stub",
    matchIfMissing = true)
public class StubSpeciesEnrichmentAiProvider implements SpeciesEnrichmentAiProvider {

  private final ObjectMapper objectMapper;

  public StubSpeciesEnrichmentAiProvider(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public ProviderResponse requestSuggestion(
      String prompt, String scientificName, String commonName) {
    AiSpeciesEnrichmentSuggestionResponse response =
        new AiSpeciesEnrichmentSuggestionResponse(
            List.of(commonName.trim(), scientificName.trim()),
            new SpeciesDistributionDto(
                List.of("Europa"), List.of("España"), "Respuesta stub orientativa para desarrollo local."),
            Map.of(
                "habitat", List.of("bosque mediterráneo", "laderas soleadas"),
                "altitudMinM", 0,
                "altitudMaxM", 1400,
                "clima", List.of("mediterráneo"),
                "growthRate", "moderate",
                "leafType", "evergreen"),
            List.of(
                new BibliographicReferenceDto(
                    "Ficha orientativa de " + scientificName.trim(),
                    List.of("MyTreeLibrary Stub"),
                    "Proveedor IA simulado",
                    Year.now().getValue(),
                    "https://example.invalid/ai/species")));
    try {
      return new ProviderResponse(
          objectMapper.writeValueAsString(response), "stub:%s".formatted(scientificName.trim()));
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo serializar la respuesta stub de IA.", ex);
    }
  }
}
