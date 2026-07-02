package com.mtl.catalog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtl.catalog.application.MongoEjemplarEnrichmentDeletionPort;
import com.mtl.catalog.infrastructure.persistence.mongo.config.CatalogMongoConfig;
import com.mtl.catalog.infrastructure.persistence.mongo.config.CatalogMongoIndexInitializer;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.document.ObservacionEmbeddable;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Persistencia Mongo de catálogo (HU-015 TASK-02). Requiere Docker. */
@Tag("integration")
@Testcontainers
@DataMongoTest
@Import({CatalogMongoConfig.class, CatalogMongoIndexInitializer.class, MongoEjemplarEnrichmentDeletionPort.class})
@ActiveProfiles("test-mongo")
@EnabledIf("com.mtl.catalog.integration.support.DockerConditions#dockerDisponible")
class CatalogMongoPersistenceIT {

  @Container
  static final MongoDBContainer MONGO_DB = new MongoDBContainer("mongo:7.0");

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", () -> MONGO_DB.getConnectionString() + "/mtl_catalog");
  }

  @Autowired private EspecieDetalleMongoRepository especieDetalleMongoRepository;
  @Autowired private EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository;
  @Autowired private MongoTemplate mongoTemplate;
  @Autowired private MongoEjemplarEnrichmentDeletionPort mongoEjemplarEnrichmentDeletionPort;

  @Test
  void guardarYLeerDocumentosConObservacionesEmbebidas() {
    EspecieDetalleDocument especie = new EspecieDetalleDocument();
    especie.assignEspeciePgId(12L);
    especie.setNombreCientifico("Quercus robur");
    especie.setNombreComun("Roble pedunculado");
    especieDetalleMongoRepository.save(especie);

    EjemplarDetalleDocument ejemplar = new EjemplarDetalleDocument();
    ejemplar.assignEjemplarPgId(847L);
    ejemplar.setEspeciePgId(12L);
    ejemplar.setMedidas(Map.of("altura_m", 24.5, "diametro_tronco_cm", 187));
    ObservacionEmbeddable observacion = new ObservacionEmbeddable();
    observacion.setFecha(LocalDate.parse("2024-04-10"));
    observacion.setTexto("Brotación intensa.");
    observacion.setAutor("Carlos Mendoza");
    ejemplar.getObservaciones().add(observacion);
    ejemplarDetalleMongoRepository.save(ejemplar);

    assertThat(especieDetalleMongoRepository.findById(12L))
        .isPresent()
        .get()
        .extracting(EspecieDetalleDocument::getNombreCientifico)
        .isEqualTo("Quercus robur");

    assertThat(ejemplarDetalleMongoRepository.findById(847L))
        .isPresent()
        .get()
        .satisfies(
            doc -> {
              assertThat(doc.getEspeciePgId()).isEqualTo(12L);
              assertThat(doc.getObservaciones()).hasSize(1);
              assertThat(doc.getObservaciones().getFirst().getTexto()).isEqualTo("Brotación intensa.");
            });
  }

  @Test
  void borradoEjemplar_eliminaEjemplarDetalleYConservaEspecieDetalle() {
    EspecieDetalleDocument especie = new EspecieDetalleDocument();
    especie.assignEspeciePgId(12L);
    especie.setNombreCientifico("Quercus robur");
    especieDetalleMongoRepository.save(especie);

    EjemplarDetalleDocument ejemplar = new EjemplarDetalleDocument();
    ejemplar.assignEjemplarPgId(847L);
    ejemplar.setEspeciePgId(12L);
    ejemplarDetalleMongoRepository.save(ejemplar);

    mongoEjemplarEnrichmentDeletionPort.deleteEnrichmentForEjemplar(847L);

    assertThat(ejemplarDetalleMongoRepository.findById(847L)).isEmpty();
    assertThat(especieDetalleMongoRepository.findById(12L)).isPresent();
  }

  @Test
  void indicesAcordadosExistenTrasArranque() {
    assertThat(indexNames("especie_detalle"))
        .contains("uidx_especie_pg_id", "idx_text_nombres_especie");
    assertThat(indexNames("ejemplar_detalle"))
        .contains("uidx_ejemplar_pg_id", "idx_especie_pg_id", "idx_etiquetas");
  }

  private Iterable<String> indexNames(String collection) {
    return mongoTemplate.indexOps(collection).getIndexInfo().stream()
        .map(IndexInfo::getName)
        .toList();
  }
}
