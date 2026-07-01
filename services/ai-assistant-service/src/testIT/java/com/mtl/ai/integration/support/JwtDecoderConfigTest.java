package com.mtl.ai.integration.support;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class JwtDecoderConfigTest {

  public static final String TOKEN_ADMIN = "test-token-admin";
  public static final String TOKEN_COLABORADOR = "test-token-colaborador";
  public static final String TOKEN_ROL_NO_AUTORIZADO = "test-token-rol-no-autorizado";
  public static final String TOKEN_INVALIDO = "token-inexistente";

  public static final String SUBJECT_ADMIN = "it-subject-admin";
  public static final String SUBJECT_COLABORADOR = "it-subject-colaborador";

  private static final String ISSUER = "http://localhost:8180/realms/mtl";

  @Bean
  @Primary
  JwtDecoder jwtDecoder() {
    return token -> {
      if (TOKEN_ADMIN.equals(token)) {
        return jwt(token, SUBJECT_ADMIN, "ADMIN");
      }
      if (TOKEN_COLABORADOR.equals(token)) {
        return jwt(token, SUBJECT_COLABORADOR, "COLABORADOR");
      }
      if (TOKEN_ROL_NO_AUTORIZADO.equals(token)) {
        return jwt(token, "it-subject-visitante", "VISITANTE");
      }
      throw new BadJwtException("Token de prueba no reconocido");
    };
  }

  private static Jwt jwt(String tokenValue, String subject, String role) {
    return Jwt.withTokenValue(tokenValue)
        .header("alg", "none")
        .issuer(ISSUER)
        .subject(subject)
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .claim("realm_access", Map.of("roles", List.of(role)))
        .claim("email", subject + "@test.invalid")
        .build();
  }
}
