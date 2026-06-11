package com.mtl.catalog.application;

import com.mtl.catalog.dto.PublicTreeEnrichmentResponse;
import com.mtl.catalog.dto.SpeciesEnrichmentResponse;
import com.mtl.catalog.dto.TreeEnrichmentResponse;
import com.mtl.catalog.exception.CatalogNotFoundException;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.EjemplarRepository;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.PublicEjemplarReadRepository;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.projection.PublicEjemplarDetailRow;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class PublicTreeEnrichmentQueryService {

  private static final Set<String> PRIVILEGED_ROLES = Set.of("COLABORADOR", "ADMIN");
  private static final String PUBLICADO = "PUBLICADO";
  private static final String PUBLICO = "PUBLICO";

  private final PublicEjemplarReadRepository publicEjemplarReadRepository;
  private final EjemplarRepository ejemplarRepository;
  private final EspecieDetalleMongoRepository especieDetalleMongoRepository;
  private final EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository;

  public PublicTreeEnrichmentQueryService(
      PublicEjemplarReadRepository publicEjemplarReadRepository,
      EjemplarRepository ejemplarRepository,
      EspecieDetalleMongoRepository especieDetalleMongoRepository,
      EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository) {
    this.publicEjemplarReadRepository = publicEjemplarReadRepository;
    this.ejemplarRepository = ejemplarRepository;
    this.especieDetalleMongoRepository = especieDetalleMongoRepository;
    this.ejemplarDetalleMongoRepository = ejemplarDetalleMongoRepository;
  }

  @Transactional(readOnly = true)
  public PublicTreeEnrichmentResponse getPublishedTreeEnrichment(long treeId, Jwt jwt) {
    PublicEjemplarDetailRow row = requirePublishedEjemplar(treeId, jwt);
    long speciesId =
        ejemplarRepository
            .findById(treeId)
            .orElseThrow(
                () ->
                    new CatalogNotFoundException(
                        "No se encontró el árbol público solicitado con id " + treeId))
            .getEspecieId();

    EspecieDetalleDocument speciesDocument =
        especieDetalleMongoRepository.findById(speciesId).orElse(null);
    EjemplarDetalleDocument treeDocument =
        ejemplarDetalleMongoRepository.findById(treeId).orElse(null);

    SpeciesEnrichmentResponse speciesEnrichment =
        hasSpeciesEnrichmentContent(speciesDocument)
            ? SpeciesEnrichmentMapper.toResponse(
                speciesId, row.getScientificName(), row.getCommonName(), speciesDocument)
            : null;

    TreeEnrichmentResponse treeEnrichment =
        hasTreeEnrichmentContent(treeDocument)
            ? TreeEnrichmentMapper.toResponse(treeId, speciesId, treeDocument)
            : null;

    return new PublicTreeEnrichmentResponse(speciesEnrichment, treeEnrichment);
  }

  private PublicEjemplarDetailRow requirePublishedEjemplar(long treeId, Jwt jwt) {
    AccessScope scope = resolveScope(jwt);
    return publicEjemplarReadRepository
        .findPublicEjemplarDetailRow(treeId, scope.estado(), scope.visibilidad())
        .orElseThrow(
            () ->
                new CatalogNotFoundException(
                    "No se encontró el árbol público solicitado con id " + treeId));
  }

  private static AccessScope resolveScope(Jwt jwt) {
    if (!hasPrivilegedRole(jwt)) {
      return new AccessScope(PUBLICADO, PUBLICO);
    }
    return new AccessScope(null, null);
  }

  private static boolean hasPrivilegedRole(Jwt jwt) {
    if (jwt == null) {
      return false;
    }
    Object realmAccess = jwt.getClaims().get("realm_access");
    if (!(realmAccess instanceof java.util.Map<?, ?> accessMap)) {
      return false;
    }
    Object rolesObj = accessMap.get("roles");
    if (!(rolesObj instanceof java.util.List<?> roles)) {
      return false;
    }
    return roles.stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(String::toUpperCase)
        .anyMatch(PRIVILEGED_ROLES::contains);
  }

  private static boolean hasSpeciesEnrichmentContent(EspecieDetalleDocument document) {
    if (document == null) {
      return false;
    }
    return document.getSinonimos() != null && !document.getSinonimos().isEmpty()
        || document.getDistribucion() != null && !document.getDistribucion().isEmpty()
        || document.getDatosEcologicos() != null && !document.getDatosEcologicos().isEmpty()
        || document.getReferencias() != null && !document.getReferencias().isEmpty();
  }

  private static boolean hasTreeEnrichmentContent(EjemplarDetalleDocument document) {
    if (document == null) {
      return false;
    }
    return document.getMedidas() != null && !document.getMedidas().isEmpty()
        || document.getEstadoSanitario() != null && !document.getEstadoSanitario().isEmpty()
        || document.getEtiquetas() != null && !document.getEtiquetas().isEmpty()
        || document.getObservaciones() != null && !document.getObservaciones().isEmpty();
  }

  private record AccessScope(String estado, String visibilidad) {}
}
