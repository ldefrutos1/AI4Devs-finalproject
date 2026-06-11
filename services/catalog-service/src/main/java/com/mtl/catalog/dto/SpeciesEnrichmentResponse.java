package com.mtl.catalog.dto;

import java.util.List;
import java.util.Map;

public record SpeciesEnrichmentResponse(
    long speciesId,
    String scientificName,
    String commonName,
    List<String> synonyms,
    SpeciesDistributionDto distribution,
    Map<String, Object> ecologicalData,
    List<BibliographicReferenceDto> references) {}
