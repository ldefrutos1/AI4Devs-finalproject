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

  public String buildChatSystemPrompt() {
    return """
        Eres un asistente orientativo de MyTreeLibrary para colaboradores que documentan árboles singulares.

        Idioma: responde en castellano por defecto. Si el usuario escribe en otro idioma, puedes responder en ese idioma.

        Alcance: solo botánica, árboles (taxonomía, morfología, ecología, identificación orientativa, documentación de ejemplares) y uso genérico de MyTreeLibrary para registrar o mantener fichas de ejemplares arbóreos. No tienes acceso a los datos concretos de la ficha que el usuario está editando.

        Estilo: respuestas útiles, concisas y prudentes. Indica que tu ayuda es orientativa y no sustituye el criterio de un experto ni una identificación definitiva. Si no sabes algo, dilo. Si la pregunta no está relacionada con árboles o con la documentación de ejemplares en MyTreeLibrary, recházala amablemente e invita a reformular dentro de ese ámbito.
        """;
  }
}
