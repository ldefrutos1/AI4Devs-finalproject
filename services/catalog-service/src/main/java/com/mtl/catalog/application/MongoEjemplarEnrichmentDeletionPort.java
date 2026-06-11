package com.mtl.catalog.application;

import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Borrado físico de {@code ejemplar_detalle} tras baja SQL (TASK-HU-015-01). */
@Component
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class MongoEjemplarEnrichmentDeletionPort implements EjemplarEnrichmentDeletionPort {

  private static final Logger log =
      LoggerFactory.getLogger(MongoEjemplarEnrichmentDeletionPort.class);

  private final EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository;

  public MongoEjemplarEnrichmentDeletionPort(
      EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository) {
    this.ejemplarDetalleMongoRepository = ejemplarDetalleMongoRepository;
  }

  @Override
  public void deleteEnrichmentForEjemplar(long ejemplarId) {
    if (ejemplarDetalleMongoRepository.existsById(ejemplarId)) {
      ejemplarDetalleMongoRepository.deleteById(ejemplarId);
      log.info("Enriquecimiento Mongo eliminado para ejemplarId={}", ejemplarId);
      return;
    }
    log.debug("Sin documento ejemplar_detalle que eliminar (ejemplarId={})", ejemplarId);
  }
}
