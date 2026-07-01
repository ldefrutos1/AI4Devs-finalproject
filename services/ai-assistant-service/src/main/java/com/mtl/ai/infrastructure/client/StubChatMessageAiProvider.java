package com.mtl.ai.infrastructure.client;

import com.mtl.ai.application.ChatMessageAiProvider;
import com.mtl.ai.dto.AiChatTurn;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Implementación local sin dependencia externa, útil para desarrollo y tests (HU-010). */
@Component
@ConditionalOnProperty(
    prefix = "mtl.ai.provider",
    name = "mode",
    havingValue = "stub",
    matchIfMissing = true)
public class StubChatMessageAiProvider implements ChatMessageAiProvider {

  @Override
  public ProviderResponse requestChat(String systemPrompt, List<AiChatTurn> messages) {
    String reply =
        """
        Esta ayuda es orientativa y no sustituye el criterio de un experto ni una identificación definitiva. \
        No tengo acceso a los datos concretos de la ficha que estás editando.
        """
            .trim();
    return new ProviderResponse(reply, "stub:chat:turns=%d".formatted(messages.size()));
  }
}
