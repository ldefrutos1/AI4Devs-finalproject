package com.mtl.catalog.infrastructure.persistence.mongo.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;

@ExtendWith(MockitoExtension.class)
class CatalogMongoIndexInitializerTest {

  @Mock private MongoTemplate mongoTemplate;
  @Mock private IndexOperations especieIndexOps;
  @Mock private IndexOperations ejemplarIndexOps;

  private CatalogMongoIndexInitializer initializer;

  @BeforeEach
  void setUp() {
    when(mongoTemplate.indexOps(EspecieDetalleDocument.class)).thenReturn(especieIndexOps);
    when(mongoTemplate.indexOps(EjemplarDetalleDocument.class)).thenReturn(ejemplarIndexOps);
    initializer = new CatalogMongoIndexInitializer(mongoTemplate);
  }

  @Test
  void ensureCatalogMongoIndexes_registraIndicesDeAmbasColecciones() {
    initializer.ensureCatalogMongoIndexes();

    verify(especieIndexOps, atLeastOnce()).ensureIndex(any(IndexDefinition.class));
    verify(ejemplarIndexOps, atLeastOnce()).ensureIndex(any(IndexDefinition.class));
  }
}
