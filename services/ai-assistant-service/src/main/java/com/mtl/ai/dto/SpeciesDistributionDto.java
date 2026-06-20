package com.mtl.ai.dto;

import java.util.List;

public record SpeciesDistributionDto(
    List<String> continents, List<String> countries, String description) {}
