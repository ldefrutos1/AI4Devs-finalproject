package com.mtl.catalog.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mtl.catalog.exception.CatalogValidationException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TreeEnrichmentValidatorTest {

  @Test
  void validateMeasurements_aceptaNumerosFinitos() {
    assertThatCode(
            () ->
                TreeEnrichmentValidator.validateMeasurements(
                    Map.of("heightM", 24.5, "trunkDiameterCm", 187)))
        .doesNotThrowAnyException();
  }

  @Test
  void validateMeasurements_rechazaNaN() {
    assertThatThrownBy(
            () ->
                TreeEnrichmentValidator.validateMeasurements(
                    Map.of("heightM", Double.NaN)))
        .isInstanceOf(CatalogValidationException.class);
  }

  @Test
  void validateMeasurements_rechazaNoNumerico() {
    assertThatThrownBy(
            () -> TreeEnrichmentValidator.validateMeasurements(Map.of("heightM", "alto")))
        .isInstanceOf(CatalogValidationException.class);
  }
}
