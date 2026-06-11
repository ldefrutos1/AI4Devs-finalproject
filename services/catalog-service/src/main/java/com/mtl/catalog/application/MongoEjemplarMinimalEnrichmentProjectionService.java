package com.mtl.catalog.application;

import com.mtl.catalog.domain.Especie;
import com.mtl.catalog.exception.CatalogNotFoundException;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.EspecieRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class MongoEjemplarMinimalEnrichmentProjectionService
    implements EjemplarMinimalEnrichmentProjectionPort {

  static final String ENRICHMENT_WARNING_MESSAGE =
      "La ficha se guardó correctamente, pero el enriquecimiento no se ha podido sincronizar. "
          + "Puede reintentarlo editando la ficha o el bloque de enriquecimiento.";

  private static final Logger log =
      LoggerFactory.getLogger(MongoEjemplarMinimalEnrichmentProjectionService.class);

  private final EspecieRepository especieRepository;
  private final EspecieDetalleMongoRepository especieDetalleMongoRepository;
  private final EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository;

  public MongoEjemplarMinimalEnrichmentProjectionService(
      EspecieRepository especieRepository,
      EspecieDetalleMongoRepository especieDetalleMongoRepository,
      EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository) {
    this.especieRepository = especieRepository;
    this.especieDetalleMongoRepository = especieDetalleMongoRepository;
    this.ejemplarDetalleMongoRepository = ejemplarDetalleMongoRepository;
  }

  @Override
  public Optional<String> projectAfterEjemplarSqlPersisted(long ejemplarId, long speciesId) {
    try {
      Especie especie = requireEspecie(speciesId);
      upsertEspecieDetalle(speciesId, especie.getNombreCientifico(), especie.getNombreComun());
      upsertEjemplarDetalle(ejemplarId, speciesId);
      return Optional.empty();
    } catch (Exception ex) {
      log.warn(
          "Proyección Mongo mínima fallida tras commit SQL (ejemplarId={}, speciesId={}): {}",
          ejemplarId,
          speciesId,
          ex.toString());
      log.debug("Detalle proyección Mongo mínima", ex);
      return Optional.of(ENRICHMENT_WARNING_MESSAGE);
    }
  }

  private Especie requireEspecie(long speciesId) {
    return especieRepository
        .findById(speciesId)
        .orElseThrow(
            () ->
                new CatalogNotFoundException(
                    "No se encontró una especie con el identificador indicado."));
  }

  private void upsertEspecieDetalle(long speciesId, String scientificName, String commonName) {
    EspecieDetalleDocument document =
        especieDetalleMongoRepository.findById(speciesId).orElseGet(EspecieDetalleDocument::new);
    document.assignEspeciePgId(speciesId);
    document.setNombreCientifico(scientificName);
    document.setNombreComun(commonName);
    especieDetalleMongoRepository.save(document);
  }

  private void upsertEjemplarDetalle(long ejemplarId, long speciesId) {
    EjemplarDetalleDocument document =
        ejemplarDetalleMongoRepository.findById(ejemplarId).orElseGet(EjemplarDetalleDocument::new);
    document.assignEjemplarPgId(ejemplarId);
    document.setEspeciePgId(speciesId);
    ejemplarDetalleMongoRepository.save(document);
  }
}
