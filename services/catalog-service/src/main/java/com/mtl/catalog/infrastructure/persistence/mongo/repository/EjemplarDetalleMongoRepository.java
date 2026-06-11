package com.mtl.catalog.infrastructure.persistence.mongo.repository;

import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EjemplarDetalleMongoRepository extends MongoRepository<EjemplarDetalleDocument, Long> {}
