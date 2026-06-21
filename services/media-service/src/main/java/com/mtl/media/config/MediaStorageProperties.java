package com.mtl.media.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "mtl.media.storage")
public class MediaStorageProperties {

  @NotBlank private String bucket = "mtl-photos";
  @NotBlank private String endpoint = "http://localhost:9000";

  /**
   * Host base de las URLs prefirmadas devueltas al navegador. Si no se define, coincide con
   * {@link #endpoint}. En Docker, {@code endpoint} suele ser interno ({@code http://minio:9000})
   * y {@code publicEndpoint} el puerto publicado en el host ({@code http://localhost:9000}).
   */
  private String publicEndpoint;

  /**
   * Credenciales del almacén S3/MinIO. Configurar en perfil {@code dev} o variables de entorno
   * {@code MINIO_ROOT_USER} / {@code MINIO_ROOT_PASSWORD} (véase infra/compose/.env.example).
   */
  @NotBlank private String accessKey;

  @NotBlank private String secretKey;

  /** Región S3/MinIO; evita GetBucketLocation en presign (sin TCP al generar la URL). */
  private String region = "us-east-1";

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getPublicEndpoint() {
    return publicEndpoint;
  }

  public void setPublicEndpoint(String publicEndpoint) {
    this.publicEndpoint = publicEndpoint;
  }

  /**
   * Endpoint con el que el SDK firma URLs prefirmadas (SigV4 incluye el {@code host}). Debe ser
   * alcanzable desde el navegador que hará el PUT ({@code publicEndpoint} en Docker). No reescribir
   * el host tras firmar: invalida la firma.
   */
  public String getPresignEndpoint() {
    if (publicEndpoint == null || publicEndpoint.isBlank()) {
      return endpoint;
    }
    return publicEndpoint;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }
}
