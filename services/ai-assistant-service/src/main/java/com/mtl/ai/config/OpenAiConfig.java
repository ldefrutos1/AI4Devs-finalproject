package com.mtl.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({AiProviderProperties.class, OpenAiProperties.class})
public class OpenAiConfig {

  @Bean
  @ConditionalOnProperty(prefix = "mtl.ai.provider", name = "mode", havingValue = "openai")
  RestClient openAiRestClient(OpenAiProperties properties) {
    String baseUrl = properties.baseUrl().replaceAll("/$", "");
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(properties.connectTimeout());
    requestFactory.setReadTimeout(properties.readTimeout());
    return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
  }
}
