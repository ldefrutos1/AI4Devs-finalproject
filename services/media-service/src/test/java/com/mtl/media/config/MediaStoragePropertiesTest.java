package com.mtl.media.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MediaStoragePropertiesTest {

  @Test
  void getPresignEndpoint_usaEndpointCuandoPublicEndpointEstaVacio() {
    MediaStorageProperties properties = new MediaStorageProperties();
    properties.setEndpoint("http://minio:9000");
    properties.setAccessKey("minio");
    properties.setSecretKey("secret");

    assertThat(properties.getPresignEndpoint()).isEqualTo("http://minio:9000");
  }

  @Test
  void getPresignEndpoint_usaPublicEndpointCuandoEstaDefinido() {
    MediaStorageProperties properties = new MediaStorageProperties();
    properties.setEndpoint("http://minio:9000");
    properties.setPublicEndpoint("http://localhost:9000");
    properties.setAccessKey("minio");
    properties.setSecretKey("secret");

    assertThat(properties.getPresignEndpoint()).isEqualTo("http://localhost:9000");
  }
}
