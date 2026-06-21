package com.mtl.media.application;

import com.mtl.media.domain.CategoriaFotografia;
import com.mtl.media.domain.Fotografia;
import com.mtl.media.infrastructure.client.catalog.CatalogPublicTreeVisibilityGuard;
import com.mtl.media.infrastructure.persistence.jpa.repository.FotografiaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MediaPublicPrimaryPhotoService {

  private final FotografiaRepository fotografiaRepository;
  private final CatalogPublicTreeVisibilityGuard catalogPublicTreeVisibilityGuard;
  private final MediaStoredPhotoLoader storedPhotoLoader;

  public MediaPublicPrimaryPhotoService(
      FotografiaRepository fotografiaRepository,
      CatalogPublicTreeVisibilityGuard catalogPublicTreeVisibilityGuard,
      MediaStoredPhotoLoader storedPhotoLoader) {
    this.fotografiaRepository = fotografiaRepository;
    this.catalogPublicTreeVisibilityGuard = catalogPublicTreeVisibilityGuard;
    this.storedPhotoLoader = storedPhotoLoader;
  }

  /**
   * Comprueba en catálogo que el ejemplar es visible en el mismo contexto que el listado/detalle público
   * (JWT opcional para colaboradores que ven borradores). Luego devuelve bytes desde el almacén si hay
   * foto principal no eliminada.
   */
  public ResponseEntity<byte[]> loadPrimaryPhotoBytes(long treeId, Jwt jwt) {
    catalogPublicTreeVisibilityGuard.assertVisibleInPublicCatalog(treeId, jwt);

    Fotografia foto =
        fotografiaRepository
            .findPrincipalForEjemplar(treeId, CategoriaFotografia.PUBLIC)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sin fotografía principal"));

    return storedPhotoLoader.toImageResponse(foto);
  }
}
