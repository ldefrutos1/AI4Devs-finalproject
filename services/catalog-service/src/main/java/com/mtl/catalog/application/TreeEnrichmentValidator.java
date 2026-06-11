package com.mtl.catalog.application;

import com.mtl.catalog.exception.CatalogValidationException;
import java.util.Map;

final class TreeEnrichmentValidator {

  private TreeEnrichmentValidator() {}

  static void validateMeasurements(Map<String, Object> measurements) {
    if (measurements == null || measurements.isEmpty()) {
      return;
    }
    for (Map.Entry<String, Object> entry : measurements.entrySet()) {
      Object value = entry.getValue();
      if (value == null) {
        continue;
      }
      if (!(value instanceof Number number)) {
        throw new CatalogValidationException(
            "La medida '"
                + entry.getKey()
                + "' debe ser un número válido.");
      }
      if (!Double.isFinite(number.doubleValue())) {
        throw new CatalogValidationException(
            "La medida '"
                + entry.getKey()
                + "' debe ser un número finito.");
      }
    }
  }
}
