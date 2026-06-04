package com.mtl.e2e.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtl.e2e.support.E2eCollaboratorTokenSupport;
import com.mtl.e2e.support.E2eGatewayHttpClient;
import com.mtl.e2e.support.E2eTokens;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import tools.jackson.databind.JsonNode;

/**
 * Ciclo de vida de un ejemplar por el API Gateway con JWT real de COLABORADOR, sin navegador:
 * alta ({@code POST}) -> consulta en "mis arboles" ({@code GET}) -> borrado ({@code DELETE}) ->
 * verificacion de ausencia.
 *
 * <p>Complementa al E2E de UI (Playwright, carpeta {@code e2e/}): este cubre el contrato del
 * backend del flujo de alta/borrado para no depender de la capa de front, mas volatil. Reparto
 * back vs UI: {@code docs/engineering/testing-java.md} §2.1.1.
 *
 * <p>Requiere stack arriba (perfil {@code dev}): {@code api-gateway} (8080), {@code catalog-service}
 * (8081), {@code media-service} (8082; el {@code DELETE} llama a media en cascada), Keycloak y
 * PostgreSQL con semilla de maestros. Sin token (ni {@code MTL_E2E_AUTO_KEYCLOAK_TOKEN}) queda
 * deshabilitado.
 */
@Tag("e2e")
@EnabledIf("com.mtl.e2e.support.E2eTokens#canRunGatewayE2eTests")
class EjemplarLifecycleGatewayE2EIT extends E2eCollaboratorTokenSupport {

  private static final String TREES_PAGE = "/api/catalog/trees?page=0&size=100";

  @Test
  void altaConsultaYBorradoDeEjemplarViaGateway() throws Exception {
    String token = E2eTokens.collaboratorToken();

    long speciesId = firstMasterId("/api/catalog/species?page=0&size=1", "species");
    long provinceId = firstMasterId("/api/catalog/provinces?page=0&size=1", "provinces");

    // Municipio unico para identificar de forma inequivoca el ejemplar creado.
    String municipality = "E2E-" + System.currentTimeMillis();
    String body =
        ("{\"speciesId\":%d,\"provinceId\":%d,\"latitude\":40.4168,\"longitude\":-3.7038,"
                + "\"municipality\":\"%s\",\"publicationState\":\"BORRADOR\","
                + "\"publicMapVisibility\":\"PRIVADO\"}")
            .formatted(speciesId, provinceId, municipality);

    // 1. Alta -> 201 con treeId.
    HttpResponse<String> created = E2eGatewayHttpClient.post("/api/catalog/trees", body, token);
    assertThat(created.statusCode())
        .as("alta de ejemplar -> 201; body: %s", created.body())
        .isEqualTo(201);
    long treeId = E2eGatewayHttpClient.parse(created.body()).path("treeId").asLong();
    assertThat(treeId).as("treeId del ejemplar creado").isPositive();

    try {
      // 2. Consulta "mis arboles": el creado aparece (orden por defecto modificado_en,desc).
      JsonNode page = E2eGatewayHttpClient.getJson(TREES_PAGE, token);
      assertThat(containsTree(page, treeId, municipality))
          .as("el ejemplar %d (%s) debe aparecer en mis arboles", treeId, municipality)
          .isTrue();

      // 3. Borrado -> 204 (incluye cascada a media-service).
      HttpResponse<String> deleted =
          E2eGatewayHttpClient.delete("/api/catalog/trees/" + treeId, token);
      assertThat(deleted.statusCode())
          .as("borrado de ejemplar -> 204; body: %s", deleted.body())
          .isEqualTo(204);

      // 4. Ya no aparece en el listado del colaborador.
      JsonNode after = E2eGatewayHttpClient.getJson(TREES_PAGE, token);
      assertThat(containsTree(after, treeId, null))
          .as("el ejemplar %d no debe seguir en mis arboles tras el borrado", treeId)
          .isFalse();
    } catch (Exception | AssertionError failure) {
      // Limpieza best-effort si algo falla tras el alta; no enmascara el fallo original.
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

  /** {@code true} si el listado contiene {@code treeId}; si {@code municipality} no es nulo, exige que coincida. */
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
