package com.mtl.catalog.dto;

import java.util.List;
import java.util.Map;

public record SpeciesEnrichmentReplaceRequest(
    List<String> synonyms,
    SpeciesDistributionDto distribution,
    Map<String, Object> ecologicalData,
    List<BibliographicReferenceDto> references) {}
