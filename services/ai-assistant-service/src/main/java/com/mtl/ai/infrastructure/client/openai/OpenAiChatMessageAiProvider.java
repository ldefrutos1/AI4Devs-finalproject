package com.mtl.ai.infrastructure.client.openai;

import com.mtl.ai.application.AiResponseErrorMessages;
import com.mtl.ai.application.ChatMessageAiProvider;
import com.mtl.ai.config.OpenAiProperties;
import com.mtl.ai.dto.AiChatTurn;
import com.mtl.ai.exception.AiAssistantException;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "mtl.ai.provider", name = "mode", havingValue = "openai")
public class OpenAiChatMessageAiProvider implements ChatMessageAiProvider {

  private final OpenAiResponsesClient openAiResponsesClient;
  private final OpenAiProperties openAiProperties;

  public OpenAiChatMessageAiProvider(
      OpenAiResponsesClient openAiResponsesClient, OpenAiProperties openAiProperties) {
    this.openAiResponsesClient = openAiResponsesClient;
    this.openAiProperties = openAiProperties;
  }

  @Override
  public ProviderResponse requestChat(String systemPrompt, List<AiChatTurn> messages) {
    String model = openAiProperties.chatModel();
    List<OpenAiResponsesRequest.InputMessage> input =
        messages.stream().map(this::toInputMessage).toList();
    try {
      String content = openAiResponsesClient.createTextResponse(model, systemPrompt, input);
      String summary = "openai:%s:turns=%d".formatted(model, messages.size());
      return new ProviderResponse(content, summary);
    } catch (AiAssistantException ex) {
      if (ex.getStatus() == HttpStatus.NOT_FOUND) {
        throw new AiAssistantException(
            HttpStatus.BAD_GATEWAY,
            AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
            "No se pudo completar la consulta contra el proveedor de IA.");
      }
      throw ex;
    }
  }

  private OpenAiResponsesRequest.InputMessage toInputMessage(AiChatTurn turn) {
    return new OpenAiResponsesRequest.InputMessage(turn.role().name(), turn.content());
  }
}
