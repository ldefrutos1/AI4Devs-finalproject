package com.mtl.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.config.JwtDecoderConfigTest;
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
import org.springframework.test.web.servlet.MockMvc;

/**
 * Flujo completo HU-016 TASK-05 con proveedor {@code stub} real (sin mock del LLM): contrato HTTP,
 * validación y auditoría.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtDecoderConfigTest.class)
class SpeciesEnrichmentStubFlowIT {

  private static final String PATH = "/api/ai/species/enrichment-suggestions";

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
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.synonyms").isArray())
        .andExpect(jsonPath("$.distribution.continents[0]").value("Europa"))
        .andExpect(jsonPath("$.ecologicalData.growthRate").value("moderate"))
        .andExpect(jsonPath("$.references[0].title").exists());

    assertThat(auditoriaUsoIaRepository.count()).isEqualTo(1);
    assertThat(auditoriaUsoIaRepository.findAll().getFirst().getSubjectOidc())
        .isEqualTo("it-subject-admin");
  }
}
