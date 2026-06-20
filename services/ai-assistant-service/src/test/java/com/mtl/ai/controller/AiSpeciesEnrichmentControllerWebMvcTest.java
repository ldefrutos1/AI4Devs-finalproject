package com.mtl.ai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.application.SpeciesEnrichmentSuggestionService;
import com.mtl.ai.config.AiSecurityConfig;
import com.mtl.ai.config.JwtDecoderConfigTest;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionResponse;
import com.mtl.ai.exception.AiAssistantException;
import com.mtl.ai.web.CorrelationIdFilter;
import com.mtl.ai.web.error.AiAssistantExceptionHandler;
import com.mtl.ai.web.error.ProblemAccessDeniedHandler;
import com.mtl.ai.web.error.ProblemAuthenticationEntryPoint;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = AiSpeciesEnrichmentController.class)
@Import({
  AiSecurityConfig.class,
  ProblemAuthenticationEntryPoint.class,
  ProblemAccessDeniedHandler.class,
  AiAssistantExceptionHandler.class,
  CorrelationIdFilter.class,
  JwtDecoderConfigTest.class,
  AiSpeciesEnrichmentControllerWebMvcTest.JsonMapperWebMvcTestConfiguration.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiSpeciesEnrichmentControllerWebMvcTest {

  private static final String VALID_REQUEST_BODY =
      "{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}";

  @TestConfiguration
  static class JsonMapperWebMvcTestConfiguration {
    @org.springframework.context.annotation.Bean
    JsonMapper aiWebMvcTestJsonMapper() {
      return JsonMapper.builder().build();
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private SpeciesEnrichmentSuggestionService speciesEnrichmentSuggestionService;

  @Test
  void suggest_withoutBearer_returns401Problem() throws Exception {
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
  void suggest_withCollaborator_returns403Problem() throws Exception {
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
  void suggest_withAdmin_returns200AndBody() throws Exception {
    when(speciesEnrichmentSuggestionService.suggest(any(), any()))
        .thenReturn(
            new AiSpeciesEnrichmentSuggestionResponse(
                List.of("Encina"),
                null,
                Map.of("habitat", List.of("bosque"), "growthRate", "moderate"),
                null));

    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.synonyms[0]").value("Encina"))
        .andExpect(jsonPath("$.ecologicalData.growthRate").value("moderate"));
  }

  @Test
  void suggest_withUnauthorizedRole_returns403Problem() throws Exception {
    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + JwtDecoderConfigTest.TOKEN_ROL_NO_AUTORIZADO)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.title").value("Prohibido"));
  }

  @Test
  void suggest_withInvalidBody_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scientificName\":\"\",\"commonName\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Petición inválida"));
  }

  @Test
  void suggest_whenServiceThrowsNotFound_returns404Problem() throws Exception {
    when(speciesEnrichmentSuggestionService.suggest(any(), any()))
        .thenThrow(
            new AiAssistantException(
                HttpStatus.NOT_FOUND,
                "Sin resultado IA",
                "La IA no devolvió contenido utilizable para la especie solicitada."));

    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Sin resultado IA"))
        .andExpect(
            jsonPath("$.detail")
                .value("La IA no devolvió contenido utilizable para la especie solicitada."))
        .andExpect(jsonPath("$.instance").value("/api/ai/species/enrichment-suggestions"));
  }

  @Test
  void suggest_whenServiceThrowsUnprocessableEntity_returns422Problem() throws Exception {
    when(speciesEnrichmentSuggestionService.suggest(any(), any()))
        .thenThrow(
            new AiAssistantException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Respuesta IA inválida",
                "La IA devolvió un JSON no válido."));

    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Respuesta IA inválida"))
        .andExpect(jsonPath("$.detail").value("La IA devolvió un JSON no válido."));
  }

  @Test
  void suggest_whenServiceThrowsBadGateway_returns502Problem() throws Exception {
    when(speciesEnrichmentSuggestionService.suggest(any(), any()))
        .thenThrow(
            new AiAssistantException(
                HttpStatus.BAD_GATEWAY,
                "Proveedor IA no disponible",
                "No se pudo completar la consulta contra el proveedor de IA."));

    mockMvc
        .perform(
            post("/api/ai/species/enrichment-suggestions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtDecoderConfigTest.TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
        .andExpect(status().isBadGateway())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Proveedor IA no disponible"))
        .andExpect(
            jsonPath("$.detail")
                .value("No se pudo completar la consulta contra el proveedor de IA."))
        .andExpect(header().exists(CorrelationIdFilter.HEADER_NAME));
  }
}
