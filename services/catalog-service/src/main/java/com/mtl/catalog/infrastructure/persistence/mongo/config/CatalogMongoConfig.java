package com.mtl.catalog.infrastructure.persistence.mongo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
@EnableMongoRepositories(basePackages = "com.mtl.catalog.infrastructure.persistence.mongo.repository")
public class CatalogMongoConfig {}
