package com.mtl.ai.infrastructure.client.openai;

/** Cuerpo de `POST /v1/responses` con salida JSON (modo json_object). */
public record OpenAiResponsesRequest(String model, String input, TextConfig text) {

  public record TextConfig(Format format) {}

  public record Format(String type) {}

  public static OpenAiResponsesRequest jsonObjectMode(String model, String input) {
    return new OpenAiResponsesRequest(model, input, new TextConfig(new Format("json_object")));
  }
}
