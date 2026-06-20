package com.mtl.ai.infrastructure.client.openai;

import com.mtl.ai.application.AiResponseErrorMessages;
import com.mtl.ai.exception.AiAssistantException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** Extrae texto utilizable del cuerpo de una Response de OpenAI. */
@Component
public class OpenAiResponseParser {

  public String extractOutputText(JsonNode root) {
    if (root == null || root.isNull()) {
      throw emptyResult();
    }

    JsonNode outputText = root.get("output_text");
    if (outputText != null && outputText.isTextual() && !outputText.asText().isBlank()) {
      return outputText.asText().trim();
    }

    JsonNode status = root.get("status");
    if (status != null && status.isTextual() && !"completed".equals(status.asText())) {
      JsonNode error = root.get("error");
      if (error != null && error.hasNonNull("message")) {
        throw new AiAssistantException(
            HttpStatus.BAD_GATEWAY,
            AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
            "OpenAI no completó la respuesta solicitada.");
      }
      throw emptyResult();
    }

    JsonNode output = root.get("output");
    if (output == null || !output.isArray()) {
      throw emptyResult();
    }

    StringBuilder builder = new StringBuilder();
    for (JsonNode item : output) {
      appendMessageText(item, builder);
    }

    String text = builder.toString().trim();
    if (text.isBlank()) {
      throw emptyResult();
    }
    return text;
  }

  private static void appendMessageText(JsonNode item, StringBuilder builder) {
    if (item == null || !item.isObject()) {
      return;
    }
    JsonNode content = item.get("content");
    if (content == null || !content.isArray()) {
      return;
    }
    for (JsonNode part : content) {
      if (part != null
          && part.hasNonNull("type")
          && "output_text".equals(part.get("type").asText())
          && part.hasNonNull("text")) {
        builder.append(part.get("text").asText());
      }
    }
  }

  private static AiAssistantException emptyResult() {
    return new AiAssistantException(
        HttpStatus.NOT_FOUND,
        AiResponseErrorMessages.TITLE_NOT_FOUND,
        AiResponseErrorMessages.DETAIL_NOT_FOUND);
  }
}
