package com.mtl.catalog.application;

import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Borrado físico de {@code especie_detalle} tras baja SQL de especie (HU-015). */
@Component
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class MongoEspecieDetalleEnrichmentDeletionPort implements EspecieDetalleEnrichmentDeletionPort {

  private static final Logger log =
      LoggerFactory.getLogger(MongoEspecieDetalleEnrichmentDeletionPort.class);

  private final EspecieDetalleMongoRepository especieDetalleMongoRepository;

  public MongoEspecieDetalleEnrichmentDeletionPort(
      EspecieDetalleMongoRepository especieDetalleMongoRepository) {
    this.especieDetalleMongoRepository = especieDetalleMongoRepository;
  }

  @Override
  public void deleteEnrichmentForSpecies(long speciesId) {
    if (especieDetalleMongoRepository.existsById(speciesId)) {
      especieDetalleMongoRepository.deleteById(speciesId);
      log.info("Enriquecimiento Mongo eliminado para speciesId={}", speciesId);
      return;
    }
    log.debug("Sin documento especie_detalle que eliminar (speciesId={})", speciesId);
  }
}
