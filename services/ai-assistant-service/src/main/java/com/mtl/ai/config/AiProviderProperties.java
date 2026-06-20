package com.mtl.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "mtl.ai.provider")
public record AiProviderProperties(@DefaultValue("stub") String mode) {}
