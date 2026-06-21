package com.mtl.media.controller;

import com.mtl.media.application.MediaEjemplarPhotoContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
public class MediaEjemplarPhotoContentController {

  private final MediaEjemplarPhotoContentService photoContentService;

  public MediaEjemplarPhotoContentController(MediaEjemplarPhotoContentService photoContentService) {
    this.photoContentService = photoContentService;
  }

  @GetMapping("/trees/{treeId}/photos/{photoId}/content")
  public ResponseEntity<byte[]> getPhotoContent(
      @PathVariable long treeId, @PathVariable long photoId, Authentication authentication) {
    return photoContentService.loadVisiblePhotoBytes(treeId, photoId, resolveJwt(authentication));
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
