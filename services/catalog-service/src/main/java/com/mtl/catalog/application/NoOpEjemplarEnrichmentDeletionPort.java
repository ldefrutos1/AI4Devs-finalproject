package com.mtl.catalog.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Stub cuando Mongo está desactivado. */
@Component
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEjemplarEnrichmentDeletionPort implements EjemplarEnrichmentDeletionPort {

  private static final Logger log = LoggerFactory.getLogger(NoOpEjemplarEnrichmentDeletionPort.class);

  @Override
  public void deleteEnrichmentForEjemplar(long ejemplarId) {
    log.debug("Mongo no configurado: omitiendo borrado de enriquecimiento (ejemplarId={})", ejemplarId);
  }
}
