package com.mtl.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record AiChatMessageRequest(
    @NotNull UUID conversationId,
    @NotNull @Min(1) Long treeId,
    @NotEmpty @Size(max = 20) List<@Valid AiChatTurn> messages) {}
