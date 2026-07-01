package com.mtl.ai.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AiPromptFactoryTest {

  private final AiPromptFactory factory = new AiPromptFactory();

  @Test
  void buildChatSystemPrompt_containsOrientativeScopeWithoutTreeContext() {
    String prompt = factory.buildChatSystemPrompt();

    assertThat(prompt).contains("MyTreeLibrary");
    assertThat(prompt).contains("orientativa");
    assertThat(prompt).contains("No tienes acceso a los datos concretos de la ficha");
    assertThat(prompt.toLowerCase()).contains("castellano");
  }
}
