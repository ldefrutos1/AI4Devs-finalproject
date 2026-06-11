package com.mtl.catalog.application;

/** Resultado de alta de ejemplar con aviso opcional de proyección Mongo (HU-015). */
public record RegisteredEjemplarOutcome(long treeId, String enrichmentWarning) {}
