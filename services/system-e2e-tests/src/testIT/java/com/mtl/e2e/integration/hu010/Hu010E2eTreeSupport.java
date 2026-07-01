package com.mtl.e2e.integration.hu010;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtl.e2e.support.E2eGatewayHttpClient;
import com.mtl.e2e.support.E2eTokens;
import java.net.http.HttpResponse;
import tools.jackson.databind.JsonNode;

/**
 * Alta/borrado de ejemplar borrador vía gateway para obtener un {@code treeId} válido en HU-010 (§2.1:
 * {@code treeId} obligatorio desde {@code EditTreeView}).
 */
final class Hu010E2eTreeSupport {

  private Hu010E2eTreeSupport() {}

  static long createDraftTreeForChat() throws Exception {
    String token = E2eTokens.collaboratorToken();
    long speciesId = firstMasterId("/api/catalog/species?page=0&size=1", "species");
    long provinceId = firstMasterId("/api/catalog/provinces?page=0&size=1", "provinces");
    String municipality = "E2E-HU010-" + System.currentTimeMillis();
    String body =
        ("{\"speciesId\":%d,\"provinceId\":%d,\"latitude\":40.4168,\"longitude\":-3.7038,"
                + "\"municipality\":\"%s\",\"publicationState\":\"BORRADOR\","
                + "\"publicMapVisibility\":\"PRIVADO\"}")
            .formatted(speciesId, provinceId, municipality);

    HttpResponse<String> created =
        E2eGatewayHttpClient.post("/api/catalog/trees", body, token);
    assertThat(created.statusCode())
        .as("alta de ejemplar para HU-010 -> 201; body: %s", created.body())
        .isEqualTo(201);
    long treeId = E2eGatewayHttpClient.parse(created.body()).path("treeId").asLong();
    assertThat(treeId).as("treeId del ejemplar de prueba").isPositive();
    return treeId;
  }

  static void safeDeleteTree(long treeId) {
    try {
      E2eGatewayHttpClient.delete("/api/catalog/trees/" + treeId, E2eTokens.collaboratorToken());
    } catch (Exception ignored) {
      // best-effort
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
}
