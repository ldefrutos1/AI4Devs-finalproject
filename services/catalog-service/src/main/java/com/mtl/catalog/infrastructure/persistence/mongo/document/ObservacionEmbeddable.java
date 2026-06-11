package com.mtl.catalog.infrastructure.persistence.mongo.document;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Subdocumento embebido en {@link EjemplarDetalleDocument#observaciones}. */
@Getter
@Setter
@NoArgsConstructor
public class ObservacionEmbeddable {

  private LocalDate fecha;

  private String texto;

  private String autor;

  /** Condiciones contextuales (clima, época, etc.) con esquema flexible. */
  private Map<String, Object> condiciones = new LinkedHashMap<>();
}
