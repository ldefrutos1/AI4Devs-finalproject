package com.mtl.ai.application;

import com.mtl.ai.domain.AuditoriaUsoIa;
import com.mtl.ai.dto.AiChatAssistantMessage;
import com.mtl.ai.dto.AiChatMessageRequest;
import com.mtl.ai.dto.AiChatMessageResponse;
import com.mtl.ai.dto.AiChatTurn;
import com.mtl.ai.exception.AiAssistantException;
import com.mtl.ai.infrastructure.persistence.jpa.repository.AuditoriaUsoIaRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatMessageService {

  public static final String TIPO_USO_IA = "chat-message";

  private static final int PROMPT_MAX_LENGTH = 8_000;
  private static final int RESULT_SUMMARY_MAX_LENGTH = 4_000;

  private final AiPromptFactory aiPromptFactory;
  private final ChatMessageAiProvider chatMessageAiProvider;
  private final ChatMessageRequestValidator requestValidator;
  private final ChatMessageRateLimiter chatMessageRateLimiter;
  private final AuditoriaUsoIaRepository auditoriaUsoIaRepository;

  public ChatMessageService(
      AiPromptFactory aiPromptFactory,
      ChatMessageAiProvider chatMessageAiProvider,
      ChatMessageRequestValidator requestValidator,
      ChatMessageRateLimiter chatMessageRateLimiter,
      AuditoriaUsoIaRepository auditoriaUsoIaRepository) {
    this.aiPromptFactory = aiPromptFactory;
    this.chatMessageAiProvider = chatMessageAiProvider;
    this.requestValidator = requestValidator;
    this.chatMessageRateLimiter = chatMessageRateLimiter;
    this.auditoriaUsoIaRepository = auditoriaUsoIaRepository;
  }

  @Transactional
  public AiChatMessageResponse process(AiChatMessageRequest request, Jwt jwt) {
    chatMessageRateLimiter.checkAllowed(jwt != null ? jwt.getSubject() : null);
    requestValidator.validateThread(request);
    String systemPrompt = aiPromptFactory.buildChatSystemPrompt();
    ChatMessageAiProvider.ProviderResponse providerResponse =
        chatMessageAiProvider.requestChat(systemPrompt, request.messages());
    String content = providerResponse.content();
    if (content == null || content.isBlank()) {
      throw new AiAssistantException(
          HttpStatus.BAD_GATEWAY,
          AiResponseErrorMessages.TITLE_PROVIDER_UNAVAILABLE,
          "No se pudo completar la consulta contra el proveedor de IA.");
    }
    saveAudit(
        jwt,
        request.treeId(),
        buildAuditPrompt(systemPrompt, request.messages()),
        buildResultSummary(providerResponse.providerSummary(), request.conversationId()));
    OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    return new AiChatMessageResponse(
        request.conversationId(),
        AiChatAssistantMessage.create(content.trim(), createdAt));
  }

  static String buildAuditPrompt(String systemPrompt, List<AiChatTurn> messages) {
    StringBuilder builder = new StringBuilder();
    builder.append("[System]\n").append(systemPrompt.trim()).append("\n\n[Messages]\n");
    for (AiChatTurn turn : messages) {
      builder.append(turn.role().name()).append(": ").append(turn.content().trim()).append('\n');
    }
    return builder.toString().trim();
  }

  static String buildResultSummary(String providerSummary, java.util.UUID conversationId) {
    String summary = providerSummary != null ? providerSummary.trim() : "";
    if (conversationId != null) {
      summary =
          summary.isEmpty()
              ? "conversationId=" + conversationId
              : summary + ";conversationId=" + conversationId;
    }
    return summary;
  }

  private void saveAudit(Jwt jwt, Long treeId, String prompt, String providerSummary) {
    AuditoriaUsoIa auditoria = new AuditoriaUsoIa();
    auditoria.setSubjectOidc(jwt != null ? jwt.getSubject() : "system");
    auditoria.setTipoUsoIa(TIPO_USO_IA);
    auditoria.setEjemplarId(treeId);
    auditoria.setPrompt(trimToLength(prompt, PROMPT_MAX_LENGTH));
    auditoria.setResultadoResumen(trimToLength(providerSummary, RESULT_SUMMARY_MAX_LENGTH));
    auditoria.setConsultadoEn(OffsetDateTime.now());
    auditoriaUsoIaRepository.save(auditoria);
  }

  private static String trimToLength(String value, int maxLength) {
    if (value == null) {
      return "";
    }
    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }
}
