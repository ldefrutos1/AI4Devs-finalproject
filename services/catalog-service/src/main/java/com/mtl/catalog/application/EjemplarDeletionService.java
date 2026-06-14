package com.mtl.catalog.application;

import com.mtl.catalog.domain.UsuarioApp;
import com.mtl.catalog.exception.CatalogForbiddenException;
import com.mtl.catalog.infrastructure.client.media.MediaEjemplarPhotosClient;
import com.mtl.catalog.util.JwtRealmRoles;
import com.mtl.catalog.util.OidcUserProfileExtractor;
import com.mtl.catalog.web.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Orquestación de baja de ficha (TASK-HU-008-07): media → PostgreSQL → hook Mongo
 * ({@link MongoEjemplarEnrichmentDeletionPort} con Mongo activo; {@link NoOpEjemplarEnrichmentDeletionPort}
 * si está desactivado). La llamada a media queda fuera de la transacción JPA del catálogo.
 */
@Service
public class EjemplarDeletionService {

  private static final Logger log = LoggerFactory.getLogger(EjemplarDeletionService.class);
  private static final String FASE_CATALOG_DELETE = "CATALOG_DELETE";

  private final UsuarioAppMaterializationService usuarioAppMaterializationService;
  private final EjemplarDeleteService ejemplarDeleteService;
  private final MediaEjemplarPhotosClient mediaEjemplarPhotosClient;
  private final CatalogAuditService catalogAuditService;

  public EjemplarDeletionService(
      UsuarioAppMaterializationService usuarioAppMaterializationService,
      EjemplarDeleteService ejemplarDeleteService,
      MediaEjemplarPhotosClient mediaEjemplarPhotosClient,
      CatalogAuditService catalogAuditService) {
    this.usuarioAppMaterializationService = usuarioAppMaterializationService;
    this.ejemplarDeleteService = ejemplarDeleteService;
    this.mediaEjemplarPhotosClient = mediaEjemplarPhotosClient;
    this.catalogAuditService = catalogAuditService;
  }

  public void deleteEjemplar(long ejemplarId, Jwt jwt) {
    UsuarioApp actor =
        usuarioAppMaterializationService.materialize(OidcUserProfileExtractor.extract(jwt));
    boolean admin = JwtRealmRoles.hasRealmRole(jwt, "ADMIN");
    boolean collaborator = JwtRealmRoles.hasRealmRole(jwt, "COLABORADOR");

    if (!admin && !collaborator) {
      throw new CatalogForbiddenException(
          "Se requiere rol COLABORADOR o ADMIN para eliminar fichas de árbol.");
    }

    EjemplarDeleteAuthorization authorization =
        ejemplarDeleteService.authorize(ejemplarId, actor.getId(), admin);

    mediaEjemplarPhotosClient.deleteAllPhotosForEjemplar(ejemplarId, jwt);

    try {
      ejemplarDeleteService.commitPhysicalDelete(authorization, actor.getId());
    } catch (RuntimeException ex) {
      recordPartialFailure(actor.getId(), authorization, ex);
      throw ex;
    }
  }

  private void recordPartialFailure(
      long actorUsuarioAppId, EjemplarDeleteAuthorization authorization, RuntimeException ex) {
    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
    log.error(
        "Fallo parcial al eliminar ejemplar: media_delete=OK, faseFallida={}, ejemplarId={}, correlationId={}",
        FASE_CATALOG_DELETE,
        authorization.treeId(),
        correlationId,
        ex);
    try {
      catalogAuditService.recordEjemplarDeletePartialFailure(
          actorUsuarioAppId,
          authorization.treeId(),
          authorization.especieId(),
          authorization.provinciaId(),
          FASE_CATALOG_DELETE,
          correlationId,
          ex);
    } catch (RuntimeException auditEx) {
      log.error(
          "No se pudo auditar el fallo parcial de borrado de ejemplar (ejemplarId={}, correlationId={})",
          authorization.treeId(),
          correlationId,
          auditEx);
    }
  }
}
