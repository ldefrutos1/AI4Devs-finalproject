package com.mtl.ai.infrastructure.client.openai;

import com.mtl.ai.application.AiResponseErrorMessages;
import com.mtl.ai.config.OpenAiProperties;
import com.mtl.ai.exception.AiAssistantException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Cliente HTTP hacia la OpenAI Responses API ({@code POST /v1/responses}). Reutilizable por otros
 * casos de uso (visión, chat) en el mismo servicio.
 */
@Component
@ConditionalOnProperty(prefix = "mtl.ai.provider", name = "mode", havingValue = "openai")
public class OpenAiResponsesClient {

  private static final Logger log = LoggerFactory.getLogger(OpenAiResponsesClient.class);
  private static final String RESPONSES_PATH = "/v1/responses";

  private final RestClient restClient;
  private final OpenAiProperties properties;
  private final OpenAiResponseParser responseParser;
  private final ObjectMapper objectMapper;

  public OpenAiResponsesClient(
      RestClient openAiRestClient,
      OpenAiProperties properties,
      OpenAiResponseParser responseParser,
      ObjectMapper objectMapper) {
    this.restClient = openAiRestClient;
    this.properties = properties;
    this.responseParser = responseParser;
    this.objectMapper = objectMapper;
  }

  /** Invoca OpenAI pidiendo salida JSON (json_object) y devuelve el texto extraído. */
  public String createJsonObjectResponse(String model, String input) {
    return invokeResponses(OpenAiResponsesRequest.jsonObjectMode(model, input), model);
  }

  /** Invoca OpenAI en modo chat (system prompt + hilo) y devuelve texto plano. */
  public String createTextResponse(
      String model, String instructions, List<OpenAiResponsesRequest.InputMessage> messages) {
    return invokeResponses(
        OpenAiResponsesRequest.textChatMode(model, instructions, messages), model);
  }

  private String invokeResponses(OpenAiResponsesRequest request, String model) {
    int maxAttempts = Math.max(1, properties.retry().maxAttempts());
    RestClientException lastTransient = null;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      long startedAt = System.nanoTime();
      try {
        JsonNode body = executeOnce(request);
        String outputText = responseParser.extractOutputText(body);
        logCompletion(model, startedAt, body);
        return outputText;
      } catch (AiAssistantException ex) {
        throw ex;
      } catch (RestClientResponseException ex) {
        if (isTransientHttpStatus(ex.getStatusCode().value()) && attempt < maxAttempts) {
          lastTransient = ex;
          sleepBeforeRetry(attempt);
          continue;
        }
        throw mapHttpError(ex);
      } catch (RestClientException ex) {
        if (isTransientConnectionIssue(ex) && attempt < maxAttempts) {
          lastTransient = ex;
          sleepBeforeRetry(attempt);
          continue;
        }
        throw new AiAssistantException(
            HttpStatus.BAD_GATEWAY,
            AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
            "No se pudo completar la consulta contra el proveedor de IA.",
            ex);
      }
    }

    throw new AiAssistantException(
        HttpStatus.BAD_GATEWAY,
        AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
        "No se pudo completar la consulta contra el proveedor de IA.",
        lastTransient);
  }

  private JsonNode executeOnce(OpenAiResponsesRequest request) {
    String responseBody =
        restClient
            .post()
            .uri(RESPONSES_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
            .body(request)
            .retrieve()
            .body(String.class);

    if (responseBody == null || responseBody.isBlank()) {
      throw new AiAssistantException(
          HttpStatus.NOT_FOUND,
          AiResponseErrorMessages.TITLE_NOT_FOUND,
          AiResponseErrorMessages.DETAIL_NOT_FOUND);
    }
    try {
      return objectMapper.readTree(responseBody);
    } catch (Exception ex) {
      throw new AiAssistantException(
          HttpStatus.BAD_GATEWAY,
          AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
          "OpenAI devolvió una respuesta no interpretable.",
          ex);
    }
  }

  private void logCompletion(String model, long startedAtNanos, JsonNode body) {
    long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
    JsonNode usage = body.get("usage");
    Integer inputTokens = readInt(usage, "input_tokens");
    Integer outputTokens = readInt(usage, "output_tokens");
    log.info(
        "OpenAI response completed model={} durationMs={} inputTokens={} outputTokens={}",
        model,
        durationMs,
        inputTokens,
        outputTokens);
  }

  private static Integer readInt(JsonNode parent, String field) {
    if (parent == null || !parent.hasNonNull(field)) {
      return null;
    }
    return parent.get(field).asInt();
  }

  private AiAssistantException mapHttpError(RestClientResponseException ex) {
    int status = ex.getStatusCode().value();
    if (status == 404) {
      return new AiAssistantException(
          HttpStatus.NOT_FOUND,
          AiResponseErrorMessages.TITLE_NOT_FOUND,
          AiResponseErrorMessages.DETAIL_NOT_FOUND);
    }
    if (status >= 400 && status < 500) {
      log.warn("OpenAI devolvió error de cliente HTTP {}", status);
      return new AiAssistantException(
          HttpStatus.BAD_GATEWAY,
          AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
          "No se pudo completar la consulta contra el proveedor de IA.");
    }
    log.warn("OpenAI devolvió error HTTP {}", status);
    return new AiAssistantException(
        HttpStatus.BAD_GATEWAY,
        AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
        "No se pudo completar la consulta contra el proveedor de IA.");
  }

  private static boolean isTransientHttpStatus(int status) {
    return status == 429 || status == 502 || status == 503;
  }

  private static boolean isTransientConnectionIssue(RestClientException ex) {
    Throwable current = ex;
    while (current != null) {
      if (current instanceof SocketTimeoutException || current instanceof IOException) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  private void sleepBeforeRetry(int attempt) {
    long backoffMs =
        Math.min(
            properties.retry().maxBackoff().toMillis(),
            properties.retry().initialBackoff().toMillis() * (1L << Math.max(0, attempt - 1)));
    log.warn("Reintentando llamada OpenAI tras error transitorio; backoffMs={}", backoffMs);
    try {
      Thread.sleep(backoffMs);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new AiAssistantException(
          HttpStatus.BAD_GATEWAY,
          AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
          "No se pudo completar la consulta contra el proveedor de IA.",
          interrupted);
    }
  }
}
