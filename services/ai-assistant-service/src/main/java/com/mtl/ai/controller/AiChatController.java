package com.mtl.ai.controller;

import com.mtl.ai.application.ChatMessageService;
import com.mtl.ai.dto.AiChatMessageRequest;
import com.mtl.ai.dto.AiChatMessageResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/chat")
public class AiChatController {

  private final ChatMessageService chatMessageService;

  public AiChatController(ChatMessageService chatMessageService) {
    this.chatMessageService = chatMessageService;
  }

  @PostMapping("/messages")
  public AiChatMessageResponse sendMessage(
      @Valid @RequestBody AiChatMessageRequest request, @AuthenticationPrincipal Jwt jwt) {
    return chatMessageService.process(request, jwt);
  }
}
