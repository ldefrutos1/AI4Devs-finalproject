package com.mtl.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mtl.catalog.domain.Ejemplar;
import com.mtl.catalog.domain.Especie;
import com.mtl.catalog.dto.SpeciesEnrichmentReplaceRequest;
import com.mtl.catalog.dto.TreeEnrichmentReplaceRequest;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.EspecieRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
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
class SpeciesEnrichmentServiceTest {

  @Mock private EspecieRepository especieRepository;
  @Mock private EspecieDetalleMongoRepository especieDetalleMongoRepository;

  @InjectMocks private SpeciesEnrichmentService speciesEnrichmentService;

  @Test
  void getSpeciesEnrichment_sinDocumentoMongo_devuelveProyeccionSql() {
    Especie especie = new Especie();
    especie.setNombreCientifico("Quercus ilex");
    especie.setNombreComun("Encina");
    when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));
    when(especieDetalleMongoRepository.findById(1L)).thenReturn(Optional.empty());

    var response =
        speciesEnrichmentService.getSpeciesEnrichment(
            1L, jwtWithRoles("colab", "COLABORADOR"));

    assertThat(response.speciesId()).isEqualTo(1L);
    assertThat(response.scientificName()).isEqualTo("Quercus ilex");
    assertThat(response.commonName()).isEqualTo("Encina");
    assertThat(response.synonyms()).isNull();
  }

  @Test
  void replaceSpeciesEnrichment_reinyectaNombresSql() {
    Especie especie = new Especie();
    especie.setNombreCientifico("Quercus ilex");
    especie.setNombreComun("Encina");
    when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));
    when(especieDetalleMongoRepository.save(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        speciesEnrichmentService.replaceSpeciesEnrichment(
            1L,
            new SpeciesEnrichmentReplaceRequest(List.of("Holly Oak"), null, null, null),
            jwtWithRoles("admin", "ADMIN"));

    assertThat(response.scientificName()).isEqualTo("Quercus ilex");
    assertThat(response.synonyms()).containsExactly("Holly Oak");
  }

  private static Jwt jwtWithRoles(String subject, String role) {
    return Jwt.withTokenValue("test")
        .header("alg", "none")
        .subject(subject)
        .claim("realm_access", Map.of("roles", List.of(role)))
        .claim("email", subject + "@test.invalid")
        .build();
  }
}
