package com.mtl.catalog.application;

import com.mtl.catalog.domain.Ejemplar;
import com.mtl.catalog.exception.CatalogForbiddenException;
import com.mtl.catalog.exception.CatalogNotFoundException;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.EjemplarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EjemplarDeleteService {

  private static final Logger log = LoggerFactory.getLogger(EjemplarDeleteService.class);

  private final EjemplarRepository ejemplarRepository;
  private final EjemplarEnrichmentDeletionPort ejemplarEnrichmentDeletionPort;
  private final CatalogAuditService catalogAuditService;
  private final AfterCommitTaskRegistrar afterCommitTaskRegistrar;

  public EjemplarDeleteService(
      EjemplarRepository ejemplarRepository,
      EjemplarEnrichmentDeletionPort ejemplarEnrichmentDeletionPort,
      CatalogAuditService catalogAuditService,
      AfterCommitTaskRegistrar afterCommitTaskRegistrar) {
    this.ejemplarRepository = ejemplarRepository;
    this.ejemplarEnrichmentDeletionPort = ejemplarEnrichmentDeletionPort;
    this.catalogAuditService = catalogAuditService;
    this.afterCommitTaskRegistrar = afterCommitTaskRegistrar;
  }

  public EjemplarDeleteAuthorization authorize(long ejemplarId, long actorUsuarioAppId, boolean admin) {
    Ejemplar ejemplar =
        ejemplarRepository
            .findById(ejemplarId)
            .orElseThrow(
                () ->
                    new CatalogNotFoundException(
                        "No se encontró un árbol con el identificador indicado."));

    if (!admin && !ejemplar.getUsuarioAppId().equals(actorUsuarioAppId)) {
      throw new CatalogForbiddenException("No tiene permiso para eliminar esta ficha de árbol.");
    }

    return new EjemplarDeleteAuthorization(ejemplarId, ejemplar.getEspecieId(), ejemplar.getProvinciaId());
  }

  /**
   * Borrado físico SQL + auditoría en transacción; hook Mongo tras commit (HU-015). Un fallo en
   * Mongo no revierte el DELETE en PostgreSQL.
   */
  @Transactional
  public void commitPhysicalDelete(EjemplarDeleteAuthorization authorization, long actorUsuarioAppId) {
    if (!ejemplarRepository.existsById(authorization.treeId())) {
      throw new CatalogNotFoundException("No se encontró un árbol con el identificador indicado.");
    }
    ejemplarRepository.deleteById(authorization.treeId());
    catalogAuditService.recordEjemplarDeleted(
        actorUsuarioAppId,
        authorization.treeId(),
        authorization.especieId(),
        authorization.provinciaId());
    long treeId = authorization.treeId();
    afterCommitTaskRegistrar.runAfterCommit(() -> deleteMongoEnrichmentSafely(treeId));
  }

  private void deleteMongoEnrichmentSafely(long ejemplarId) {
    try {
      ejemplarEnrichmentDeletionPort.deleteEnrichmentForEjemplar(ejemplarId);
    } catch (Exception ex) {
      log.error(
          "Fallo al eliminar enriquecimiento Mongo tras baja SQL (ejemplarId={}): {}",
          ejemplarId,
          ex.toString());
      log.debug("Detalle fallo borrado Mongo", ex);
    }
  }
}
