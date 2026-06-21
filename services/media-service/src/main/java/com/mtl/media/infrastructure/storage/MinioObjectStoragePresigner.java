package com.mtl.media.infrastructure.storage;

import com.mtl.media.config.MediaStorageProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Presign PUT/GET con SigV4. El cliente debe usar el endpoint público ({@code publicEndpoint}) para
 * que el {@code host} firmado coincida con el PUT del navegador. La región fijada en el cliente
 * evita {@code GetBucketLocation} (TCP) durante {@code getPresignedObjectUrl}.
 */
public class MinioObjectStoragePresigner implements ObjectStoragePresigner {

  private final MinioClient minioClient;
  private final String region;

  public MinioObjectStoragePresigner(MinioClient minioClient, MediaStorageProperties properties) {
    this.minioClient = minioClient;
    this.region = properties.getRegion();
  }

  @Override
  public String presignedPutUrl(String bucket, String objectKey, Duration ttl) {
    return presignedUrl(Method.PUT, bucket, objectKey, ttl);
  }

  @Override
  public String presignedGetUrl(String bucket, String objectKey, Duration ttl) {
    return presignedUrl(Method.GET, bucket, objectKey, ttl);
  }

  private String presignedUrl(Method method, String bucket, String objectKey, Duration ttl) {
    long seconds = ttl.toSeconds();
    if (seconds <= 0) {
      seconds = 60;
    }
    int expiry = (int) Math.min(seconds, Integer.MAX_VALUE);
    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(method)
              .bucket(bucket)
              .object(objectKey)
              .region(region)
              .expiry(expiry, TimeUnit.SECONDS)
              .build());
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo generar URL prefirmada en MinIO", e);
    }
  }
}
