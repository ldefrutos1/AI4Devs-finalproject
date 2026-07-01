package com.mtl.ai.integration;

import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_PATH;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_REQUEST_BODY_MINIMAL;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.ENRICHMENT_PATH;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.ENRICHMENT_REQUEST_BODY;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.TOKEN_INVALIDO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.integration.support.AiIntegrationTestBase;
import com.mtl.ai.integration.support.JwtDecoderConfigTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Seguridad transversal con contexto Spring completo. La matriz 401/403 por rol y cuerpos Problem
 * en controllers está en {@code *WebMvcTest}; aquí solo escenarios que exigen wiring real y token
 * rechazado por el {@link JwtDecoderConfigTest} stub.
 */
class AiEndpointsSecurityIT extends AiIntegrationTestBase {

  @Autowired private MockMvc mockMvc;

  @Test
  void sendMessage_withInvalidBearer_returns401() throws Exception {
    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_INVALIDO)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY_MINIMAL))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void suggest_withInvalidBearer_returns401() throws Exception {
    mockMvc
        .perform(
            post(ENRICHMENT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_INVALIDO)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ENRICHMENT_REQUEST_BODY))
        .andExpect(status().isUnauthorized());
  }
}
