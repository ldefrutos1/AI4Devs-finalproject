package com.mtl.ai.infrastructure.client.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mtl.ai.exception.AiAssistantException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class OpenAiResponseParserTest {

  private final OpenAiResponseParser parser = new OpenAiResponseParser();
  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  @Test
  void extractOutputText_readsMessageOutputText() throws Exception {
    var root =
        jsonMapper.readTree(
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
              ]
            }
            """);

    assertThat(parser.extractOutputText(root)).isEqualTo("{\"synonyms\":[\"Encina\"]}");
  }

  @Test
  void extractOutputText_readsRootOutputTextProperty() throws Exception {
    var root = jsonMapper.readTree("{\"output_text\":\"{\\\"synonyms\\\":[]}\"}");

    assertThat(parser.extractOutputText(root)).isEqualTo("{\"synonyms\":[]}");
  }

  @Test
  void extractOutputText_whenEmpty_throwsNotFound() throws Exception {
    var root = jsonMapper.readTree("{\"status\":\"completed\",\"output\":[]}");

    assertThatThrownBy(() -> parser.extractOutputText(root))
        .isInstanceOf(AiAssistantException.class)
        .hasMessageContaining("utilizable");
  }
}
