package com.mtl.catalog.infrastructure.persistence.mongo.config;

import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

/**
 * Índices acordados en [mongo.md] §4. Idempotente: {@code ensureIndex} no recrea si ya existen.
 */
@Component
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class CatalogMongoIndexInitializer {

  private static final Logger log = LoggerFactory.getLogger(CatalogMongoIndexInitializer.class);

  private final MongoTemplate mongoTemplate;

  public CatalogMongoIndexInitializer(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @PostConstruct
  void ensureCatalogMongoIndexes() {
    log.info("Verificando índices Mongo de catálogo (especie_detalle, ejemplar_detalle)…");
    ensureEspecieDetalleIndexes();
    ensureEjemplarDetalleIndexes();
  }

  private void ensureEspecieDetalleIndexes() {
    var indexOps = mongoTemplate.indexOps(EspecieDetalleDocument.class);
    indexOps.ensureIndex(
        new Index()
            .on("especie_pg_id", Sort.Direction.ASC)
            .unique()
            .named("uidx_especie_pg_id"));
    indexOps.ensureIndex(
        new TextIndexDefinition.TextIndexDefinitionBuilder()
            .onField("nombre_cientifico")
            .onField("nombre_comun")
            .named("idx_text_nombres_especie")
            .withDefaultLanguage("spanish")
            .build());
  }

  private void ensureEjemplarDetalleIndexes() {
    var indexOps = mongoTemplate.indexOps(EjemplarDetalleDocument.class);
    indexOps.ensureIndex(
        new Index()
            .on("ejemplar_pg_id", Sort.Direction.ASC)
            .unique()
            .named("uidx_ejemplar_pg_id"));
    indexOps.ensureIndex(
        new Index().on("especie_pg_id", Sort.Direction.ASC).named("idx_especie_pg_id"));
    indexOps.ensureIndex(new Index().on("etiquetas", Sort.Direction.ASC).named("idx_etiquetas"));
  }
}
