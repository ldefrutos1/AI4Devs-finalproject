package com.mtl.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.catalog.dto.CollaboratorEjemplarDetailDto;
import com.mtl.catalog.dto.CreateEjemplarRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class CollaboratorEjemplarWriteServiceTest {

  @Mock private EjemplarRegistrationService ejemplarRegistrationService;
  @Mock private EjemplarModificationService ejemplarModificationService;
  @Mock private EjemplarMinimalEnrichmentProjectionPort ejemplarMinimalEnrichmentProjectionPort;

  @InjectMocks private CollaboratorEjemplarWriteService service;

  @Test
  void registerEjemplar_proyectaMongoTrasCommitSql() {
    CreateEjemplarRequest request =
        new CreateEjemplarRequest(10L, 28L, BigDecimal.ONE, BigDecimal.TWO, null, null, null, null, null);
    when(ejemplarRegistrationService.register(any(), any()))
        .thenReturn(new CreatedEjemplarResult(42L, 5L, OffsetDateTime.parse("2024-01-02T12:00:00Z")));
    when(ejemplarMinimalEnrichmentProjectionPort.projectAfterEjemplarSqlPersisted(42L, 10L))
        .thenReturn(Optional.of("Aviso de enriquecimiento incompleto."));

    RegisteredEjemplarOutcome outcome = service.registerEjemplar(request, testJwt());

    assertThat(outcome.treeId()).isEqualTo(42L);
    assertThat(outcome.enrichmentWarning()).isEqualTo("Aviso de enriquecimiento incompleto.");
    verify(ejemplarRegistrationService).register(eq(request), any());
    verify(ejemplarMinimalEnrichmentProjectionPort).projectAfterEjemplarSqlPersisted(42L, 10L);
  }

  @Test
  void updateEjemplar_aplicaEnrichmentWarningEnDetalle() {
    CreateEjemplarRequest request =
        new CreateEjemplarRequest(11L, 29L, BigDecimal.ONE, BigDecimal.TWO, null, null, null, null, null);
    CollaboratorEjemplarDetailDto detail = detailDto(42L, 11L, null);
    when(ejemplarModificationService.updateEjemplar(eq(42L), eq(request), any()))
        .thenReturn(detail);
    when(ejemplarMinimalEnrichmentProjectionPort.projectAfterEjemplarSqlPersisted(42L, 11L))
        .thenReturn(Optional.of("Aviso de sincronización."));

    CollaboratorEjemplarDetailDto result = service.updateEjemplar(42L, request, testJwt());

    assertThat(result.enrichmentWarning()).isEqualTo("Aviso de sincronización.");
  }

  private static Jwt testJwt() {
    return Jwt.withTokenValue("test")
        .header("alg", "none")
        .subject("colab")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .claim("realm_access", Map.of("roles", List.of("COLABORADOR")))
        .build();
  }

  private static CollaboratorEjemplarDetailDto detailDto(
      long treeId, long speciesId, String enrichmentWarning) {
    return new CollaboratorEjemplarDetailDto(
        treeId,
        speciesId,
        29L,
        BigDecimal.ONE,
        BigDecimal.TWO,
        "Madrid",
        "Nota",
        600,
        "PUBLICADO",
        "PUBLICO",
        7L,
        "Encina (Quercus ilex)",
        "Madrid (29)",
        OffsetDateTime.parse("2024-01-01T10:00:00Z"),
        OffsetDateTime.parse("2024-02-01T12:00:00Z"),
        enrichmentWarning);
  }
}
