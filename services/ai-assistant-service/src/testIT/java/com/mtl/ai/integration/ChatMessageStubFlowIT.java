package com.mtl.ai.integration;

import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_PATH;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_REQUEST_BODY;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.CHAT_REQUEST_BODY_ASSISTANT_LAST_TURN;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.CONVERSATION_ID_STRING;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.TREE_ID;
import static com.mtl.ai.integration.support.AiIntegrationFixtures.chatResultSummary;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.SUBJECT_COLABORADOR;
import static com.mtl.ai.integration.support.JwtDecoderConfigTest.TOKEN_COLABORADOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.ai.application.ChatMessageRateLimiter;
import com.mtl.ai.application.ChatMessageService;
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
 * Flujo completo HU-010 (TASK-04/05) con proveedor {@code stub} real: contrato HTTP, validación,
 * rate limit y persistencia R3 en {@code AUDITORIA_USO_IA}. Fallo del proveedor sin auditoría:
 * {@code ChatMessageServiceTest}.
 */
class ChatMessageStubFlowIT extends AiIntegrationTestBase {

  @Autowired private MockMvc mockMvc;
  @Autowired private AuditoriaUsoIaRepository auditoriaUsoIaRepository;
  @Autowired private ChatMessageRateLimiter chatMessageRateLimiter;

  @BeforeEach
  void setUp() {
    chatMessageRateLimiter.resetForTests();
    auditoriaUsoIaRepository.deleteAll();
  }

  @Test
  void sendMessage_withStubProvider_returnsContractCompatibleResponseAndAudits() throws Exception {
    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_COLABORADOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conversationId").value(CONVERSATION_ID_STRING))
        .andExpect(jsonPath("$.message.role").value("assistant"))
        .andExpect(jsonPath("$.message.content", containsString("orientativa")))
        .andExpect(jsonPath("$.message.createdAt").exists());

    assertThat(auditoriaUsoIaRepository.count()).isEqualTo(1);
    AuditoriaUsoIa auditoria = auditoriaUsoIaRepository.findAll().getFirst();
    assertThat(auditoria.getSubjectOidc()).isEqualTo(SUBJECT_COLABORADOR);
    assertThat(auditoria.getTipoUsoIa()).isEqualTo(ChatMessageService.TIPO_USO_IA);
    assertThat(auditoria.getEjemplarId()).isEqualTo(TREE_ID);
    assertThat(auditoria.getPrompt()).contains("MyTreeLibrary");
    assertThat(auditoria.getPrompt()).contains("user: ¿Qué datos necesito");
    assertThat(auditoria.getResultadoResumen()).isEqualTo(chatResultSummary("stub:chat:turns=1"));
    assertThat(auditoria.getConsultadoEn()).isNotNull();
  }

  @Test
  void sendMessage_withAssistantAsLastTurn_returns400() throws Exception {
    mockMvc
        .perform(
            post(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_COLABORADOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHAT_REQUEST_BODY_ASSISTANT_LAST_TURN))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Petición inválida"));

    assertThat(auditoriaUsoIaRepository.count()).isZero();
  }
}
