package com.mtl.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.application.SpeciesEnrichmentAiProvider;
import com.mtl.ai.application.SpeciesEnrichmentSuggestionService;
import com.mtl.ai.config.JwtDecoderConfigTest;
import com.mtl.ai.domain.AuditoriaUsoIa;
import com.mtl.ai.infrastructure.persistence.jpa.repository.AuditoriaUsoIaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integración HU-016 TASK-04: persistencia R3 en {@code AUDITORIA_USO_IA} tras consulta IA
 * exitosa.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtDecoderConfigTest.class)
class AuditoriaUsoIaPersistenceIT {

  private static final String PATH = "/api/ai/species/enrichment-suggestions";
  private static final String REQUEST_BODY =
      "{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}";

  @Autowired private MockMvc mockMvc;
  @Autowired private AuditoriaUsoIaRepository auditoriaUsoIaRepository;

  @MockitoBean private SpeciesEnrichmentAiProvider speciesEnrichmentAiProvider;

  @BeforeEach
  void cleanAuditoria() {
    auditoriaUsoIaRepository.deleteAll();
  }

  @Test
  void suggest_success_persistsAuditoriaUsoIa() throws Exception {
    when(speciesEnrichmentAiProvider.requestSuggestion(anyString(), anyString(), anyString()))
        .thenReturn(
            new SpeciesEnrichmentAiProvider.ProviderResponse(
                """
                {
                  "synonyms": ["Encina"],
                  "ecologicalData": { "growthRate": "moderate" }
                }
                """,
                "stub:Quercus ilex"));

    mockMvc
        .perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isOk());

    assertThat(auditoriaUsoIaRepository.count()).isEqualTo(1);
    AuditoriaUsoIa auditoria = auditoriaUsoIaRepository.findAll().getFirst();
    assertThat(auditoria.getSubjectOidc()).isEqualTo("it-subject-admin");
    assertThat(auditoria.getTipoUsoIa())
        .isEqualTo(SpeciesEnrichmentSuggestionService.TIPO_USO_IA);
    assertThat(auditoria.getEjemplarId()).isNull();
    assertThat(auditoria.getPrompt()).contains("Quercus ilex");
    assertThat(auditoria.getPrompt()).contains("Encina");
    assertThat(auditoria.getResultadoResumen()).isEqualTo("stub:Quercus ilex");
    assertThat(auditoria.getConsultadoEn()).isNotNull();
  }

  @Test
  void suggest_validationFailure_doesNotPersistAuditoria() throws Exception {
    when(speciesEnrichmentAiProvider.requestSuggestion(anyString(), anyString(), anyString()))
        .thenReturn(
            new SpeciesEnrichmentAiProvider.ProviderResponse(
                "{\"synonyms\":[\"Encina\"],\"unexpected\":true}", "stub:invalid"));

    mockMvc
        .perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isUnprocessableEntity());

    assertThat(auditoriaUsoIaRepository.count()).isZero();
  }
}
