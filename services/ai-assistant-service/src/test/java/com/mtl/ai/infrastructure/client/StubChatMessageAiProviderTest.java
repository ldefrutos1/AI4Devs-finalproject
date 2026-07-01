package com.mtl.ai.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtl.ai.dto.AiChatTurn;
import com.mtl.ai.dto.ChatRole;
import java.util.List;
import org.junit.jupiter.api.Test;

class StubChatMessageAiProviderTest {

  private final StubChatMessageAiProvider provider = new StubChatMessageAiProvider();

  @Test
  void requestChat_returnsOrientativeStubUsingLastUserMessage() {
    var response =
        provider.requestChat(
            "system",
            List.of(
                new AiChatTurn(ChatRole.user, "¿Cómo registro coordenadas?"),
                new AiChatTurn(ChatRole.assistant, "Anterior"),
                new AiChatTurn(ChatRole.user, "¿Y la especie?")));

    assertThat(response.content()).contains("orientativa");
    assertThat(response.content()).doesNotContain("stub local");
    assertThat(response.content()).doesNotContain("recibido tu consulta");
    assertThat(response.providerSummary()).isEqualTo("stub:chat:turns=3");
  }
}
