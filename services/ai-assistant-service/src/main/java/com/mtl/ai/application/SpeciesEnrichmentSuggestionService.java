package com.mtl.ai.application;

import com.mtl.ai.domain.AuditoriaUsoIa;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionRequest;
import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionResponse;
import com.mtl.ai.infrastructure.persistence.jpa.repository.AuditoriaUsoIaRepository;
import java.time.OffsetDateTime;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpeciesEnrichmentSuggestionService {

  public static final String TIPO_USO_IA = "species-enrichment-suggestion";

  private final AiPromptFactory aiPromptFactory;
  private final SpeciesEnrichmentAiProvider aiProvider;
  private final SpeciesEnrichmentValidationService validationService;
  private final AuditoriaUsoIaRepository auditoriaUsoIaRepository;

  public SpeciesEnrichmentSuggestionService(
      AiPromptFactory aiPromptFactory,
      SpeciesEnrichmentAiProvider aiProvider,
      SpeciesEnrichmentValidationService validationService,
      AuditoriaUsoIaRepository auditoriaUsoIaRepository) {
    this.aiPromptFactory = aiPromptFactory;
    this.aiProvider = aiProvider;
    this.validationService = validationService;
    this.auditoriaUsoIaRepository = auditoriaUsoIaRepository;
  }

  @Transactional
  public AiSpeciesEnrichmentSuggestionResponse suggest(
      AiSpeciesEnrichmentSuggestionRequest request, Jwt jwt) {
    String prompt = aiPromptFactory.buildSpeciesEnrichmentPrompt(request);
    SpeciesEnrichmentAiProvider.ProviderResponse providerResponse =
        aiProvider.requestSuggestion(prompt, request.scientificName(), request.commonName());
    AiSpeciesEnrichmentSuggestionResponse response =
        validationService.validateAndMap(providerResponse.rawJson());
    saveAudit(jwt, prompt, providerResponse.providerSummary());
    return response;
  }

  private void saveAudit(Jwt jwt, String prompt, String providerSummary) {
    AuditoriaUsoIa auditoria = new AuditoriaUsoIa();
    auditoria.setSubjectOidc(jwt != null ? jwt.getSubject() : "system");
    auditoria.setTipoUsoIa(TIPO_USO_IA);
    auditoria.setEjemplarId(null);
    auditoria.setPrompt(trimToLength(prompt, 8000));
    auditoria.setResultadoResumen(trimToLength(providerSummary, 4000));
    auditoria.setConsultadoEn(OffsetDateTime.now());
    auditoriaUsoIaRepository.save(auditoria);
  }

  private String trimToLength(String value, int maxLength) {
    if (value == null) {
      return "";
    }
    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }
}
