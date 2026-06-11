package com.mtl.catalog.dto;

import java.util.List;

public record BibliographicReferenceDto(
    String title, List<String> authors, String source, Integer year, String url) {}
