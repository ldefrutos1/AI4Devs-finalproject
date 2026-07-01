package com.mtl.ai.controller.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

@TestConfiguration
public class WebMvcTestJsonMapperConfig {

  @Bean
  JsonMapper aiWebMvcTestJsonMapper() {
    return JsonMapper.builder().build();
  }
}
