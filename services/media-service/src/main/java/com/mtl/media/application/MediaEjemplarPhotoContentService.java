package com.mtl.media.application;

import com.mtl.media.domain.Fotografia;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MediaEjemplarPhotoContentService {

  private final MediaEjemplarPhotoGalleryService galleryService;
  private final MediaStoredPhotoLoader storedPhotoLoader;

  public MediaEjemplarPhotoContentService(
      MediaEjemplarPhotoGalleryService galleryService, MediaStoredPhotoLoader storedPhotoLoader) {
    this.galleryService = galleryService;
    this.storedPhotoLoader = storedPhotoLoader;
  }

  public ResponseEntity<byte[]> loadVisiblePhotoBytes(long treeId, long photoId, Jwt jwt) {
    Fotografia photo =
        galleryService.findVisiblePhotos(treeId, jwt).stream()
            .filter(item -> item.getFotografiaId() != null && item.getFotografiaId() == photoId)
            .findFirst()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fotografía no encontrada"));
    return storedPhotoLoader.toImageResponse(photo);
  }
}
