package com.mtl.notification.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.notification.application.SubscriptionRegistrationService;
import com.mtl.notification.config.NotificationSecurityConfig;
import com.mtl.notification.dto.SubscriptionCreatedResponse;
import com.mtl.notification.web.error.NotificationExceptionHandler;
import com.mtl.notification.web.error.ProblemAccessDeniedHandler;
import com.mtl.notification.web.error.ProblemAuthenticationEntryPoint;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/**
 * Seguridad real para alta pública POST /api/notifications/subscriptions (sin JWT).
 */
@WebMvcTest(controllers = NotificationSubscriptionsController.class)
@Import({
  NotificationSecurityConfig.class,
  NotificationExceptionHandler.class,
  ProblemAuthenticationEntryPoint.class,
  ProblemAccessDeniedHandler.class,
  NotificationPublicSubscriptionSecurityWebMvcTest.JsonMapperWebMvcTestConfigurationTest.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationPublicSubscriptionSecurityWebMvcTest {

  @TestConfiguration
  static class JsonMapperWebMvcTestConfigurationTest {
    @Bean
    JsonMapper notificationPublicSecurityTestJsonMapper() {
      return JsonMapper.builder().build();
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;

  @MockitoBean private SubscriptionRegistrationService subscriptionRegistrationService;

  @Test
  void post_altaPublica_sinBearer_devuelve201() throws Exception {
    when(subscriptionRegistrationService.register(anyString()))
        .thenReturn(new SubscriptionCreatedResponse("visitante@example.com"));

    mockMvc
        .perform(
            post("/api/notifications/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"visitante@example.com\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("visitante@example.com"));
  }

  @Test
  void post_altaPublica_conColaborador_noDevuelve403() throws Exception {
    when(jwtDecoder.decode(anyString()))
        .thenReturn(
            Jwt.withTokenValue("colab-token")
                .header("alg", "none")
                .issuer("http://localhost:8180/realms/mtl")
                .subject("colab-sub")
                .claim("realm_access", Map.of("roles", List.of("COLABORADOR")))
                .build());
    when(subscriptionRegistrationService.register(anyString()))
        .thenReturn(new SubscriptionCreatedResponse("colab@example.com"));

    mockMvc
        .perform(
            post("/api/notifications/subscriptions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer colab-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"colab@example.com\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("colab@example.com"));
  }
}
