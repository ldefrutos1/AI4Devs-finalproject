package com.mtl.catalog.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "mtl.media")
@Validated
public record MediaClientProperties(
    @NotBlank String baseUrl, @NotNull Duration connectTimeout, @NotNull Duration readTimeout) {}
