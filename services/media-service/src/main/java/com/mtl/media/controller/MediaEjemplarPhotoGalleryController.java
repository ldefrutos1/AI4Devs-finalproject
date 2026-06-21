package com.mtl.media.controller;

import com.mtl.media.application.MediaEjemplarPhotoGalleryService;
import com.mtl.media.application.MediaEjemplarPhotosDeleteService;
import com.mtl.media.domain.Fotografia;
import com.mtl.media.dto.EjemplarPhotoGalleryItemResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
public class MediaEjemplarPhotoGalleryController {

  private final MediaEjemplarPhotoGalleryService galleryService;
  private final MediaEjemplarPhotosDeleteService photosDeleteService;

  public MediaEjemplarPhotoGalleryController(
      MediaEjemplarPhotoGalleryService galleryService,
      MediaEjemplarPhotosDeleteService photosDeleteService) {
    this.galleryService = galleryService;
    this.photosDeleteService = photosDeleteService;
  }

  @GetMapping("/trees/{treeId}/photos")
  public List<EjemplarPhotoGalleryItemResponse> findByEjemplarId(
      @PathVariable long treeId, Authentication authentication) {
    Jwt jwt = resolveJwt(authentication);
    return galleryService.findVisiblePhotos(treeId, jwt).stream()
        .map(photo -> toResponse(treeId, photo))
        .toList();
  }

  @DeleteMapping("/trees/{treeId}/photos")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAllForEjemplar(@PathVariable long treeId, @AuthenticationPrincipal Jwt jwt) {
    photosDeleteService.deleteAllPhotosForEjemplar(treeId, jwt);
  }

  private EjemplarPhotoGalleryItemResponse toResponse(long treeId, Fotografia photo) {
    return new EjemplarPhotoGalleryItemResponse(
        photo.getFotografiaId(),
        buildReadUrl(treeId, photo.getFotografiaId()),
        photo.isEsPrincipal(),
        photo.getOrden(),
        photo.getTipoMime(),
        photo.getAnchoPx(),
        photo.getAltoPx(),
        photo.getCategoria());
  }

  /** URL relativa vía gateway (mismo origen que la SPA); evita acceso directo a MinIO desde el navegador. */
  private static String buildReadUrl(long treeId, long photoId) {
    return "/api/media/trees/" + treeId + "/photos/" + photoId + "/content";
  }

  private static Jwt resolveJwt(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return null;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof Jwt jwt) {
      return jwt;
    }
    return null;
  }
}
