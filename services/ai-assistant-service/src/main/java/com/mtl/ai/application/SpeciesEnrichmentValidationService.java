package com.mtl.ai.application;

import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionResponse;
import com.mtl.ai.dto.BibliographicReferenceDto;
import com.mtl.ai.dto.SpeciesDistributionDto;
import com.mtl.ai.exception.AiAssistantException;
import java.net.URI;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SpeciesEnrichmentValidationService {

  private static final Set<String> ROOT_KEYS =
      Set.of("synonyms", "distribution", "ecologicalData", "references");
  private static final Set<String> DISTRIBUTION_KEYS =
      Set.of("continents", "countries", "description");
  private static final Set<String> FLOWERING_KEYS = Set.of("startMonth", "endMonth");
  private static final Set<String> REFERENCE_KEYS =
      Set.of("title", "authors", "source", "year", "url");
  private static final Map<String, String> GROWTH_RATE_ALIASES =
      Map.of(
          "lento", "slow",
          "moderado", "moderate",
          "rápido", "fast",
          "slow", "slow",
          "moderate", "moderate",
          "fast", "fast");
  private static final Map<String, String> LEAF_TYPE_ALIASES =
      Map.of(
          "caduca", "deciduous",
          "perennifolia", "evergreen",
          "marcescente", "marcescent",
          "deciduous", "deciduous",
          "evergreen", "evergreen",
          "marcescent", "marcescent");

  private final ObjectMapper objectMapper;

  public SpeciesEnrichmentValidationService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public AiSpeciesEnrichmentSuggestionResponse validateAndMap(String rawJson) {
    JsonNode root = parseRoot(rawJson);
    ensureObject(root, "La respuesta IA debe ser un objeto JSON.");
    rejectUnknownKeys(root, ROOT_KEYS, "La respuesta IA contiene claves raíz no permitidas.");

    List<String> synonyms = readStringArray(root.get("synonyms"), "synonyms");
    SpeciesDistributionDto distribution = readDistribution(root.get("distribution"));
    Map<String, Object> ecologicalData = readEcologicalData(root.get("ecologicalData"));
    List<BibliographicReferenceDto> references = readReferences(root.get("references"));

    return new AiSpeciesEnrichmentSuggestionResponse(
        emptyToNull(synonyms), distribution, emptyToNull(ecologicalData), emptyToNull(references));
  }

  private JsonNode parseRoot(String rawJson) {
    if (rawJson == null || rawJson.isBlank()) {
      throw new AiAssistantException(
          HttpStatus.NOT_FOUND,
          AiResponseErrorMessages.TITLE_NOT_FOUND,
          AiResponseErrorMessages.DETAIL_NOT_FOUND);
    }
    try {
      return objectMapper.readTree(rawJson);
    } catch (Exception ex) {
      throw new AiAssistantException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          AiResponseErrorMessages.TITLE_INVALID,
          AiResponseErrorMessages.DETAIL_INVALID_JSON,
          ex);
    }
  }

  private SpeciesDistributionDto readDistribution(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    ensureObject(node, "distribution debe ser un objeto.");
    rejectUnknownKeys(node, DISTRIBUTION_KEYS, "distribution contiene claves no permitidas.");
    List<String> continents = readStringArray(node.get("continents"), "distribution.continents");
    List<String> countries = readStringArray(node.get("countries"), "distribution.countries");
    String description = readOptionalText(node.get("description"), "distribution.description");
    if (isEmpty(continents) && isEmpty(countries) && isBlank(description)) {
      return null;
    }
    return new SpeciesDistributionDto(emptyToNull(continents), emptyToNull(countries), blankToNull(description));
  }

  private Map<String, Object> readEcologicalData(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    ensureObject(node, "ecologicalData debe ser un objeto.");

    Integer altitudMinM = readOptionalInteger(node.get("altitudMinM"), "ecologicalData.altitudMinM");
    Integer altitudMaxM = readOptionalInteger(node.get("altitudMaxM"), "ecologicalData.altitudMaxM");
    if (altitudMinM != null && altitudMinM < 0) {
      fail422("ecologicalData.altitudMinM debe ser mayor o igual que 0.");
    }
    if (altitudMaxM != null && altitudMaxM < 0) {
      fail422("ecologicalData.altitudMaxM debe ser mayor o igual que 0.");
    }
    if (altitudMinM != null && altitudMaxM != null && altitudMinM >= altitudMaxM) {
      fail422("ecologicalData.altitudMinM debe ser menor que ecologicalData.altitudMaxM.");
    }

    String growthRate = readOptionalText(node.get("growthRate"), "ecologicalData.growthRate");
    if (growthRate != null) {
      growthRate = normalizeGrowthRate(growthRate);
      if (growthRate == null) {
        fail422("ecologicalData.growthRate debe ser uno de: slow, moderate, fast.");
      }
    }

    String leafType = readOptionalText(node.get("leafType"), "ecologicalData.leafType");
    if (leafType != null) {
      leafType = normalizeLeafType(leafType);
      if (leafType == null) {
        fail422("ecologicalData.leafType debe ser uno de: deciduous, evergreen, marcescent.");
      }
    }

    readStringArray(node.get("habitat"), "ecologicalData.habitat");
    readStringArray(node.get("clima"), "ecologicalData.clima");
    readStringArray(node.get("suelo"), "ecologicalData.suelo");
    readStringArray(node.get("associatedFauna"), "ecologicalData.associatedFauna");

    Integer longevityMaxYears =
        readOptionalInteger(node.get("longevityMaxYears"), "ecologicalData.longevityMaxYears");
    if (longevityMaxYears != null && longevityMaxYears < 0) {
      fail422("ecologicalData.longevityMaxYears debe ser mayor o igual que 0.");
    }

    JsonNode floweringPeriod = node.get("floweringPeriod");
    if (floweringPeriod != null && !floweringPeriod.isNull()) {
      ensureObject(floweringPeriod, "ecologicalData.floweringPeriod debe ser un objeto.");
      rejectUnknownKeys(
          floweringPeriod,
          FLOWERING_KEYS,
          "ecologicalData.floweringPeriod contiene claves no permitidas.");
      Integer startMonth =
          readOptionalInteger(floweringPeriod.get("startMonth"), "ecologicalData.floweringPeriod.startMonth");
      Integer endMonth =
          readOptionalInteger(floweringPeriod.get("endMonth"), "ecologicalData.floweringPeriod.endMonth");
      validateMonth(startMonth, "ecologicalData.floweringPeriod.startMonth");
      validateMonth(endMonth, "ecologicalData.floweringPeriod.endMonth");
    }

    Map<String, Object> ecologicalData =
        objectMapper.convertValue(node, new TypeReference<LinkedHashMap<String, Object>>() {});
    if (growthRate != null) {
      ecologicalData.put("growthRate", growthRate);
    }
    if (leafType != null) {
      ecologicalData.put("leafType", leafType);
    }
    return ecologicalData;
  }

  private List<BibliographicReferenceDto> readReferences(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    if (!node.isArray()) {
      fail422("references debe ser un array.");
    }
    for (int i = 0; i < node.size(); i++) {
      JsonNode item = node.get(i);
      ensureObject(item, "references[%s] debe ser un objeto.".formatted(i));
      rejectUnknownKeys(
          item, REFERENCE_KEYS, "references[%s] contiene claves no permitidas.".formatted(i));
    }
    List<BibliographicReferenceDto> references =
        objectMapper.convertValue(node, new TypeReference<List<BibliographicReferenceDto>>() {});
    int currentYear = Year.now().getValue();
    for (int i = 0; i < references.size(); i++) {
      BibliographicReferenceDto ref = references.get(i);
      if (ref == null) {
        fail422("references[%s] no puede ser null.".formatted(i));
      }
      if (ref.year() != null) {
        if (ref.year() < 1000 || ref.year() > currentYear) {
          fail422("references[%s].year debe tener 4 dígitos y no ser futuro.".formatted(i));
        }
      }
      if (ref.url() != null) {
        validateHttpUrl(ref.url(), "references[%s].url".formatted(i));
      }
      if (ref.authors() != null && ref.authors().stream().anyMatch(author -> author == null || author.isBlank())) {
        fail422("references[%s].authors debe contener solo textos no vacíos.".formatted(i));
      }
    }
    return references;
  }

  private void validateHttpUrl(String url, String field) {
    try {
      URI uri = URI.create(url.trim());
      String scheme = uri.getScheme();
      if (scheme == null
          || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
        fail422(field + " debe ser una URL http o https válida.");
      }
      if (uri.getHost() == null || uri.getHost().isBlank()) {
        fail422(field + " debe ser una URL http o https válida.");
      }
    } catch (IllegalArgumentException ex) {
      fail422(field + " debe tener formato de URL válido.");
    }
  }

  private void validateMonth(Integer month, String field) {
    if (month != null && (month < 1 || month > 12)) {
      fail422(field + " debe estar entre 1 y 12.");
    }
  }

  private List<String> readStringArray(JsonNode node, String field) {
    if (node == null || node.isNull()) {
      return null;
    }
    if (!node.isArray()) {
      fail422(field + " debe ser un array.");
    }
    Set<String> values = new LinkedHashSet<>();
    for (JsonNode item : node) {
      if (!item.isTextual()) {
        fail422(field + " debe contener solo textos.");
      }
      String text = item.asText().trim();
      if (!text.isEmpty()) {
        values.add(text);
      }
    }
    return values.isEmpty() ? null : List.copyOf(values);
  }

  private Integer readOptionalInteger(JsonNode node, String field) {
    if (node == null || node.isNull()) {
      return null;
    }
    if (!node.canConvertToInt()) {
      fail422(field + " debe ser un entero.");
    }
    return node.intValue();
  }

  private String readOptionalText(JsonNode node, String field) {
    if (node == null || node.isNull()) {
      return null;
    }
    if (!node.isTextual()) {
      fail422(field + " debe ser un texto.");
    }
    return blankToNull(node.asText().trim());
  }

  private void ensureObject(JsonNode node, String detail) {
    if (node == null || !node.isObject()) {
      fail422(detail);
    }
  }

  private void rejectUnknownKeys(JsonNode node, Set<String> allowedKeys, String detail) {
    Map<String, Object> asMap =
        objectMapper.convertValue(node, new TypeReference<LinkedHashMap<String, Object>>() {});
    for (String fieldName : asMap.keySet()) {
      if (!allowedKeys.contains(fieldName)) {
        fail422(detail + " Clave desconocida: " + fieldName);
      }
    }
  }

  private void fail422(String detail) {
    throw new AiAssistantException(
        HttpStatus.UNPROCESSABLE_ENTITY,
        AiResponseErrorMessages.TITLE_INVALID,
        detail + AiResponseErrorMessages.VALIDATION_SUFFIX);
  }

  private static String normalizeGrowthRate(String value) {
    return GROWTH_RATE_ALIASES.get(value);
  }

  private static String normalizeLeafType(String value) {
    return LEAF_TYPE_ALIASES.get(value);
  }

  private static <T> List<T> emptyToNull(List<T> values) {
    return values == null || values.isEmpty() ? null : values;
  }

  private static <K, V> Map<K, V> emptyToNull(Map<K, V> values) {
    return values == null || values.isEmpty() ? null : values;
  }

  private static boolean isEmpty(List<?> values) {
    return values == null || values.isEmpty();
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String blankToNull(String value) {
    return isBlank(value) ? null : value;
  }
}
