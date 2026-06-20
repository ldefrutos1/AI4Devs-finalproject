package com.mtl.ai.application;

/** Mensajes de error al cliente para respuestas IA (HU-016, product-context). */
public final class AiResponseErrorMessages {

  public static final String TITLE_NOT_FOUND = "Sin resultado IA";
  public static final String TITLE_INVALID = "Respuesta IA inválida";
  public static final String TITLE_PROVIDER_UNAVAILABLE = "Proveedor IA no disponible";

  public static final String DETAIL_NOT_FOUND =
      "La IA no devolvió contenido utilizable para la especie solicitada."
          + " Los datos de IA son orientativos y deben revisarse manualmente antes de usarlos.";

  public static final String DETAIL_INVALID_JSON =
      "La IA devolvió un JSON no válido. No se puede precargar la pantalla con una sugerencia"
          + " orientativa hasta corregir el formato.";

  public static final String VALIDATION_SUFFIX =
      " No se puede precargar la pantalla; la sugerencia de IA es orientativa y debe revisarse"
          + " manualmente.";

  private AiResponseErrorMessages() {}
}
