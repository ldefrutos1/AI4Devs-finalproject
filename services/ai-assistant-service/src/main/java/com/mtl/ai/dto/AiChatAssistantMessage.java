package com.mtl.ai.dto;

import java.time.OffsetDateTime;

public record AiChatAssistantMessage(
    ChatRole role, String content, OffsetDateTime createdAt) {

  public AiChatAssistantMessage {
    if (role != ChatRole.assistant) {
      throw new IllegalArgumentException("role must be assistant");
    }
  }

  public static AiChatAssistantMessage create(String content, OffsetDateTime createdAt) {
    return new AiChatAssistantMessage(ChatRole.assistant, content, createdAt);
  }
}
