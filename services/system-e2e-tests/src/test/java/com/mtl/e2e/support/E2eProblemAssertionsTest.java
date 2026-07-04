package com.mtl.e2e.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Test;

class E2eProblemAssertionsTest {

  @Test
  void assertProblem_validatesDetailAndInstance() throws Exception {
    String body =
        """
        {
          "status": 401,
          "title": "No autenticado",
          "detail": "Se requiere autenticación con un token Bearer válido",
          "instance": "/api/catalog/species"
        }
        """;
    HttpResponse<String> response =
        stubResponse(401, "application/problem+json", body);

    E2eProblemAssertions.assertProblem(
        response,
        401,
        E2eProblemExpectations.UNAUTHORIZED_TITLE,
        E2eProblemExpectations.UNAUTHORIZED_DETAIL,
        "/api/catalog/species");
  }

  @Test
  void assertProblem_rejectsInternalDetailLeak() {
    String body =
        """
        {
          "status": 401,
          "title": "No autenticado",
          "detail": "java.lang.Exception at com.mtl.catalog.Foo.bar"
        }
        """;
    HttpResponse<String> response =
        stubResponse(401, "application/problem+json", body);

    assertThatThrownBy(
            () ->
                E2eProblemAssertions.assertProblem(
                    response,
                    401,
                    E2eProblemExpectations.UNAUTHORIZED_TITLE,
                    "java.lang.Exception at com.mtl.catalog.Foo.bar",
                    null))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void assertProblem_legacyOverloadSkipsDetailAndInstance() throws Exception {
    String body =
        """
        {"status": 403, "title": "Prohibido", "detail": "mensaje genérico"}
        """;
    HttpResponse<String> response =
        stubResponse(403, "application/problem+json", body);

    E2eProblemAssertions.assertProblem(response, 403, E2eProblemExpectations.FORBIDDEN_TITLE);
  }

  private static HttpResponse<String> stubResponse(int status, String contentType, String body) {
    HttpHeaders headers =
        HttpHeaders.of(Map.of("Content-Type", List.of(contentType)), (a, b) -> true);
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return status;
      }

      @Override
      public HttpRequest request() {
        throw new UnsupportedOperationException();
      }

      @Override
      public Optional<HttpResponse<String>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return headers;
      }

      @Override
      public String body() {
        return body;
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return URI.create("http://127.0.0.1:8080/api/catalog/species");
      }

      @Override
      public HttpClient.Version version() {
        return HttpClient.Version.HTTP_1_1;
      }
    };
  }
}
