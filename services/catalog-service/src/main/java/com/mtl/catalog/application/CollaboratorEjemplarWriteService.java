package com.mtl.catalog.application;

import com.mtl.catalog.dto.CollaboratorEjemplarDetailDto;
import com.mtl.catalog.dto.CreateEjemplarRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Orquesta altas y ediciones de fichas de colaborador, incluyendo la proyección mínima Mongo tras el
 * commit SQL (HU-015). Sin {@code @Transactional}: delega la transacción a los servicios internos.
 */
@Service
public class CollaboratorEjemplarWriteService {

  private final EjemplarRegistrationService ejemplarRegistrationService;
  private final EjemplarModificationService ejemplarModificationService;
  private final EjemplarMinimalEnrichmentProjectionPort ejemplarMinimalEnrichmentProjectionPort;

  public CollaboratorEjemplarWriteService(
      EjemplarRegistrationService ejemplarRegistrationService,
      EjemplarModificationService ejemplarModificationService,
      EjemplarMinimalEnrichmentProjectionPort ejemplarMinimalEnrichmentProjectionPort) {
    this.ejemplarRegistrationService = ejemplarRegistrationService;
    this.ejemplarModificationService = ejemplarModificationService;
    this.ejemplarMinimalEnrichmentProjectionPort = ejemplarMinimalEnrichmentProjectionPort;
  }

  public RegisteredEjemplarOutcome registerEjemplar(CreateEjemplarRequest request, Jwt jwt) {
    CreatedEjemplarResult created = ejemplarRegistrationService.register(request, jwt);
    String enrichmentWarning =
        ejemplarMinimalEnrichmentProjectionPort
            .projectAfterEjemplarSqlPersisted(created.treeId(), request.speciesId())
            .orElse(null);
    return new RegisteredEjemplarOutcome(created.treeId(), enrichmentWarning);
  }

  public CollaboratorEjemplarDetailDto updateEjemplar(
      long treeId, CreateEjemplarRequest request, Jwt jwt) {
    CollaboratorEjemplarDetailDto updated =
        ejemplarModificationService.updateEjemplar(treeId, request, jwt);
    return applyEnrichmentWarning(updated);
  }

  private CollaboratorEjemplarDetailDto applyEnrichmentWarning(
      CollaboratorEjemplarDetailDto detail) {
    return ejemplarMinimalEnrichmentProjectionPort
        .projectAfterEjemplarSqlPersisted(detail.treeId(), detail.speciesId())
        .map(detail::withEnrichmentWarning)
        .orElse(detail);
  }
}
