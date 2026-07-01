package com.mtl.ai.application;

/** Mensajes de error al cliente para rate limit de chat (HU-010). */
public final class ChatRateLimitMessages {

  public static final String TITLE_TOO_MANY_REQUESTS = "Demasiadas consultas";
  public static final String DETAIL_TOO_MANY_REQUESTS =
      "Has superado el límite de consultas al asistente. Inténtalo más tarde.";

  private ChatRateLimitMessages() {}
}
