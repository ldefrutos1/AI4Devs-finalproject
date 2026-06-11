package com.mtl.catalog.dto;

import java.util.List;
import java.util.Map;

public record TreeEnrichmentReplaceRequest(
    Map<String, Object> measurements,
    Map<String, Object> healthStatus,
    List<String> tags,
    List<FieldObservationDto> observations) {}
