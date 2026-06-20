package com.mtl.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiSpeciesEnrichmentSuggestionRequest(
    @NotBlank @Size(max = 255) String scientificName,
    @NotBlank @Size(max = 255) String commonName) {}
