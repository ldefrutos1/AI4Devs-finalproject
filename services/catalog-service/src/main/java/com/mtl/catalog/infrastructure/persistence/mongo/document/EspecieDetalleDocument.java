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
 * Colección {@code especie_detalle} — enriquecimiento ampliado de especie ([mongo.md] §3.1).
 * {@code _id} numérico igual a {@code especie_pg_id}.
 */
@Document(collection = "especie_detalle")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspecieDetalleDocument {

  @Id
  @EqualsAndHashCode.Include
  private Long id;

  @Field("especie_pg_id")
  private Long especiePgId;

  @Field("nombre_cientifico")
  private String nombreCientifico;

  @Field("nombre_comun")
  private String nombreComun;

  private List<String> sinonimos = new ArrayList<>();

  /** Rango geográfico (continentes, países, descripción narrativa). */
  private Map<String, Object> distribucion = new LinkedHashMap<>();

  /** Hábitat, altitud, clima, etc. */
  @Field("datos_ecologicos")
  private Map<String, Object> datosEcologicos = new LinkedHashMap<>();

  private List<ReferenciaBibliograficaEmbeddable> referencias = new ArrayList<>();

  /** Asigna {@code _id} y {@code especie_pg_id} al mismo valor canónico PostgreSQL. */
  public void assignEspeciePgId(long especiePgId) {
    this.id = especiePgId;
    this.especiePgId = especiePgId;
  }
}
