package com.mtl.e2e.integration.hu010;

/** Cuerpos JSON y constantes compartidos por los E2E de HU-010. */
final class Hu010E2eFixtures {

  static final String CONVERSATION_ID = "550e8400-e29b-41d4-a716-446655440000";

  private Hu010E2eFixtures() {}

  static String chatRequestBody(long treeId) {
    return """
        {
          "conversationId":"550e8400-e29b-41d4-a716-446655440000",
          "treeId":%d,
          "messages":[{"role":"user","content":"¿Qué datos necesito para documentar un árbol singular?"}]
        }
        """
        .formatted(treeId);
  }
}
