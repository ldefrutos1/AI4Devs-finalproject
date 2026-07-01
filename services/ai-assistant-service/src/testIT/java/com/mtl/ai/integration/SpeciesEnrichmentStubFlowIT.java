package com.mtl.ai.integration;

import static com.mtl.ai.integration.support.AiIntegrationFixtures.ENRICHMENT_PATH;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.ENRICHMENT_REQUEST_BODY;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.SUBJECT_ADMIN;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.TOKEN_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.application.SpeciesEnrichmentSuggestionService;
import com.mtl.ai.domain.AuditoriaUsoIa;
import com.mtl.ai.infrastructure.persistence.jpa.repository.AuditoriaUsoIaRepository;
import com.mtl.ai.integration.support.AiIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Flujo completo HU-016 (TASK-04/05) con proveedor {@code stub} real: contrato HTTP, validación y
 * persistencia R3 en {@code AUDITORIA_USO_IA}. Validación estructural sin persistir:
 * {@code SpeciesEnrichmentValidationIT}.
 */
class SpeciesEnrichmentStubFlowIT extends AiIntegrationTestBase {

  @Autowired private MockMvc mockMvc;
  @Autowired private AuditoriaUsoIaRepository auditoriaUsoIaRepository;

  @BeforeEach
  void cleanAuditoria() {
    auditoriaUsoIaRepository.deleteAll();
  }

  @Test
  void suggest_withStubProvider_returnsContractCompatibleResponseAndAudits() throws Exception {
    mockMvc
        .perform(
            post(ENRICHMENT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ENRICHMENT_REQUEST_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.synonyms").isArray())
        .andExpect(jsonPath("$.distribution.continents[0]").value("Europa"))
        .andExpect(jsonPath("$.ecologicalData.growthRate").value("moderate"))
        .andExpect(jsonPath("$.references[0].title").exists());

    assertThat(auditoriaUsoIaRepository.count()).isEqualTo(1);
    AuditoriaUsoIa auditoria = auditoriaUsoIaRepository.findAll().getFirst();
    assertThat(auditoria.getSubjectOidc()).isEqualTo(SUBJECT_ADMIN);
    assertThat(auditoria.getTipoUsoIa())
        .isEqualTo(SpeciesEnrichmentSuggestionService.TIPO_USO_IA);
    assertThat(auditoria.getEjemplarId()).isNull();
    assertThat(auditoria.getPrompt()).contains("Quercus ilex");
    assertThat(auditoria.getPrompt()).contains("Encina");
    assertThat(auditoria.getResultadoResumen()).isEqualTo("stub:Quercus ilex");
    assertThat(auditoria.getConsultadoEn()).isNotNull();
  }
}
