package com.mtl.e2e.integration.hu005hu008;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtl.e2e.support.E2eCollaboratorTokenSupport;
import com.mtl.e2e.support.E2eGatewayHttpClient;
import com.mtl.e2e.support.E2eTokens;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import tools.jackson.databind.JsonNode;

/**
 * Flujo vertical colaborador vía gateway (sin navegador): alta → listado → baja → ausencia.
 *
 * <ul>
 *   <li>HU-005 TASK-005-06: {@code POST /api/catalog/trees}
 *   <li>HU-008 TASK-008-02: {@code GET /api/catalog/trees} (mis árboles)
 *   <li>HU-008 TASK-008-07: {@code DELETE /api/catalog/trees/{treeId}} (cascada media)
 * </ul>
 *
 * <p>Complementa el E2E UI (Playwright {@code e2e/tests/alta-ejemplar.spec.ts}). Reparto back vs UI:
 * {@code docs/engineering/testing-java.md} §2.1.1.
 *
 * <p>Requiere {@code api-gateway} (8080), {@code catalog-service} (8081), {@code media-service}
 * (8082), Keycloak y PostgreSQL con semilla de maestros.
 */
@Tag("e2e")
@Tag("hu005hu008")
@Tag("hu005")
@Tag("hu005-t06")
@Tag("hu008")
@Tag("hu008-t02")
@Tag("hu008-t07")
@EnabledIf("com.mtl.e2e.support.E2eTokens#canRunGatewayE2eTests")
class Hu005Hu008CollaboratorTreeLifecycleGatewayE2EIT extends E2eCollaboratorTokenSupport {

  private static final String TREES_PAGE = "/api/catalog/trees?page=0&size=100";

  @Test
  @DisplayName("TASK-005-06 + 008-02/07: alta, listado y borrado de ejemplar vía gateway")
  void createListAndDeleteTree_withCollaboratorToken_viaGateway() throws Exception {
    String token = E2eTokens.collaboratorToken();

    long speciesId = firstMasterId("/api/catalog/species?page=0&size=1", "species");
    long provinceId = firstMasterId("/api/catalog/provinces?page=0&size=1", "provinces");

    String municipality = "E2E-" + System.currentTimeMillis();
    String body =
        ("{\"speciesId\":%d,\"provinceId\":%d,\"latitude\":40.4168,\"longitude\":-3.7038,"
                + "\"municipality\":\"%s\",\"publicationState\":\"BORRADOR\","
                + "\"publicMapVisibility\":\"PRIVADO\"}")
            .formatted(speciesId, provinceId, municipality);

    HttpResponse<String> created = E2eGatewayHttpClient.post("/api/catalog/trees", body, token);
    assertThat(created.statusCode())
        .as("alta de ejemplar -> 201; body: %s", created.body())
        .isEqualTo(201);
    long treeId = E2eGatewayHttpClient.parse(created.body()).path("treeId").asLong();
    assertThat(treeId).as("treeId del ejemplar creado").isPositive();

    try {
      JsonNode page = E2eGatewayHttpClient.getJson(TREES_PAGE, token);
      assertThat(containsTree(page, treeId, municipality))
          .as("el ejemplar %d (%s) debe aparecer en mis arboles", treeId, municipality)
          .isTrue();

      HttpResponse<String> deleted =
          E2eGatewayHttpClient.delete("/api/catalog/trees/" + treeId, token);
      assertThat(deleted.statusCode())
          .as("borrado de ejemplar -> 204; body: %s", deleted.body())
          .isEqualTo(204);

      JsonNode after = E2eGatewayHttpClient.getJson(TREES_PAGE, token);
      assertThat(containsTree(after, treeId, null))
          .as("el ejemplar %d no debe seguir en mis arboles tras el borrado", treeId)
          .isFalse();
    } catch (Exception | AssertionError failure) {
      safeDelete(treeId, token);
      throw failure;
    }
  }

  private static long firstMasterId(String pathAndQuery, String contexto) throws Exception {
    JsonNode content =
        E2eGatewayHttpClient.getJson(pathAndQuery, E2eTokens.collaboratorToken()).path("content");
    assertThat(content.isArray() && !content.isEmpty())
        .as("%s: se necesita al menos un maestro sembrado", contexto)
        .isTrue();
    return content.get(0).path("id").asLong();
  }

  private static boolean containsTree(JsonNode page, long treeId, String municipality) {
    JsonNode content = page.path("content");
    if (!content.isArray()) {
      return false;
    }
    for (JsonNode item : content) {
      if (item.path("treeId").asLong() == treeId) {
        return municipality == null || municipality.equals(item.path("municipality").asString(""));
      }
    }
    return false;
  }

  private static void safeDelete(long treeId, String token) {
    try {
      E2eGatewayHttpClient.delete("/api/catalog/trees/" + treeId, token);
    } catch (Exception ignored) {
      // best-effort
    }
  }
}
