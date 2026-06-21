package com.mtl.media.config;

import com.mtl.media.infrastructure.storage.MinioObjectStoragePresigner;
import com.mtl.media.infrastructure.storage.MinioObjectStorageRemover;
import com.mtl.media.infrastructure.storage.ObjectStoragePresigner;
import com.mtl.media.infrastructure.storage.ObjectStorageRemover;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaMinioConfig {

  @Bean
  public MinioClient mediaMinioClient(MediaStorageProperties properties) {
    return buildMinioClient(properties.getEndpoint(), properties);
  }

  @Bean
  public ObjectStoragePresigner objectStoragePresigner(
      MediaStorageProperties properties) {
    MinioClient presignClient = buildMinioClient(properties.getPresignEndpoint(), properties);
    return new MinioObjectStoragePresigner(presignClient, properties);
  }

  @Bean
  public ObjectStorageRemover objectStorageRemover(MinioClient mediaMinioClient) {
    return new MinioObjectStorageRemover(mediaMinioClient);
  }

  private static MinioClient buildMinioClient(
      String endpoint, MediaStorageProperties properties) {
    return MinioClient.builder()
        .endpoint(endpoint)
        .region(properties.getRegion())
        .credentials(properties.getAccessKey(), properties.getSecretKey())
        .build();
  }
}
