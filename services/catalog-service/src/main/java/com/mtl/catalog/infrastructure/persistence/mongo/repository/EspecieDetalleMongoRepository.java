package com.mtl.catalog.infrastructure.persistence.mongo.repository;

import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EspecieDetalleMongoRepository extends MongoRepository<EspecieDetalleDocument, Long> {}
