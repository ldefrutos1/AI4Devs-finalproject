package com.mtl.catalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.catalog.application.PublicTreeEnrichmentQueryService;
import com.mtl.catalog.application.SpeciesEnrichmentService;
import com.mtl.catalog.application.TreeEnrichmentService;
import com.mtl.catalog.config.JwtAuthenticationPrincipalTestMvcConfig;
import com.mtl.catalog.dto.PublicTreeEnrichmentResponse;
import com.mtl.catalog.dto.SpeciesEnrichmentResponse;
import com.mtl.catalog.dto.TreeEnrichmentResponse;
import com.mtl.catalog.web.error.CatalogExceptionHandler;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(
    controllers = CatalogEnrichmentController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@Import({
  CatalogExceptionHandler.class,
  JwtAuthenticationPrincipalTestMvcConfig.class,
  CatalogEnrichmentControllerWebMvcTest.JsonMapperConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = "mtl.catalog.mongo.enabled=true")
class CatalogEnrichmentControllerWebMvcTest {

  @TestConfiguration
  static class JsonMapperConfiguration {
    @Bean
    JsonMapper catalogEnrichmentWebMvcTestJsonMapper() {
      return JsonMapper.builder().build();
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private SpeciesEnrichmentService speciesEnrichmentService;
  @MockitoBean private TreeEnrichmentService treeEnrichmentService;
  @MockitoBean private PublicTreeEnrichmentQueryService publicTreeEnrichmentQueryService;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  private static MockHttpServletRequestBuilder withJwtPrincipal(
      MockHttpServletRequestBuilder builder, Authentication authentication) {
    return builder.with(
        request -> {
          SecurityContextHolder.getContext().setAuthentication(authentication);
          return request;
        });
  }

  private static JwtAuthenticationToken collaboratorAuthentication() {
    Jwt jwt =
        Jwt.withTokenValue("dummy.jwt.value")
            .headers(h -> h.put("alg", "none"))
            .issuer("http://localhost:8180/realms/mtl")
            .subject("kc-sub")
            .audience(Collections.singletonList("account"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .claim("email", "u@example.invalid")
            .build();
    return new JwtAuthenticationToken(
        jwt, Collections.singleton(new SimpleGrantedAuthority("ROLE_COLABORADOR")));
  }

  @Test
  void getSpeciesEnrichment_devuelve200() throws Exception {
    when(speciesEnrichmentService.getSpeciesEnrichment(eq(12L), any()))
        .thenReturn(new SpeciesEnrichmentResponse(12L, "Quercus robur", "Roble", null, null, null, null));

    mockMvc
        .perform(
            withJwtPrincipal(
                get("/api/catalog/species/{speciesId}/enrichment", 12),
                collaboratorAuthentication()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.speciesId").value(12))
        .andExpect(jsonPath("$.scientificName").value("Quercus robur"));
  }

  @Test
  void getTreeEnrichment_devuelve200() throws Exception {
    when(treeEnrichmentService.getTreeEnrichment(eq(42L), any()))
        .thenReturn(
            new TreeEnrichmentResponse(
                42L, 12L, Map.of("heightM", 10.0), null, List.of("monumental"), null));

    mockMvc
        .perform(
            withJwtPrincipal(
                get("/api/catalog/trees/{treeId}/enrichment", 42), collaboratorAuthentication()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.treeId").value(42))
        .andExpect(jsonPath("$.measurements.heightM").value(10.0));
  }

  @Test
  void getPublicTreeEnrichment_devuelve200() throws Exception {
    when(publicTreeEnrichmentQueryService.getPublishedTreeEnrichment(eq(7L), any()))
        .thenReturn(new PublicTreeEnrichmentResponse(null, null));

    mockMvc
        .perform(
            withJwtPrincipal(
                get("/api/catalog/public/trees/{treeId}/enrichment", 7),
                collaboratorAuthentication()))
        .andExpect(status().isOk());
  }

  @Test
  void putTreeEnrichment_devuelve200() throws Exception {
    when(treeEnrichmentService.replaceTreeEnrichment(eq(42L), any(), any()))
        .thenReturn(new TreeEnrichmentResponse(42L, 12L, Map.of("heightM", 5.0), null, null, null));

    mockMvc
        .perform(
            withJwtPrincipal(
                put("/api/catalog/trees/{treeId}/enrichment", 42)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"measurements\":{\"heightM\":5.0}}"),
                collaboratorAuthentication()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.measurements.heightM").value(5.0));
  }
}
