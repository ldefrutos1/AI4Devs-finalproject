package com.mtl.catalog.controller;

import com.mtl.catalog.application.PublicTreeEnrichmentQueryService;
import com.mtl.catalog.application.SpeciesEnrichmentService;
import com.mtl.catalog.application.TreeEnrichmentService;
import com.mtl.catalog.dto.PublicTreeEnrichmentResponse;
import com.mtl.catalog.dto.SpeciesEnrichmentReplaceRequest;
import com.mtl.catalog.dto.SpeciesEnrichmentResponse;
import com.mtl.catalog.dto.TreeEnrichmentReplaceRequest;
import com.mtl.catalog.dto.TreeEnrichmentResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de enriquecimiento Mongo (HU-015). Solo activos con `mtl.catalog.mongo.enabled=true`. */
@RestController
@RequestMapping("/api/catalog")
@Validated
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class CatalogEnrichmentController {

  private final SpeciesEnrichmentService speciesEnrichmentService;
  private final TreeEnrichmentService treeEnrichmentService;
  private final PublicTreeEnrichmentQueryService publicTreeEnrichmentQueryService;

  public CatalogEnrichmentController(
      SpeciesEnrichmentService speciesEnrichmentService,
      TreeEnrichmentService treeEnrichmentService,
      PublicTreeEnrichmentQueryService publicTreeEnrichmentQueryService) {
    this.speciesEnrichmentService = speciesEnrichmentService;
    this.treeEnrichmentService = treeEnrichmentService;
    this.publicTreeEnrichmentQueryService = publicTreeEnrichmentQueryService;
  }

  @GetMapping("/species/{speciesId}/enrichment")
  public SpeciesEnrichmentResponse getSpeciesEnrichment(
      @PathVariable long speciesId, @AuthenticationPrincipal Jwt jwt) {
    return speciesEnrichmentService.getSpeciesEnrichment(speciesId, jwt);
  }

  @PutMapping("/species/{speciesId}/enrichment")
  public SpeciesEnrichmentResponse replaceSpeciesEnrichment(
      @PathVariable long speciesId,
      @Valid @RequestBody SpeciesEnrichmentReplaceRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    return speciesEnrichmentService.replaceSpeciesEnrichment(speciesId, request, jwt);
  }

  @GetMapping("/trees/{treeId}/enrichment")
  public TreeEnrichmentResponse getTreeEnrichment(
      @PathVariable long treeId, @AuthenticationPrincipal Jwt jwt) {
    return treeEnrichmentService.getTreeEnrichment(treeId, jwt);
  }

  @PutMapping("/trees/{treeId}/enrichment")
  public TreeEnrichmentResponse replaceTreeEnrichment(
      @PathVariable long treeId,
      @Valid @RequestBody TreeEnrichmentReplaceRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    return treeEnrichmentService.replaceTreeEnrichment(treeId, request, jwt);
  }

  @GetMapping("/public/trees/{treeId}/enrichment")
  public PublicTreeEnrichmentResponse getPublicTreeEnrichment(
      @PathVariable long treeId, @AuthenticationPrincipal Jwt jwt) {
    return publicTreeEnrichmentQueryService.getPublishedTreeEnrichment(treeId, jwt);
  }
}
