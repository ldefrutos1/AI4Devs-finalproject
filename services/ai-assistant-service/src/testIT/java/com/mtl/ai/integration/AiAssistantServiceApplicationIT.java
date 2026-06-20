package com.mtl.ai.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.application.SpeciesEnrichmentSuggestionService;
import com.mtl.ai.config.JwtDecoderConfigTest;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionResponse;
import java.util.List;
import java.util.Map;
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

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtDecoderConfigTest.class)
class AiAssistantServiceApplicationIT {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private SpeciesEnrichmentSuggestionService speciesEnrichmentSuggestionService;

  @Test
  void suggest_withoutBearer_returns401ProblemJson() throws Exception {
    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("No autenticado"));
  }

  @Test
  void suggest_withCollaborator_returns403ProblemJson() throws Exception {
    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .header(
                    HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_COLABORADOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Prohibido"));
  }

  @Test
  void suggest_withAdmin_returns200() throws Exception {
    when(speciesEnrichmentSuggestionService.suggest(any(), any()))
        .thenReturn(
            new AiSpeciesEnrichmentSuggestionResponse(
                List.of("Encina"),
                null,
                Map.of("growthRate", "moderate"),
                null));

    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.synonyms[0]").value("Encina"));
  }
}
