package com.mtl.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AiChatTurn(
    @NotNull ChatRole role,
    @NotBlank @Size(max = 2000) String content) {}
