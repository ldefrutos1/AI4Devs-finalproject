package com.mtl.ai.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtl.ai.dto.AiChatTurn;
import com.mtl.ai.dto.ChatRole;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatMessageAuditTextTest {

  @Test
  void buildAuditPrompt_includesSystemAndThread() {
    String prompt =
        ChatMessageService.buildAuditPrompt(
            "System prompt",
            List.of(
                new AiChatTurn(ChatRole.user, "Hola"),
                new AiChatTurn(ChatRole.assistant, "Respuesta")));

    assertThat(prompt).contains("[System]").contains("System prompt");
    assertThat(prompt).contains("user: Hola").contains("assistant: Respuesta");
  }

  @Test
  void buildResultSummary_appendsConversationId() {
    UUID conversationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    assertThat(ChatMessageService.buildResultSummary("stub:chat", conversationId))
        .isEqualTo("stub:chat;conversationId=" + conversationId);
  }
}
