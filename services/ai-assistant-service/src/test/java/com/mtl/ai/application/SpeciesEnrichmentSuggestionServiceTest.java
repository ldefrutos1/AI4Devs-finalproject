package com.mtl.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.ai.domain.AuditoriaUsoIa;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionRequest;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionResponse;
import com.mtl.ai.exception.AiAssistantException;
import com.mtl.ai.infrastructure.persistence.jpa.repository.AuditoriaUsoIaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class SpeciesEnrichmentSuggestionServiceTest {

  @Mock private AiPromptFactory aiPromptFactory;
  @Mock private SpeciesEnrichmentAiProvider aiProvider;
  @Mock private SpeciesEnrichmentValidationService validationService;
  @Mock private AuditoriaUsoIaRepository auditoriaUsoIaRepository;

  @InjectMocks private SpeciesEnrichmentSuggestionService service;

  @Test
  void suggest_invokesProvider_validatesAndAudits() {
    AiSpeciesEnrichmentSuggestionRequest request =
        new AiSpeciesEnrichmentSuggestionRequest("Quercus ilex", "Encina");
    Jwt jwt =
        Jwt.withTokenValue("test")
            .header("alg", "none")
            .issuer("http://localhost:8180/realms/mtl")
            .subject("admin-sub")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    String prompt = "prompt";
    String rawJson = "{\"synonyms\":[\"Encina\"]}";
    AiSpeciesEnrichmentSuggestionResponse expected =
        new AiSpeciesEnrichmentSuggestionResponse(
            List.of("Encina"), null, Map.of("habitat", List.of("bosque")), null);

    when(aiPromptFactory.buildSpeciesEnrichmentPrompt(request)).thenReturn(prompt);
    when(aiProvider.requestSuggestion(prompt, "Quercus ilex", "Encina"))
        .thenReturn(new SpeciesEnrichmentAiProvider.ProviderResponse(rawJson, "stub:Quercus ilex"));
    when(validationService.validateAndMap(rawJson)).thenReturn(expected);
    when(auditoriaUsoIaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    AiSpeciesEnrichmentSuggestionResponse actual = service.suggest(request, jwt);

    assertThat(actual).isEqualTo(expected);
    ArgumentCaptor<AuditoriaUsoIa> captor = ArgumentCaptor.forClass(AuditoriaUsoIa.class);
    verify(auditoriaUsoIaRepository).save(captor.capture());
    assertThat(captor.getValue().getSubjectOidc()).isEqualTo("admin-sub");
    assertThat(captor.getValue().getTipoUsoIa())
        .isEqualTo(SpeciesEnrichmentSuggestionService.TIPO_USO_IA);
    assertThat(captor.getValue().getPrompt()).isEqualTo(prompt);
    assertThat(captor.getValue().getResultadoResumen()).isEqualTo("stub:Quercus ilex");
    assertThat(captor.getValue().getConsultadoEn()).isNotNull();
  }

  @Test
  void suggest_trimsLongPromptAndProviderSummary() {
    AiSpeciesEnrichmentSuggestionRequest request =
        new AiSpeciesEnrichmentSuggestionRequest("Quercus ilex", "Encina");
    Jwt jwt =
        Jwt.withTokenValue("test")
            .header("alg", "none")
            .issuer("http://localhost:8180/realms/mtl")
            .subject("admin-sub")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    String longPrompt = "p".repeat(9000);
    String longSummary = "s".repeat(5000);
    String rawJson = "{\"synonyms\":[\"Encina\"]}";
    AiSpeciesEnrichmentSuggestionResponse expected =
        new AiSpeciesEnrichmentSuggestionResponse(List.of("Encina"), null, null, null);

    when(aiPromptFactory.buildSpeciesEnrichmentPrompt(request)).thenReturn(longPrompt);
    when(aiProvider.requestSuggestion(longPrompt, "Quercus ilex", "Encina"))
        .thenReturn(new SpeciesEnrichmentAiProvider.ProviderResponse(rawJson, longSummary));
    when(validationService.validateAndMap(rawJson)).thenReturn(expected);
    when(auditoriaUsoIaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.suggest(request, jwt);

    ArgumentCaptor<AuditoriaUsoIa> captor = ArgumentCaptor.forClass(AuditoriaUsoIa.class);
    verify(auditoriaUsoIaRepository).save(captor.capture());
    assertThat(captor.getValue().getPrompt()).hasSize(8000);
    assertThat(captor.getValue().getResultadoResumen()).hasSize(4000);
  }

  @Test
  void suggest_whenValidationFails_doesNotAudit() {
    AiSpeciesEnrichmentSuggestionRequest request =
        new AiSpeciesEnrichmentSuggestionRequest("Quercus ilex", "Encina");
    Jwt jwt =
        Jwt.withTokenValue("test")
            .header("alg", "none")
            .issuer("http://localhost:8180/realms/mtl")
            .subject("admin-sub")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    String rawJson = "{\"unexpected\":true}";

    when(aiPromptFactory.buildSpeciesEnrichmentPrompt(request)).thenReturn("prompt");
    when(aiProvider.requestSuggestion("prompt", "Quercus ilex", "Encina"))
        .thenReturn(new SpeciesEnrichmentAiProvider.ProviderResponse(rawJson, "stub:invalid"));
    when(validationService.validateAndMap(rawJson))
        .thenThrow(
            new AiAssistantException(
                HttpStatus.UNPROCESSABLE_ENTITY, "Respuesta IA inválida", "detalle"));

    assertThatThrownBy(() -> service.suggest(request, jwt))
        .isInstanceOf(AiAssistantException.class);

    verify(auditoriaUsoIaRepository, never()).save(any());
  }
}
