package com.mtl.e2e.integration.hu001;

import static com.mtl.e2e.integration.hu001.Hu001E2ePaths.SPECIES_PAGE;
import static com.mtl.e2e.integration.hu001.Hu001E2ePaths.SPECIES_PATH;

import com.mtl.e2e.support.E2eGatewayHttpClient;
import com.mtl.e2e.support.E2eProblemExpectations;
import com.mtl.e2e.support.E2eTokens;
import com.mtl.e2e.support.KeycloakE2eAdminSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/** HU-001 — Escenario 3: API protegida sin sesión válida → **401** Problem (vía gateway). */
@Tag("e2e")
@Tag("hu001")
@Tag("hu001-s03")
@EnabledIf("com.mtl.e2e.support.E2eTokens#canRunGatewaySecurityE2eTests")
class Hu001Scenario03UnauthorizedGatewayE2EIT {

  @Test
  @DisplayName("Escenario 3: GET species sin Bearer → 401 No autenticado y correlación")
  void listSpecies_withoutBearer_viaGateway_returns401ProblemWithCorrelation() throws Exception {
    assertUnauthorizedSpecies(null, "hu001-s03-no-bearer");
  }

  @Test
  @DisplayName("Escenario 3: GET species con Bearer inválido → 401 Problem y correlación")
  void listSpecies_withInvalidBearer_viaGateway_returns401ProblemWithCorrelation() throws Exception {
    assertUnauthorizedSpecies(E2eTokens.invalidBearerToken(), "hu001-s03-invalid");
  }

  @Test
  @EnabledIf("com.mtl.e2e.support.E2eTokens#autoKeycloakTokenRequested")
  @DisplayName("Escenario 3: GET species con Bearer expirado → 401 Problem y correlación")
  void listSpecies_withExpiredBearer_viaGateway_returns401ProblemWithCorrelation() throws Exception {
    String expiredToken = KeycloakE2eAdminSupport.fetchExpiredCollaboratorAccessToken();
    assertUnauthorizedSpecies(expiredToken, "hu001-s03-expired");
  }

  private static void assertUnauthorizedSpecies(String bearerToken, String correlationId)
      throws Exception {
    E2eGatewayHttpClient.getProblem(
        SPECIES_PAGE,
        bearerToken,
        correlationId,
        401,
        E2eProblemExpectations.UNAUTHORIZED_TITLE,
        E2eProblemExpectations.UNAUTHORIZED_DETAIL,
        SPECIES_PATH);
  }
}
