package com.mtl.ai.integration.support;

import java.util.UUID;

/** Constantes HTTP y cuerpos JSON compartidos por tests de integración y WebMvcTest. */
public final class AiIntegrationFixtures {

  private AiIntegrationFixtures() {}

  public static final String CHAT_PATH = "/api/ai/chat/messages";
  public static final String ENRICHMENT_PATH = "/api/ai/species/enrichment-suggestions";

  public static final UUID CONVERSATION_ID =
      UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
  public static final String CONVERSATION_ID_STRING = CONVERSATION_ID.toString();
  public static final long TREE_ID = 42L;

  public static final String ENRICHMENT_REQUEST_BODY =
      "{\"scientificName\":\"Quercus ilex\",\"commonName\":\"Encina\"}";

  public static final String CHAT_REQUEST_BODY =
      """
      {
        "conversationId":"550e8400-e29b-41d4-a716-446655440000",
        "treeId":42,
        "messages":[{"role":"user","content":"¿Qué datos necesito para dar de alta un árbol?"}]
      }
      """;

  public static final String CHAT_REQUEST_BODY_MINIMAL =
      """
      {
        "conversationId":"550e8400-e29b-41d4-a716-446655440000",
        "treeId":42,
        "messages":[{"role":"user","content":"Hola"}]
      }
      """;

  public static final String CHAT_REQUEST_BODY_ASSISTANT_LAST_TURN =
      """
      {
        "conversationId":"550e8400-e29b-41d4-a716-446655440000",
        "treeId":42,
        "messages":[{"role":"assistant","content":"Solo assistant"}]
      }
      """;

  public static final String CHAT_REQUEST_BODY_INVALID_TREE_ID =
      """
      {
        "conversationId":"550e8400-e29b-41d4-a716-446655440000",
        "treeId":0,
        "messages":[{"role":"user","content":"Hola"}]
      }
      """;

  public static String chatResultSummary(String providerSummary) {
    return providerSummary + ";conversationId=" + CONVERSATION_ID_STRING;
  }
}
