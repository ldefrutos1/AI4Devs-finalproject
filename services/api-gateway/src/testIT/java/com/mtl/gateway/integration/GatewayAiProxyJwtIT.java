package com.mtl.gateway.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mtl.gateway.integration.support.JwtTestTokens;
import com.mtl.gateway.integration.support.LocalRsaReactiveJwtDecoderConfig;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integración: JWT validado con clave RSA local + proxy hacia ai-assistant-service simulado
 * (WireMock), comprobando token relay hacia el upstream en {@code /api/ai/**}.
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LocalRsaReactiveJwtDecoderConfig.class)
class GatewayAiProxyJwtIT {

  private static final String ENRICHMENT_PATH = "/api/ai/species/enrichment-suggestions";
  private static final String ENRICHMENT_REQUEST_BODY =
      "{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}";
  private static final String ENRICHMENT_RESPONSE_BODY = "{\"synonyms\":[\"Encina\"]}";

  private static volatile WireMockServer wireMock;

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @DynamicPropertySource
  static void registerUpstreamUris(DynamicPropertyRegistry registry) {
    if (wireMock == null || !wireMock.isRunning()) {
      wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
      wireMock.start();
    }
    String base = "http://127.0.0.1:" + wireMock.port();
    registry.add("mtl.ai.uri", () -> base);
  }

  @AfterAll
  static void stopWireMock() {
    if (wireMock != null) {
      wireMock.stop();
      wireMock = null;
    }
  }

  @BeforeEach
  void setUp() {
    wireMock.resetAll();
    webTestClient = WebTestClient.bindToServer().baseUrl("http://127.0.0.1:" + port).build();
  }

  @Test
  void protectedAiRoute_withoutBearer_returnsUnauthorized() {
    webTestClient
        .post()
        .uri(ENRICHMENT_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(ENRICHMENT_REQUEST_BODY)
        .exchange()
        .expectStatus()
        .isEqualTo(UNAUTHORIZED);
  }

  @Test
  void protectedAiRoute_withBearer_forwardsTokenAndReturnsUpstreamBody() {
    String token = JwtTestTokens.accessTokenWithRealmRoles("it-admin", List.of("ADMIN"));
    wireMock.stubFor(
        post(urlPathEqualTo(ENRICHMENT_PATH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(ENRICHMENT_RESPONSE_BODY)));

    webTestClient
        .post()
        .uri(ENRICHMENT_PATH)
        .headers(h -> h.setBearerAuth(token))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(ENRICHMENT_REQUEST_BODY)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo(ENRICHMENT_RESPONSE_BODY);

    wireMock.verify(
        1,
        postRequestedFor(urlPathEqualTo(ENRICHMENT_PATH))
            .withHeader(AUTHORIZATION, equalTo("Bearer " + token)));
  }
}
