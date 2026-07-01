package com.mtl.ai.application;

import com.mtl.ai.dto.AiChatMessageRequest;
import com.mtl.ai.dto.AiChatTurn;
import com.mtl.ai.dto.ChatRole;
import com.mtl.ai.exception.AiAssistantException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageRequestValidator {

  private static final String TITLE_BAD_REQUEST = "Petición inválida";

  public void validateThread(AiChatMessageRequest request) {
    if (request.conversationId().version() != 4) {
      throw new AiAssistantException(
          HttpStatus.BAD_REQUEST,
          TITLE_BAD_REQUEST,
          "conversationId: debe ser un UUID v4 válido.");
    }
    AiChatTurn last = request.messages().getLast();
    if (last.role() != ChatRole.user) {
      throw new AiAssistantException(
          HttpStatus.BAD_REQUEST,
          TITLE_BAD_REQUEST,
          "messages: el último turno debe ser role user.");
    }
    if (last.content().trim().isEmpty()) {
      throw new AiAssistantException(
          HttpStatus.BAD_REQUEST,
          TITLE_BAD_REQUEST,
          "messages: el último turno user no puede estar vacío.");
    }
  }
}
