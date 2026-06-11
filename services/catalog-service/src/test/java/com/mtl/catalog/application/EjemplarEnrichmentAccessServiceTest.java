package com.mtl.catalog.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mtl.catalog.domain.Ejemplar;
import com.mtl.catalog.domain.UsuarioApp;
import com.mtl.catalog.exception.CatalogForbiddenException;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.EjemplarRepository;
import com.mtl.catalog.util.OidcUserProfileExtractor.OidcUserProfile;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class EjemplarEnrichmentAccessServiceTest {

  @Mock private EjemplarRepository ejemplarRepository;
  @Mock private UsuarioAppMaterializationService usuarioAppMaterializationService;

  @InjectMocks private EjemplarEnrichmentAccessService service;

  @Test
  void requireWritableEjemplar_colaboradorAjeno_devuelve403() {
    Ejemplar ejemplar = ejemplar(42L, 99L);
    when(ejemplarRepository.findById(42L)).thenReturn(Optional.of(ejemplar));
    when(usuarioAppMaterializationService.materialize(any(OidcUserProfile.class)))
        .thenReturn(usuario(7L));

    assertThatThrownBy(() -> service.requireWritableEjemplar(42L, collaboratorJwt()))
        .isInstanceOf(CatalogForbiddenException.class)
        .hasMessageContaining("modificar");
  }

  private static Jwt collaboratorJwt() {
    return Jwt.withTokenValue("test")
        .header("alg", "none")
        .subject("colab")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .claim("realm_access", Map.of("roles", List.of("COLABORADOR")))
        .claim("email", "colab@test.invalid")
        .build();
  }

  private static UsuarioApp usuario(long id) {
    UsuarioApp u = new UsuarioApp();
    u.setId(id);
    return u;
  }

  private static Ejemplar ejemplar(long id, long ownerId) {
    Ejemplar ejemplar = new Ejemplar();
    ejemplar.setId(id);
    ejemplar.setUsuarioApp(usuario(ownerId));
    ejemplar.setLatitud(BigDecimal.ONE);
    ejemplar.setLongitud(BigDecimal.ONE);
    return ejemplar;
  }
}
