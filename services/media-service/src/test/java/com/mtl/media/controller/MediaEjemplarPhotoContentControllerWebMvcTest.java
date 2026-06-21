package com.mtl.media.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.media.application.MediaEjemplarPhotoContentService;
import com.mtl.media.config.MediaJwtAuthenticationPrincipalTestMvcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = MediaEjemplarPhotoContentController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@Import(MediaJwtAuthenticationPrincipalTestMvcConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MediaEjemplarPhotoContentControllerWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private MediaEjemplarPhotoContentService photoContentService;

  @Test
  void getPhotoContent_ok_returnsImageBytes() throws Exception {
    when(photoContentService.loadVisiblePhotoBytes(eq(5L), eq(10L), eq(null)))
        .thenReturn(ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new byte[] {1, 2, 3}));

    mockMvc
        .perform(get("/api/media/trees/5/photos/10/content"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
        .andExpect(content().bytes(new byte[] {1, 2, 3}));
  }
}
