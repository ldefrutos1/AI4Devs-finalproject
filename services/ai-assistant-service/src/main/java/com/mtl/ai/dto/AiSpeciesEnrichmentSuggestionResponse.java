package com.mtl.ai.dto;

import java.util.List;
import java.util.Map;

public record AiSpeciesEnrichmentSuggestionResponse(
    List<String> synonyms,
    SpeciesDistributionDto distribution,
    Map<String, Object> ecologicalData,
    List<BibliographicReferenceDto> references) {}
