package com.mtl.e2e.support;

/** Mensajes Problem alineados con gateway y microservicios MVC (RFC 9457). */
public final class E2eProblemExpectations {

  public static final String UNAUTHORIZED_TITLE = "No autenticado";
  public static final String UNAUTHORIZED_DETAIL =
      "Se requiere autenticación con un token Bearer válido";

  public static final String FORBIDDEN_TITLE = "Prohibido";
  public static final String FORBIDDEN_DETAIL =
      "No tiene permisos para acceder a este recurso";

  private E2eProblemExpectations() {}
}
