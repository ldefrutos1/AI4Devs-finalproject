package com.mtl.ai.controller;

import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_PATH;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_REQUEST_BODY;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_REQUEST_BODY_INVALID_TREE_ID;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.CONVERSATION_ID;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.TOKEN_ADMIN;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.TOKEN_COLABORADOR;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.TOKEN_ROL_NO_AUTORIZADO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.application.ChatMessageService;
import com.mtl.ai.application.ChatRateLimitMessages;
import com.mtl.ai.config.AiSecurityConfig;
import com.mtl.ai.controller.support.WebMvcTestJsonMapperConfig;
import com.mtl.ai.dto.AiChatAssistantMessage;
import com.mtl.ai.dto.AiChatMessageResponse;
import com.mtl.ai.exception.AiAssistantException;
import com.mtl.ai.integration.support.JwtDecoderConfigTest;
import com.mtl.ai.web.CorrelationIdFilter;
import com.mtl.ai.web.error.AiAssistantExceptionHandler;
import com.mtl.ai.web.error.ProblemAccessDeniedHandler;
import com.mtl.ai.web.error.ProblemAuthenticationEntryPoint;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AiChatController.class)
@Import({
  AiSecurityConfig.class,
  ProblemAuthenticationEntryPoint.class,
  ProblemAccessDeniedHandler.class,
  AiAssistantExceptionHandler.class,
  CorrelationIdFilter.class,
  JwtDecoderConfigTest.class,
  WebMvcTestJsonMapperConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiChatControllerWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ChatMessageService chatMessageService;

  @Test
  void sendMessage_withoutBearer_returns401Problem() throws Exception {
    mockMvc
        .perform(
            post(CHAT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("No autenticado"));
  }

  @Test
  void sendMessage_withUnauthorizedRole_returns403Problem() throws Exception {
    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_ROL_NO_AUTORIZADO)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.title").value("Prohibido"));
  }

  @Test
  void sendMessage_withCollaborator_returns200AndBody() throws Exception {
    when(chatMessageService.process(any(), any()))
        .thenReturn(
            new AiChatMessageResponse(
                CONVERSATION_ID,
                AiChatAssistantMessage.create(
                    "Respuesta orientativa.", OffsetDateTime.parse("2026-07-01T10:15:30Z"))));

    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_COLABORADOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conversationId").value(CONVERSATION_ID.toString()))
        .andExpect(jsonPath("$.message.role").value("assistant"))
        .andExpect(jsonPath("$.message.content").value("Respuesta orientativa."))
        .andExpect(header().exists(CorrelationIdFilter.HEADER_NAME));
  }

  @Test
  void sendMessage_withAdmin_returns200AndBody() throws Exception {
    when(chatMessageService.process(any(), any()))
        .thenReturn(
            new AiChatMessageResponse(
                CONVERSATION_ID,
                AiChatAssistantMessage.create(
                    "Respuesta admin.", OffsetDateTime.parse("2026-07-01T10:15:30Z"))));

    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message.content").value("Respuesta admin."));
  }

  @Test
  void sendMessage_withInvalidBody_returns400() throws Exception {
    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_COLABORADOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY_INVALID_TREE_ID))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Petición inválida"));
  }

  @Test
  void sendMessage_whenServiceThrowsBadGateway_returns502Problem() throws Exception {
    when(chatMessageService.process(any(), any()))
        .thenThrow(
            new AiAssistantException(
                HttpStatus.BAD_GATEWAY,
                "Proveedor IA no disponible",
                "No se pudo completar la consulta contra el proveedor de IA."));

    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_COLABORADOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY))
        .andExpect(status().isBadGateway())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Proveedor IA no disponible"));
  }

  @Test
  void sendMessage_whenRateLimited_returns429Problem() throws Exception {
    when(chatMessageService.process(any(), any()))
        .thenThrow(
            new AiAssistantException(
                HttpStatus.TOO_MANY_REQUESTS,
                ChatRateLimitMessages.TITLE_TOO_MANY_REQUESTS,
                ChatRateLimitMessages.DETAIL_TOO_MANY_REQUESTS));

    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_COLABORADOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value(ChatRateLimitMessages.TITLE_TOO_MANY_REQUESTS))
        .andExpect(jsonPath("$.detail").value(ChatRateLimitMessages.DETAIL_TOO_MANY_REQUESTS));
  }
}
