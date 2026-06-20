package com.mtl.ai.integration;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.application.AiResponseErrorMessages;
import com.mtl.ai.application.SpeciesEnrichmentAiProvider;
import com.mtl.ai.config.JwtDecoderConfigTest;
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
 * Integración HU-016 TASK-03/05: seguridad ADMIN, validación estructural y errores del proveedor IA
 * (LLM mockeado).
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtDecoderConfigTest.class)
class SpeciesEnrichmentValidationIT {

  private static final String PATH = "/api/ai/species/enrichment-suggestions";
  private static final String REQUEST_BODY =
      "{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private SpeciesEnrichmentAiProvider speciesEnrichmentAiProvider;

  @Test
  void suggest_withCollaborator_returns403ProblemJson() throws Exception {
    mockMvc
        .perform(
            post(PATH)
                .header(
                    HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_COLABORADOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Prohibido"));
  }

  @Test
  void suggest_withAdminAndInvalidLlmJson_returns422ProblemJson() throws Exception {
    when(speciesEnrichmentAiProvider.requestSuggestion(anyString(), anyString(), anyString()))
        .thenReturn(
            new SpeciesEnrichmentAiProvider.ProviderResponse(
                "{\"synonyms\":[\"Encina\"],\"unexpected\":true}", "test:invalid"));

    mockMvc
        .perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value(AiResponseErrorMessages.TITLE_INVALID))
        .andExpect(jsonPath("$.detail").value(containsString("orientativa")));
  }

  @Test
  void suggest_withAdminAndValidLlmJson_returns200() throws Exception {
    when(speciesEnrichmentAiProvider.requestSuggestion(anyString(), anyString(), anyString()))
        .thenReturn(
            new SpeciesEnrichmentAiProvider.ProviderResponse(
                """
                {
                  "synonyms": ["Encina"],
                  "ecologicalData": {
                    "growthRate": "moderate",
                    "leafType": "evergreen"
                  }
                }
                """,
                "test:valid"));

    mockMvc
        .perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.synonyms[0]").value("Encina"))
        .andExpect(jsonPath("$.ecologicalData.growthRate").value("moderate"));
  }

  @Test
  void suggest_withAdminAndBlankLlmJson_returns404ProblemJson() throws Exception {
    when(speciesEnrichmentAiProvider.requestSuggestion(anyString(), anyString(), anyString()))
        .thenReturn(new SpeciesEnrichmentAiProvider.ProviderResponse("   ", "test:empty"));

    mockMvc
        .perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value(AiResponseErrorMessages.TITLE_NOT_FOUND));
  }

  @Test
  void suggest_withAdminAndFullContractJson_returnsAllTopLevelFields() throws Exception {
    when(speciesEnrichmentAiProvider.requestSuggestion(anyString(), anyString(), anyString()))
        .thenReturn(
            new SpeciesEnrichmentAiProvider.ProviderResponse(
                """
                {
                  "synonyms": ["Encina"],
                  "distribution": {
                    "continents": ["Europa"],
                    "countries": ["España"],
                    "description": "Mediterráneo."
                  },
                  "ecologicalData": {
                    "habitat": ["bosque"],
                    "growthRate": "moderate",
                    "leafType": "evergreen"
                  },
                  "references": [
                    {
                      "title": "Flora",
                      "authors": ["Autor"],
                      "source": "CSIC",
                      "year": 2020,
                      "url": "https://example.org/ref"
                    }
                  ]
                }
                """,
                "test:full"));

    mockMvc
        .perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.synonyms[0]").value("Encina"))
        .andExpect(jsonPath("$.distribution.countries[0]").value("España"))
        .andExpect(jsonPath("$.ecologicalData.leafType").value("evergreen"))
        .andExpect(jsonPath("$.references[0].url").value("https://example.org/ref"));
  }
}
