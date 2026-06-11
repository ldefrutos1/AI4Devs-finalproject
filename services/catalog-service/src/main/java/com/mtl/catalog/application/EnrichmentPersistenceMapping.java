package com.mtl.catalog.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Traducción de claves API (inglés) ↔ persistencia Mongo (español) para enriquecimientos. */
final class EnrichmentPersistenceMapping {

  private static final Map<String, String> MEASUREMENT_API_TO_MONGO =
      Map.of(
          "heightM", "altura_m",
          "trunkDiameterCm", "diametro_tronco_cm",
          "crownDiameterM", "diametro_copa_m",
          "trunkPerimeterCm", "perimetro_tronco_cm");

  private static final Map<String, String> MEASUREMENT_MONGO_TO_API =
      invert(MEASUREMENT_API_TO_MONGO);

  private EnrichmentPersistenceMapping() {}

  static Map<String, Object> measurementsToMongo(Map<String, Object> apiMeasurements) {
    if (apiMeasurements == null || apiMeasurements.isEmpty()) {
      return new LinkedHashMap<>();
    }
    Map<String, Object> mongo = new LinkedHashMap<>();
    apiMeasurements.forEach(
        (key, value) -> mongo.put(MEASUREMENT_API_TO_MONGO.getOrDefault(key, key), value));
    return mongo;
  }

  static Map<String, Object> measurementsToApi(Map<String, Object> mongoMeasurements) {
    if (mongoMeasurements == null || mongoMeasurements.isEmpty()) {
      return null;
    }
    Map<String, Object> api = new LinkedHashMap<>();
    mongoMeasurements.forEach(
        (key, value) -> api.put(MEASUREMENT_MONGO_TO_API.getOrDefault(key, key), value));
    return api;
  }

  static Map<String, Object> distributionToMongo(
      List<String> continents, List<String> countries, String description) {
    if (continents == null && countries == null && description == null) {
      return new LinkedHashMap<>();
    }
    Map<String, Object> mongo = new LinkedHashMap<>();
    if (continents != null) {
      mongo.put("continentes", continents);
    }
    if (countries != null) {
      mongo.put("paises", countries);
    }
    if (description != null) {
      mongo.put("descripcion", description);
    }
    return mongo;
  }

  static List<String> stringList(Object value) {
    if (!(value instanceof List<?> list)) {
      return null;
    }
    List<String> out = new ArrayList<>();
    for (Object item : list) {
      if (item != null) {
        out.add(Objects.toString(item));
      }
    }
    return out.isEmpty() ? null : out;
  }

  private static Map<String, String> invert(Map<String, String> source) {
    Map<String, String> inverted = new LinkedHashMap<>();
    source.forEach((k, v) -> inverted.put(v, k));
    return Map.copyOf(inverted);
  }
}
