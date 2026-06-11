package com.mtl.catalog.dto;

import java.time.LocalDate;
import java.util.Map;

public record FieldObservationDto(
    LocalDate date, String text, String author, Map<String, Object> conditions) {}
