package com.mtl.catalog.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Stub cuando Mongo está desactivado. */
@Component
@ConditionalOnProperty(
    name = "mtl.catalog.mongo.enabled",
    havingValue = "false",
    matchIfMissing = true)
public class NoOpEspecieDetalleEnrichmentDeletionPort implements EspecieDetalleEnrichmentDeletionPort {

  private static final Logger log =
      LoggerFactory.getLogger(NoOpEspecieDetalleEnrichmentDeletionPort.class);

  @Override
  public void deleteEnrichmentForSpecies(long speciesId) {
    log.debug(
        "Mongo no configurado: omitiendo borrado de enriquecimiento de especie (speciesId={})",
        speciesId);
  }
}
