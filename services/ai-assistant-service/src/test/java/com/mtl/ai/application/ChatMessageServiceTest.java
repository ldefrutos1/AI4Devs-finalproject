package com.mtl.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.ai.domain.AuditoriaUsoIa;
import com.mtl.ai.dto.AiChatMessageRequest;
import com.mtl.ai.dto.AiChatTurn;
import com.mtl.ai.dto.ChatRole;
import com.mtl.ai.exception.AiAssistantException;
import com.mtl.ai.infrastructure.persistence.jpa.repository.AuditoriaUsoIaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

  @Mock private AiPromptFactory aiPromptFactory;
  @Mock private ChatMessageAiProvider chatMessageAiProvider;
  @Mock private ChatMessageRequestValidator requestValidator;
  @Mock private ChatMessageRateLimiter chatMessageRateLimiter;
  @Mock private AuditoriaUsoIaRepository auditoriaUsoIaRepository;

  @InjectMocks private ChatMessageService service;

  private static Jwt testJwt() {
    return Jwt.withTokenValue("test")
        .header("alg", "none")
        .issuer("http://localhost:8180/realms/mtl")
        .subject("collab-sub")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
  }

  @Test
  void process_invokesProviderWithSystemPromptAndMessagesWithoutTreeId() {
    UUID conversationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    AiChatMessageRequest request =
        new AiChatMessageRequest(
            conversationId,
            42L,
            List.of(new AiChatTurn(ChatRole.user, "¿Qué datos necesito para dar de alta un árbol?")));
    String systemPrompt = "system prompt";
    when(aiPromptFactory.buildChatSystemPrompt()).thenReturn(systemPrompt);
    when(chatMessageAiProvider.requestChat(eq(systemPrompt), eq(request.messages())))
        .thenReturn(
            new ChatMessageAiProvider.ProviderResponse(
                "Respuesta orientativa de prueba.", "stub:chat:turns=1"));
    when(auditoriaUsoIaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.process(request, testJwt());

    assertThat(response.conversationId()).isEqualTo(conversationId);
    assertThat(response.message().role()).isEqualTo(ChatRole.assistant);
    assertThat(response.message().content()).isEqualTo("Respuesta orientativa de prueba.");
    assertThat(response.message().createdAt()).isNotNull();
    verify(requestValidator).validateThread(request);
    verify(chatMessageRateLimiter).checkAllowed("collab-sub");
  }

  @Test
  void process_success_auditsChatMessageWithEjemplarId() {
    UUID conversationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    AiChatMessageRequest request =
        new AiChatMessageRequest(
            conversationId, 42L, List.of(new AiChatTurn(ChatRole.user, "Hola")));
    when(aiPromptFactory.buildChatSystemPrompt()).thenReturn("system prompt");
    when(chatMessageAiProvider.requestChat(any(), any()))
        .thenReturn(
            new ChatMessageAiProvider.ProviderResponse("Respuesta.", "stub:chat:turns=1"));
    when(auditoriaUsoIaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.process(request, testJwt());

    ArgumentCaptor<AuditoriaUsoIa> captor = ArgumentCaptor.forClass(AuditoriaUsoIa.class);
    verify(auditoriaUsoIaRepository).save(captor.capture());
    AuditoriaUsoIa auditoria = captor.getValue();
    assertThat(auditoria.getSubjectOidc()).isEqualTo("collab-sub");
    assertThat(auditoria.getTipoUsoIa()).isEqualTo(ChatMessageService.TIPO_USO_IA);
    assertThat(auditoria.getEjemplarId()).isEqualTo(42L);
    assertThat(auditoria.getPrompt()).contains("system prompt");
    assertThat(auditoria.getPrompt()).contains("user: Hola");
    assertThat(auditoria.getResultadoResumen())
        .isEqualTo("stub:chat:turns=1;conversationId=" + conversationId);
    assertThat(auditoria.getConsultadoEn()).isNotNull();
  }

  @Test
  void process_trimsLongPromptAndProviderSummary() {
    UUID conversationId = UUID.randomUUID();
    String longContent = "x".repeat(3000);
    AiChatMessageRequest request =
        new AiChatMessageRequest(
            conversationId, 7L, List.of(new AiChatTurn(ChatRole.user, longContent)));
    when(aiPromptFactory.buildChatSystemPrompt()).thenReturn("p".repeat(9000));
    when(chatMessageAiProvider.requestChat(any(), any()))
        .thenReturn(
            new ChatMessageAiProvider.ProviderResponse("ok", "s".repeat(5000)));
    when(auditoriaUsoIaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.process(request, testJwt());

    ArgumentCaptor<AuditoriaUsoIa> captor = ArgumentCaptor.forClass(AuditoriaUsoIa.class);
    verify(auditoriaUsoIaRepository).save(captor.capture());
    assertThat(captor.getValue().getPrompt()).hasSize(8000);
    assertThat(captor.getValue().getResultadoResumen()).hasSize(4000);
  }

  @Test
  void process_whenProviderReturnsBlank_throwsBadGatewayAndDoesNotAudit() {
    AiChatMessageRequest request =
        new AiChatMessageRequest(
            UUID.randomUUID(),
            1L,
            List.of(new AiChatTurn(ChatRole.user, "Hola")));
    when(aiPromptFactory.buildChatSystemPrompt()).thenReturn("system");
    when(chatMessageAiProvider.requestChat(any(), any()))
        .thenReturn(new ChatMessageAiProvider.ProviderResponse("   ", "empty"));

    assertThatThrownBy(() -> service.process(request, testJwt()))
        .isInstanceOf(AiAssistantException.class)
        .extracting(ex -> ((AiAssistantException) ex).getStatus())
        .isEqualTo(HttpStatus.BAD_GATEWAY);

    verify(auditoriaUsoIaRepository, never()).save(any());
  }
}
