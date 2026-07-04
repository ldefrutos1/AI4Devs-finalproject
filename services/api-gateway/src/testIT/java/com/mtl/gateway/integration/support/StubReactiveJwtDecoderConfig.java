package com.mtl.gateway.integration.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

/**
 * Sustituye el decoder OIDC del emisor real en tests de integración que no deben contactar con Keycloak.
 */
@TestConfiguration
public class StubReactiveJwtDecoderConfig {

  @Bean
  @Primary
  ReactiveJwtDecoder stubReactiveJwtDecoder() {
    return token ->
        Mono.error(
            new BadJwtException(
                "Invalid JWT serialization: Missing dot delimiter(s) (stub IT, sin issuer)"));
  }
}
