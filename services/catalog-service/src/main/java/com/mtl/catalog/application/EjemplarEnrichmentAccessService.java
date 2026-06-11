package com.mtl.catalog.application;

import com.mtl.catalog.domain.Ejemplar;
import com.mtl.catalog.domain.UsuarioApp;
import com.mtl.catalog.exception.CatalogForbiddenException;
import com.mtl.catalog.exception.CatalogNotFoundException;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.EjemplarRepository;
import com.mtl.catalog.util.JwtRealmRoles;
import com.mtl.catalog.util.OidcUserProfileExtractor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Autorización de acceso a enriquecimiento de ejemplar (mismas reglas que HU-008). */
@Service
public class EjemplarEnrichmentAccessService {

  private final EjemplarRepository ejemplarRepository;
  private final UsuarioAppMaterializationService usuarioAppMaterializationService;

  public EjemplarEnrichmentAccessService(
      EjemplarRepository ejemplarRepository,
      UsuarioAppMaterializationService usuarioAppMaterializationService) {
    this.ejemplarRepository = ejemplarRepository;
    this.usuarioAppMaterializationService = usuarioAppMaterializationService;
  }

  @Transactional(readOnly = true)
  public Ejemplar requireReadableEjemplar(long ejemplarId, Jwt jwt) {
    return authorize(ejemplarId, jwt, false);
  }

  @Transactional(readOnly = true)
  public Ejemplar requireWritableEjemplar(long ejemplarId, Jwt jwt) {
    return authorize(ejemplarId, jwt, true);
  }

  private Ejemplar authorize(long ejemplarId, Jwt jwt, boolean write) {
    Ejemplar ejemplar =
        ejemplarRepository
            .findById(ejemplarId)
            .orElseThrow(
                () ->
                    new CatalogNotFoundException(
                        "No se encontró un árbol con el identificador indicado."));

    UsuarioApp actor =
        usuarioAppMaterializationService.materialize(OidcUserProfileExtractor.extract(jwt));
    boolean admin = JwtRealmRoles.hasRealmRole(jwt, "ADMIN");
    boolean collaborator = JwtRealmRoles.hasRealmRole(jwt, "COLABORADOR");

    if (!admin && !collaborator) {
      throw new CatalogForbiddenException(
          "Se requiere rol COLABORADOR o ADMIN para consultar el enriquecimiento del árbol.");
    }

    if (!admin && !ejemplar.getUsuarioAppId().equals(actor.getId())) {
      String action = write ? "modificar" : "consultar";
      throw new CatalogForbiddenException(
          "No tiene permiso para " + action + " el enriquecimiento de esta ficha de árbol.");
    }

    return ejemplar;
  }
}
