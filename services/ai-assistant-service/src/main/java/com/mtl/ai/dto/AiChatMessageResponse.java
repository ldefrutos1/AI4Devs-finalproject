package com.mtl.ai.dto;

import java.util.UUID;

public record AiChatMessageResponse(UUID conversationId, AiChatAssistantMessage message) {}
