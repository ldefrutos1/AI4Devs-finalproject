package com.mtl.catalog.application;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Sin Mongo configurado: omite proyección mínima. */
@Component
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEjemplarMinimalEnrichmentProjectionPort
    implements EjemplarMinimalEnrichmentProjectionPort {

  private static final Logger log =
      LoggerFactory.getLogger(NoOpEjemplarMinimalEnrichmentProjectionPort.class);

  @Override
  public Optional<String> projectAfterEjemplarSqlPersisted(long ejemplarId, long speciesId) {
    log.debug(
        "Mongo no configurado: omitiendo proyección mínima (ejemplarId={}, speciesId={})",
        ejemplarId,
        speciesId);
    return Optional.empty();
  }
}
