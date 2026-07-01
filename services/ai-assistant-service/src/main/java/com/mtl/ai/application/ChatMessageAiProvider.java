package com.mtl.ai.application;

import com.mtl.ai.dto.AiChatTurn;
import java.util.List;

/** Puerto hacia el proveedor externo de IA para chat conversacional (HU-010). */
public interface ChatMessageAiProvider {

  ProviderResponse requestChat(String systemPrompt, List<AiChatTurn> messages);

  record ProviderResponse(String content, String providerSummary) {}
}
