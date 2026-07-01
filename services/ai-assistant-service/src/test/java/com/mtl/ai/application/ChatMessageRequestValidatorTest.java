package com.mtl.ai.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mtl.ai.dto.AiChatMessageRequest;
import com.mtl.ai.dto.AiChatTurn;
import com.mtl.ai.dto.ChatRole;
import com.mtl.ai.exception.AiAssistantException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ChatMessageRequestValidatorTest {

  private final ChatMessageRequestValidator validator = new ChatMessageRequestValidator();

  @Test
  void validateThread_acceptsUserAsLastTurn() {
    AiChatMessageRequest request =
        new AiChatMessageRequest(
            UUID.randomUUID(),
            10L,
            List.of(
                new AiChatTurn(ChatRole.user, "Hola"),
                new AiChatTurn(ChatRole.assistant, "Respuesta"),
                new AiChatTurn(ChatRole.user, "  Gracias  ")));

    assertThatCode(() -> validator.validateThread(request)).doesNotThrowAnyException();
  }

  @Test
  void validateThread_rejectsAssistantAsLastTurn() {
    AiChatMessageRequest request =
        new AiChatMessageRequest(
            UUID.randomUUID(),
            10L,
            List.of(
                new AiChatTurn(ChatRole.user, "Hola"),
                new AiChatTurn(ChatRole.assistant, "Respuesta")));

    assertThatThrownBy(() -> validator.validateThread(request))
        .isInstanceOf(AiAssistantException.class)
        .extracting(ex -> ((AiAssistantException) ex).getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void validateThread_rejectsBlankLastUserTurn() {
    AiChatMessageRequest request =
        new AiChatMessageRequest(
            UUID.randomUUID(), 10L, List.of(new AiChatTurn(ChatRole.user, "   ")));

    assertThatThrownBy(() -> validator.validateThread(request))
        .isInstanceOf(AiAssistantException.class)
        .extracting(ex -> ((AiAssistantException) ex).getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void validateThread_rejectsNonV4ConversationId() {
    AiChatMessageRequest request =
        new AiChatMessageRequest(
            UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"),
            10L,
            List.of(new AiChatTurn(ChatRole.user, "Hola")));

    assertThatThrownBy(() -> validator.validateThread(request))
        .isInstanceOf(AiAssistantException.class)
        .extracting(ex -> ((AiAssistantException) ex).getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
