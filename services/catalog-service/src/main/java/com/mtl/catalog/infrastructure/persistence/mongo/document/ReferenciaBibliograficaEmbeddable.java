package com.mtl.catalog.infrastructure.persistence.mongo.document;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

/** Subdocumento embebido en {@link EspecieDetalleDocument#referencias}. */
@Getter
@Setter
@NoArgsConstructor
public class ReferenciaBibliograficaEmbeddable {

  private String titulo;

  private List<String> autores = new ArrayList<>();

  private String fuente;

  @Field("año")
  private Integer anio;

  private String url;
}
