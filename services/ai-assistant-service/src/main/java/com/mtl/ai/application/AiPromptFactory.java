package com.mtl.ai.application;

import com.mtl.ai.dto.AiSpeciesEnrichmentSuggestionRequest;
import org.springframework.stereotype.Component;

/** Construye el prompt con el contrato HTTP esperado por la UI en camelCase. */
@Component
public class AiPromptFactory {

  public String buildSpeciesEnrichmentPrompt(AiSpeciesEnrichmentSuggestionRequest request) {
    return """
        Eres un botánico experto. A partir del nombre científico y el nombre común proporcionados,
        genera un JSON orientativo de enriquecimiento de especie para precargar una interfaz de edición.
        Responde SOLO con JSON válido y con valores castellano siempre que sea posible, sin comentarios ni bloques Markdown.

        El JSON raíz puede contener exclusivamente estas claves:
        - synonyms: array<string>
        - distribution: { continents: array<string>, countries: array<string>, description: string }
        - ecologicalData: object
        - references: array<{ title: string, authors: array<string>, source: string, year: int, url: string }>

        Dentro de ecologicalData usa camelCase y prioriza estas claves:
        - habitat: array<string>
        - altitudMinM: int
        - altitudMaxM: int
        - clima: array<string>
        - suelo: array<string>
        - longevityMaxYears: int
        - growthRate: "slow" | "moderate" | "fast"
        - leafType: "deciduous" | "evergreen" | "marcescent"
        - floweringPeriod: { startMonth: int 1..12, endMonth: int 1..12 }
        - associatedFauna: array<string>

        Restricciones:
        - No inventes claves fuera del nivel raíz permitido.
        - Si desconoces un dato, omítelo; no uses null.
        - Las referencias son orientativas y deben sonar plausibles, pero no añadas explicación textual.
        - altitudMinM y altitudMaxM deben ser enteros >= 0 y min < max cuando existan ambos.
        - year no debe ser futuro.

        scientificName: %s
        commonName: %s
        """
        .formatted(request.scientificName().trim(), request.commonName().trim());
  }
}
