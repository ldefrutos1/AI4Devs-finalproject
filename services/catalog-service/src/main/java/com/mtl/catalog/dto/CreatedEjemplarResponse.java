package com.mtl.catalog.dto;

/** Cuerpo de respuesta HTTP 201 tras crear un árbol. */
public record CreatedEjemplarResponse(long treeId, String enrichmentWarning) {

  public CreatedEjemplarResponse(long treeId) {
    this(treeId, null);
  }
}
