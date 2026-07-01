package com.mtl.ai.infrastructure.client.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.ai.application.AiResponseErrorMessages;
import com.mtl.ai.config.OpenAiProperties;
import com.mtl.ai.dto.AiChatTurn;
import com.mtl.ai.dto.ChatRole;
import com.mtl.ai.exception.AiAssistantException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class OpenAiChatMessageAiProviderTest {

  @Mock private OpenAiResponsesClient openAiResponsesClient;
  @Mock private OpenAiProperties openAiProperties;

  @InjectMocks private OpenAiChatMessageAiProvider provider;

  @Test
  void requestChat_delegatesToOpenAiClient() {
    when(openAiProperties.chatModel()).thenReturn("gpt-4.1-mini");
    when(openAiResponsesClient.createTextResponse(
            eq("gpt-4.1-mini"), eq("system"), anyList()))
        .thenReturn("Respuesta orientativa.");

    var response =
        provider.requestChat(
            "system", List.of(new AiChatTurn(ChatRole.user, "¿Qué especie es esta?")));

    assertThat(response.content()).isEqualTo("Respuesta orientativa.");
    assertThat(response.providerSummary()).isEqualTo("openai:gpt-4.1-mini:turns=1");
    verify(openAiResponsesClient)
        .createTextResponse(eq("gpt-4.1-mini"), eq("system"), anyList());
  }

  @Test
  void requestChat_mapsProviderNotFoundToBadGateway() {
    when(openAiProperties.chatModel()).thenReturn("gpt-4.1-mini");
    when(openAiResponsesClient.createTextResponse(anyString(), anyString(), anyList()))
        .thenThrow(
            new AiAssistantException(
                HttpStatus.NOT_FOUND,
                AiResponseErrorMessages.TITLE_NOT_FOUND,
                AiResponseErrorMessages.DETAIL_NOT_FOUND));

    assertThatThrownBy(
            () ->
                provider.requestChat(
                    "system", List.of(new AiChatTurn(ChatRole.user, "Hola"))))
        .isInstanceOf(AiAssistantException.class)
        .satisfies(
            ex -> {
              AiAssistantException aiEx = (AiAssistantException) ex;
              assertThat(aiEx.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
              assertThat(aiEx.getTitle())
                  .isEqualTo(AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE);
            });
  }
}
