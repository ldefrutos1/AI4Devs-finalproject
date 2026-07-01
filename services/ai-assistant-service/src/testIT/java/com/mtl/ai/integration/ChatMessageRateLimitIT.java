package com.mtl.ai.integration;

import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_PATH;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_REQUEST_BODY;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.TOKEN_COLABORADOR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.application.ChatMessageRateLimiter;
import com.mtl.ai.application.ChatRateLimitMessages;
import com.mtl.ai.integration.support.AiIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Integración HU-010 TASK-05: rate limit en memoria devuelve 429 Problem Details. */
class ChatMessageRateLimitIT extends AiIntegrationTestBase {

  @Autowired private MockMvc mockMvc;
  @Autowired private ChatMessageRateLimiter chatMessageRateLimiter;

  @BeforeEach
  void resetRateLimiter() {
    chatMessageRateLimiter.resetForTests();
  }

  @Test
  void sendMessage_secondRequestWithinMinInterval_returns429Problem() throws Exception {
    var requestBuilder =
        post(CHAT_PATH)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_COLABORADOR)
            .contentType(MediaType.APPLICATION_JSON)
            .content(CHAT_REQUEST_BODY);

    mockMvc.perform(requestBuilder).andExpect(status().isOk());

    mockMvc
        .perform(requestBuilder)
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.title").value(ChatRateLimitMessages.TITLE_TOO_MANY_REQUESTS))
        .andExpect(jsonPath("$.detail").value(ChatRateLimitMessages.DETAIL_TOO_MANY_REQUESTS));
  }
}
