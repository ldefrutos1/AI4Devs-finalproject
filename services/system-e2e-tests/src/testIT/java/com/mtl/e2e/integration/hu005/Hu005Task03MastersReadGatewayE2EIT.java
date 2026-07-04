package com.mtl.e2e.integration.hu005;

import static com.mtl.e2e.support.E2ePagedJsonAssertions.assertNonEmptyMasterPage;

import com.mtl.e2e.support.E2eCollaboratorTokenSupport;
import com.mtl.e2e.support.E2eGatewayHttpClient;
import com.mtl.e2e.support.E2eTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import tools.jackson.databind.JsonNode;

/** HU-005 — TASK-HU-005-03: lectura de maestros (species/provinces) vía gateway con paginación y {@code q}. */
@Tag("e2e")
@Tag("hu005")
@Tag("hu005-t03")
@EnabledIf("com.mtl.e2e.support.E2eTokens#canRunGatewayE2eTests")
class Hu005Task03MastersReadGatewayE2EIT extends E2eCollaboratorTokenSupport {

  @Test
  @DisplayName("TASK-005-03: GET species paginado vía gateway")
  void listSpecies_withCollaboratorToken_viaGateway_returnsPagedMasterData() throws Exception {
    assertNonEmptyMasterPage(
        getJson("/api/catalog/species?page=0&size=5"), "species sin filtro");
  }

  @Test
  @DisplayName("TASK-005-03: GET species con q=cina (unaccent) vía gateway")
  void listSpecies_withQuery_viaGateway_returnsAtLeastOneRow() throws Exception {
    assertNonEmptyMasterPage(
        getJson("/api/catalog/species?page=0&size=5&q=cina"), "species con q=cina");
  }

  @Test
  @DisplayName("TASK-005-03: GET provinces paginado vía gateway")
  void listProvinces_withCollaboratorToken_viaGateway_returnsPagedMasterData() throws Exception {
    assertNonEmptyMasterPage(
        getJson("/api/catalog/provinces?page=0&size=5"), "provinces sin filtro");
  }

  @Test
  @DisplayName("TASK-005-03: GET provinces con q=01 vía gateway")
  void listProvinces_withQuery_viaGateway_returnsAtLeastOneRow() throws Exception {
    assertNonEmptyMasterPage(
        getJson("/api/catalog/provinces?page=0&size=5&q=01"), "provinces con q=01");
  }

  private static JsonNode getJson(String pathAndQuery) throws Exception {
    return E2eGatewayHttpClient.getJson(pathAndQuery, E2eTokens.collaboratorToken());
  }
}
