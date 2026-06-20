package com.mtl.ai.application;

/** Puerto hacia el proveedor externo de IA. */
public interface SpeciesEnrichmentAiProvider {

  ProviderResponse requestSuggestion(String prompt, String scientificName, String commonName);

  record ProviderResponse(String rawJson, String providerSummary) {}
}
