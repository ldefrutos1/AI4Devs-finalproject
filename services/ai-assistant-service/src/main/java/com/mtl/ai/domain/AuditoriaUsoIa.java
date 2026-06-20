package com.mtl.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "auditoria_uso_ia")
@Getter
@Setter
public class AuditoriaUsoIa {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "auditoria_ia_id")
  private Long auditoriaIaId;

  @Column(name = "subject_oidc", nullable = false, length = 255)
  private String subjectOidc;

  @Column(name = "tipo_uso_ia", nullable = false, length = 100)
  private String tipoUsoIa;

  @Column(name = "ejemplar_id")
  private Long ejemplarId;

  @Column(name = "prompt", nullable = false, columnDefinition = "text")
  private String prompt;

  @Column(name = "resultado_resumen", nullable = false, columnDefinition = "text")
  private String resultadoResumen;

  @Column(name = "consultado_en", nullable = false)
  private OffsetDateTime consultadoEn;
}
