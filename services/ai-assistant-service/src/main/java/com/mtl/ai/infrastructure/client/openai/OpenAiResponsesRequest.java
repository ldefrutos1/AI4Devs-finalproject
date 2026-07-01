package com.mtl.ai.infrastructure.client.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Cuerpo de {@code POST /v1/responses} (modo JSON o chat conversacional). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiResponsesRequest(
    String model, Object input, String instructions, TextConfig text) {

  public record TextConfig(Format format) {}

  public record Format(String type) {}

  public record InputMessage(String role, String content) {}

  public static OpenAiResponsesRequest jsonObjectMode(String model, String input) {
    return new OpenAiResponsesRequest(
        model, input, null, new TextConfig(new Format("json_object")));
  }

  public static OpenAiResponsesRequest textChatMode(
      String model, String instructions, List<InputMessage> messages) {
    return new OpenAiResponsesRequest(model, messages, instructions, null);
  }
}
