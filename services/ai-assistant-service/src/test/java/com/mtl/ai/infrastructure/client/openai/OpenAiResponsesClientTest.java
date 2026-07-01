package com.mtl.ai.infrastructure.client.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.mtl.ai.config.OpenAiProperties;
import com.mtl.ai.exception.AiAssistantException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

class OpenAiResponsesClientTest {

  private MockRestServiceServer server;
  private OpenAiResponsesClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    RestClient restClient = builder.baseUrl("https://api.openai.com").build();
    OpenAiProperties properties =
        new OpenAiProperties(
            "test-api-key",
            "https://api.openai.com",
            "gpt-4.1-mini",
            "gpt-4.1-mini",
            Duration.ofSeconds(2),
            Duration.ofSeconds(5),
            new OpenAiProperties.Retry(2, Duration.ofMillis(10), Duration.ofMillis(50)));
    client =
        new OpenAiResponsesClient(
            restClient, properties, new OpenAiResponseParser(), JsonMapper.builder().build());
  }

  @AfterEach
  void verifyServer() {
    server.verify();
  }

  @Test
  void createJsonObjectResponse_returnsExtractedJson() {
    server
        .expect(requestTo("https://api.openai.com/v1/responses"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
        .andRespond(
            withSuccess(
                """
                {
                  "status": "completed",
                  "output": [
                    {
                      "type": "message",
                      "content": [
                        {
                          "type": "output_text",
                          "text": "{\\"synonyms\\":[\\"Encina\\"]}"
                        }
                      ]
                    }
                  ],
                  "usage": {
                    "input_tokens": 12,
                    "output_tokens": 34
                  }
                }
                """,
                MediaType.APPLICATION_JSON));

    String json = client.createJsonObjectResponse("gpt-4.1-mini", "prompt json");

    assertThat(json).isEqualTo("{\"synonyms\":[\"Encina\"]}");
  }

  @Test
  void createJsonObjectResponse_retriesTransient503() {
    server
        .expect(requestTo("https://api.openai.com/v1/responses"))
        .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
    server
        .expect(requestTo("https://api.openai.com/v1/responses"))
        .andRespond(
            withSuccess(
                """
                {
                  "status": "completed",
                  "output_text": "{\\"synonyms\\":[]}"
                }
                """,
                MediaType.APPLICATION_JSON));

    String json = client.createJsonObjectResponse("gpt-4.1-mini", "prompt json");

    assertThat(json).isEqualTo("{\"synonyms\":[]}");
  }

  @Test
  void createJsonObjectResponse_mapsClient401ToBadGateway() {
    server
        .expect(requestTo("https://api.openai.com/v1/responses"))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    assertThatThrownBy(() -> client.createJsonObjectResponse("gpt-4.1-mini", "prompt json"))
        .isInstanceOf(AiAssistantException.class)
        .hasMessageContaining("proveedor");
  }

  @Test
  void createTextResponse_returnsExtractedText() {
    server
        .expect(requestTo("https://api.openai.com/v1/responses"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
        .andRespond(
            withSuccess(
                """
                {
                  "status": "completed",
                  "output": [
                    {
                      "type": "message",
                      "content": [
                        {
                          "type": "output_text",
                          "text": "Respuesta orientativa del asistente."
                        }
                      ]
                    }
                  ]
                }
                """,
                MediaType.APPLICATION_JSON));

    String text =
        client.createTextResponse(
            "gpt-4.1-mini",
            "system prompt",
            List.of(new OpenAiResponsesRequest.InputMessage("user", "Hola")));

    assertThat(text).isEqualTo("Respuesta orientativa del asistente.");
  }
}
