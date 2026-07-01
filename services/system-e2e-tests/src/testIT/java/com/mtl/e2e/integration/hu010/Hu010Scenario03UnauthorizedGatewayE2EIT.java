package com.mtl.e2e.integration.hu010;

import static com.mtl.e2e.integration.hu010.Hu010E2eFixtures.chatRequestBody;
import static com.mtl.e2e.integration.hu010.Hu010E2ePaths.CHAT_MESSAGES;

import com.mtl.e2e.support.E2eGatewayHttpClient;
import com.mtl.e2e.support.E2eTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * HU-010 — Escenario 3 (parte Java): peticiones al chat sin sesión válida → **401** Problem vía gateway.
 *
 * <p>{@code 403} por rol no autorizado: {@code AiChatControllerWebMvcTest} (sin usuario VISITANTE en
 * Keycloak de dev). No se audita uso de IA en rechazos: {@code ChatMessageStubFlowIT} / servicio.
 */
@Tag("e2e")
@Tag("hu010")
@Tag("hu010-s03")
@EnabledIf("com.mtl.e2e.support.E2eTokens#canRunGatewaySecurityE2eTests")
class Hu010Scenario03UnauthorizedGatewayE2EIT {

  private static final String REQUEST_BODY = chatRequestBody(42L);

  @Test
  @DisplayName("Escenario 3: POST chat sin Bearer → 401 No autenticado y correlación")
  void sendMessage_withoutBearer_viaGateway_returns401ProblemWithCorrelation() throws Exception {
    E2eGatewayHttpClient.postProblem(
        CHAT_MESSAGES, REQUEST_BODY, null, "hu010-s03-no-bearer", 401, "No autenticado");
  }

  @Test
  @DisplayName("Escenario 3: POST chat con Bearer inválido → 401 Problem y correlación")
  void sendMessage_withInvalidBearer_viaGateway_returns401ProblemWithCorrelation() throws Exception {
    E2eGatewayHttpClient.postProblem(
        CHAT_MESSAGES,
        REQUEST_BODY,
        E2eTokens.invalidBearerToken(),
        "hu010-s03-invalid",
        401,
        "No autenticado");
  }
}
