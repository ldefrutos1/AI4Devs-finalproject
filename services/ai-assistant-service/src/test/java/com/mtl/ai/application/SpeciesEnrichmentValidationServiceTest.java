package com.mtl.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionResponse;
import com.mtl.ai.exception.AiAssistantException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

class SpeciesEnrichmentValidationServiceTest {

  private final SpeciesEnrichmentValidationService service =
      new SpeciesEnrichmentValidationService(JsonMapper.builder().build());

  @Test
  void validateAndMap_acceptsValidContract() {
    String json =
        """
        {
          "synonyms": ["Encina", "Quercus ilex"],
          "distribution": {
            "continents": ["Europa"],
            "countries": ["España"],
            "description": "Bosques mediterráneos."
          },
          "ecologicalData": {
            "habitat": ["bosque mediterráneo"],
            "altitudMinM": 0,
            "altitudMaxM": 1200,
            "clima": ["mediterráneo"],
            "growthRate": "moderado",
            "leafType": "perennifolia",
            "floweringPeriod": {
              "startMonth": 4,
              "endMonth": 5
            }
          },
          "references": [
            {
              "title": "Flora Ibérica",
              "authors": ["Castroviejo, S."],
              "source": "CSIC",
              "year": 1993,
              "url": "https://www.floraiberica.es"
            }
          ]
        }
        """;

    AiSpeciesEnrichmentSuggestionResponse response = service.validateAndMap(json);

    assertThat(response.synonyms()).containsExactly("Encina", "Quercus ilex");
    assertThat(response.ecologicalData()).containsEntry("growthRate", "moderate");
    assertThat(response.ecologicalData()).containsEntry("leafType", "evergreen");
  }

  @Test
  void validateAndMap_rejectsUnknownRootKey() {
    String json = "{\"synonyms\":[\"Encina\"],\"unexpected\":true}";

    assertThatThrownBy(() -> service.validateAndMap(json))
        .isInstanceOf(AiAssistantException.class)
        .hasMessageContaining("claves raíz no permitidas");
  }

  @Test
  void validateAndMap_rejectsInvalidAltitudeRange() {
    String json = "{\"ecologicalData\":{\"altitudMinM\":1000,\"altitudMaxM\":500}}";

    assertThatThrownBy(() -> service.validateAndMap(json))
        .isInstanceOf(AiAssistantException.class)
        .satisfies(
            ex ->
                assertThat(((AiAssistantException) ex).getStatus())
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY))
        .hasMessageContaining("debe ser menor");
  }

  @Test
  void validateAndMap_rejectsInvalidReferenceUrl() {
    String json =
        """
        {
          "references": [
            {
              "title": "Ejemplo",
              "authors": ["Autor"],
              "source": "Fuente",
              "year": 2020,
              "url": "no-es-url"
            }
          ]
        }
        """;

    assertThatThrownBy(() -> service.validateAndMap(json))
        .isInstanceOf(AiAssistantException.class)
        .satisfies(
            ex ->
                assertThat(((AiAssistantException) ex).getStatus())
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
  }

  @Test
  void validateAndMap_rejectsFutureReferenceYear() {
    int futureYear = java.time.Year.now().getValue() + 1;
    String json =
        """
        {
          "references": [
            {
              "title": "Ejemplo",
              "authors": ["Autor"],
              "source": "Fuente",
              "year": %d,
              "url": "https://example.org"
            }
          ]
        }
        """
            .formatted(futureYear);

    assertThatThrownBy(() -> service.validateAndMap(json))
        .isInstanceOf(AiAssistantException.class)
        .hasMessageContaining("no ser futuro");
  }

  @Test
  void validateAndMap_rejectsUnknownReferenceKey() {
    String json =
        """
        {
          "references": [
            {
              "title": "Ejemplo",
              "authors": ["Autor"],
              "source": "Fuente",
              "year": 2020,
              "url": "https://example.org",
              "unexpected": true
            }
          ]
        }
        """;

    assertThatThrownBy(() -> service.validateAndMap(json))
        .isInstanceOf(AiAssistantException.class)
        .hasMessageContaining("claves no permitidas");
  }

  @Test
  void validateAndMap_rejectsMalformedJson() {
    assertThatThrownBy(() -> service.validateAndMap("{invalid"))
        .isInstanceOf(AiAssistantException.class)
        .satisfies(
            ex ->
                assertThat(((AiAssistantException) ex).getStatus())
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
  }

  @Test
  void validateAndMap_rejectsBlankRawJsonWithNotFound() {
    assertThatThrownBy(() -> service.validateAndMap("   "))
        .isInstanceOf(AiAssistantException.class)
        .satisfies(
            ex ->
                assertThat(((AiAssistantException) ex).getStatus())
                    .isEqualTo(HttpStatus.NOT_FOUND));
  }
}
