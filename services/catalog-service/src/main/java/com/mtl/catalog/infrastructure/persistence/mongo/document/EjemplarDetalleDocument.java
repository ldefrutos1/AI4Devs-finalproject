package com.mtl.catalog.infrastructure.persistence.mongo.document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Colección {@code ejemplar_detalle} — enriquecimiento del ejemplar ([mongo.md] §3.2).
 * {@code _id} numérico igual a {@code ejemplar_pg_id}.
 */
@Document(collection = "ejemplar_detalle")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EjemplarDetalleDocument {

  @Id
  @EqualsAndHashCode.Include
  private Long id;

  @Field("ejemplar_pg_id")
  private Long ejemplarPgId;

  @Field("especie_pg_id")
  private Long especiePgId;

  private Map<String, Object> medidas = new LinkedHashMap<>();

  @Field("estado_sanitario")
  private Map<String, Object> estadoSanitario = new LinkedHashMap<>();

  private List<String> etiquetas = new ArrayList<>();

  private List<ObservacionEmbeddable> observaciones = new ArrayList<>();

  /** Asigna {@code _id} y {@code ejemplar_pg_id} al mismo valor canónico PostgreSQL. */
  public void assignEjemplarPgId(long ejemplarPgId) {
    this.id = ejemplarPgId;
    this.ejemplarPgId = ejemplarPgId;
  }
}
