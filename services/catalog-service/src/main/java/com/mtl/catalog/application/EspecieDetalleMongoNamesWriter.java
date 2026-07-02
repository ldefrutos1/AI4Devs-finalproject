package com.mtl.catalog.application;

import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Escritura de nombres desnormalizados en {@code especie_detalle}. */
@Component
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class EspecieDetalleMongoNamesWriter {

  private final EspecieDetalleMongoRepository especieDetalleMongoRepository;

  public EspecieDetalleMongoNamesWriter(EspecieDetalleMongoRepository especieDetalleMongoRepository) {
    this.especieDetalleMongoRepository = especieDetalleMongoRepository;
  }

  /** Crea o actualiza el documento con los nombres del maestro SQL. */
  public void upsertNames(long speciesId, String scientificName, String commonName) {
    EspecieDetalleDocument document =
        especieDetalleMongoRepository
            .findById(speciesId)
            .orElseGet(EspecieDetalleDocument::new);
    document.assignEspeciePgId(speciesId);
    document.setNombreCientifico(scientificName);
    document.setNombreComun(commonName);
    especieDetalleMongoRepository.save(document);
  }

  /** Actualiza nombres solo si ya existe enriquecimiento Mongo para la especie. */
  public void updateNamesIfPresent(long speciesId, String scientificName, String commonName) {
    especieDetalleMongoRepository
        .findById(speciesId)
        .ifPresent(
            document -> {
              document.setNombreCientifico(scientificName);
              document.setNombreComun(commonName);
              especieDetalleMongoRepository.save(document);
            });
  }
}
