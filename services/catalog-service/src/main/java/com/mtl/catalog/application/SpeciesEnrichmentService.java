package com.mtl.catalog.application;

import com.mtl.catalog.domain.Especie;
import com.mtl.catalog.dto.SpeciesEnrichmentReplaceRequest;
import com.mtl.catalog.dto.SpeciesEnrichmentResponse;
import com.mtl.catalog.exception.CatalogForbiddenException;
import com.mtl.catalog.exception.CatalogNotFoundException;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.EspecieRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import com.mtl.catalog.util.JwtRealmRoles;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class SpeciesEnrichmentService {

  private final EspecieRepository especieRepository;
  private final EspecieDetalleMongoRepository especieDetalleMongoRepository;

  public SpeciesEnrichmentService(
      EspecieRepository especieRepository,
      EspecieDetalleMongoRepository especieDetalleMongoRepository) {
    this.especieRepository = especieRepository;
    this.especieDetalleMongoRepository = especieDetalleMongoRepository;
  }

  @Transactional(readOnly = true)
  public SpeciesEnrichmentResponse getSpeciesEnrichment(long speciesId, Jwt jwt) {
    requireReaderRole(jwt);
    Especie especie = requireEspecie(speciesId);
    EspecieDetalleDocument document =
        especieDetalleMongoRepository.findById(speciesId).orElse(null);
    return SpeciesEnrichmentMapper.toResponse(
        speciesId, especie.getNombreCientifico(), especie.getNombreComun(), document);
  }

  @Transactional
  public SpeciesEnrichmentResponse replaceSpeciesEnrichment(
      long speciesId, SpeciesEnrichmentReplaceRequest request, Jwt jwt) {
    requireAdminRole(jwt);
    Especie especie = requireEspecie(speciesId);
    EspecieDetalleDocument document =
        SpeciesEnrichmentMapper.toDocument(
            speciesId, especie.getNombreCientifico(), especie.getNombreComun(), request);
    EspecieDetalleDocument saved = especieDetalleMongoRepository.save(document);
    return SpeciesEnrichmentMapper.toResponse(
        speciesId, especie.getNombreCientifico(), especie.getNombreComun(), saved);
  }

  private Especie requireEspecie(long speciesId) {
    return especieRepository
        .findById(speciesId)
        .orElseThrow(
            () ->
                new CatalogNotFoundException(
                    "No se encontró una especie con el identificador indicado."));
  }

  private static void requireReaderRole(Jwt jwt) {
    if (!JwtRealmRoles.hasRealmRole(jwt, "COLABORADOR")
        && !JwtRealmRoles.hasRealmRole(jwt, "ADMIN")) {
      throw new CatalogForbiddenException(
          "Se requiere rol COLABORADOR o ADMIN para consultar el enriquecimiento de la especie.");
    }
  }

  private static void requireAdminRole(Jwt jwt) {
    if (!JwtRealmRoles.hasRealmRole(jwt, "ADMIN")) {
      throw new CatalogForbiddenException(
          "Se requiere rol ADMIN para modificar el enriquecimiento de la especie.");
    }
  }
}
