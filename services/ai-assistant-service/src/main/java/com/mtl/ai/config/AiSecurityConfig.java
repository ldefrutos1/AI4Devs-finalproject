package com.mtl.ai.config;

import com.mtl.ai.web.error.ProblemAccessDeniedHandler;
import com.mtl.ai.web.error.ProblemAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AiSecurityConfig {

  private final ProblemAuthenticationEntryPoint authenticationEntryPoint;
  private final ProblemAccessDeniedHandler accessDeniedHandler;

  public AiSecurityConfig(
      ProblemAuthenticationEntryPoint authenticationEntryPoint,
      ProblemAccessDeniedHandler accessDeniedHandler) {
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
  }

  @Bean
  SecurityFilterChain aiSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus")
                    .permitAll()
                    .requestMatchers("/api/ai/species/enrichment-suggestions")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/ai/chat/messages")
                    .hasAnyRole("COLABORADOR", "ADMIN")
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(
                            KeycloakRealmRoleConverter.jwtAuthenticationConverter())))
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .build();
  }
}
