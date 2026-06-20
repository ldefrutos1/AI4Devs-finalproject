package com.mtl.ai.controller;

import com.mtl.ai.application.SpeciesEnrichmentSuggestionService;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionRequest;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/species")
public class AiSpeciesEnrichmentController {

  private final SpeciesEnrichmentSuggestionService speciesEnrichmentSuggestionService;

  public AiSpeciesEnrichmentController(
      SpeciesEnrichmentSuggestionService speciesEnrichmentSuggestionService) {
    this.speciesEnrichmentSuggestionService = speciesEnrichmentSuggestionService;
  }

  @PostMapping("/enrichment-suggestions")
  public AiSpeciesEnrichmentSuggestionResponse suggestEnrichment(
      @Valid @RequestBody AiSpeciesEnrichmentSuggestionRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    return speciesEnrichmentSuggestionService.suggest(request, jwt);
  }
}
