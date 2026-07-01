package com.mtl.e2e.integration.hu010;

import static com.mtl.e2e.integration.hu010.Hu010E2eFixtures.CONVERSATION_ID;
import static com.mtl.e2e.integration.hu010.Hu010E2eFixtures.chatRequestBody;
import static com.mtl.e2e.integration.hu010.Hu010E2ePaths.CHAT_MESSAGES;
import static org.assertj.core.api.Assertions.assertThat;

import com.mtl.e2e.support.E2eCollaboratorTokenSupport;
import com.mtl.e2e.support.E2eCorrelationAssertions;
import com.mtl.e2e.support.E2eGatewayHttpClient;
import com.mtl.e2e.support.E2eTokens;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import tools.jackson.databind.JsonNode;

/**
 * HU-010 — Escenario 1 (parte Java): colaborador autenticado envía un turno de chat vía gateway con
 * {@code treeId} del ejemplar en edición y recibe respuesta orientativa.
 *
 * <p>Aspectos cubiertos en otros niveles: auditoría {@code AUDITORIA_USO_IA} y ausencia de {@code treeId}
 * en prompt LLM ({@code ChatMessageStubFlowIT}); copy UX orientativa (frontend); metadatos del formulario
 * (frontend).
 */
@Tag("e2e")
@Tag("hu010")
@Tag("hu010-s01")
@EnabledIf("com.mtl.e2e.support.E2eTokens#canRunGatewayE2eTests")
class Hu010Scenario01ChatMessageGatewayE2EIT extends E2eCollaboratorTokenSupport {

  @Test
  @DisplayName(
      "Escenario 1: COLABORADOR POST /api/ai/chat/messages con treeId devuelve 200, respuesta orientativa y correlación")
  void sendMessage_withCollaboratorAndTreeId_viaGateway_returns200AndOrientativeReply() throws Exception {
    long treeId = Hu010E2eTreeSupport.createDraftTreeForChat();
    try {
      String correlationId = "hu010-s01-corr";
      HttpResponse<String> response =
          E2eGatewayHttpClient.post(
              CHAT_MESSAGES,
              chatRequestBody(treeId),
              E2eTokens.collaboratorToken(),
              correlationId);

      assertThat(response.statusCode())
          .as("chat -> 200; body: %s", response.body())
          .isEqualTo(200);
      E2eCorrelationAssertions.assertResponseHeader(response, correlationId);

      JsonNode body = E2eGatewayHttpClient.parse(response.body());
      assertThat(body.path("conversationId").asString()).isEqualTo(CONVERSATION_ID);
      assertThat(body.path("message").path("role").asString()).isEqualTo("assistant");
      assertThat(body.path("message").path("content").asString())
          .as("respuesta orientativa del stub/proveedor")
          .containsIgnoringCase("orientativa");
      assertThat(body.path("message").path("createdAt").asString()).isNotBlank();
    } finally {
      Hu010E2eTreeSupport.safeDeleteTree(treeId);
    }
  }
}
