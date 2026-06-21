package com.mtl.media.application;

import com.mtl.media.domain.Fotografia;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import java.io.InputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Lectura de bytes de una fotografía persistida en MinIO. */
@Component
public class MediaStoredPhotoLoader {

  private final MinioClient mediaMinioClient;

  public MediaStoredPhotoLoader(MinioClient mediaMinioClient) {
    this.mediaMinioClient = mediaMinioClient;
  }

  public ResponseEntity<byte[]> toImageResponse(Fotografia foto) {
    byte[] body;
    try (InputStream stream =
        mediaMinioClient.getObject(
            GetObjectArgs.builder()
                .bucket(foto.getBucketAlmacenamiento())
                .object(foto.getClaveObjeto())
                .build())) {
      body = stream.readAllBytes();
    } catch (ErrorResponseException ex) {
      if ("NoSuchKey".equals(ex.errorResponse().code())
          || "NoSuchBucket".equals(ex.errorResponse().code())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Objeto no encontrado en almacén");
      }
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "No se pudo leer la imagen desde el almacén de objetos");
    } catch (MinioException
        | java.io.IOException
        | java.security.InvalidKeyException
        | java.security.NoSuchAlgorithmException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "No se pudo leer la imagen desde el almacén de objetos");
    }

    MediaType contentType = MediaType.parseMediaType(foto.getTipoMime());
    return ResponseEntity.ok().contentType(contentType).body(body);
  }
}
