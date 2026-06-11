package com.mtl.catalog.application;

import com.mtl.catalog.domain.Ejemplar;
import com.mtl.catalog.dto.TreeEnrichmentReplaceRequest;
import com.mtl.catalog.dto.TreeEnrichmentResponse;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class TreeEnrichmentService {

  private final EjemplarEnrichmentAccessService ejemplarEnrichmentAccessService;
  private final EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository;

  public TreeEnrichmentService(
      EjemplarEnrichmentAccessService ejemplarEnrichmentAccessService,
      EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository) {
    this.ejemplarEnrichmentAccessService = ejemplarEnrichmentAccessService;
    this.ejemplarDetalleMongoRepository = ejemplarDetalleMongoRepository;
  }

  @Transactional(readOnly = true)
  public TreeEnrichmentResponse getTreeEnrichment(long treeId, Jwt jwt) {
    Ejemplar ejemplar = ejemplarEnrichmentAccessService.requireReadableEjemplar(treeId, jwt);
    EjemplarDetalleDocument document = ejemplarDetalleMongoRepository.findById(treeId).orElse(null);
    return TreeEnrichmentMapper.toResponse(treeId, ejemplar.getEspecieId(), document);
  }

  @Transactional
  public TreeEnrichmentResponse replaceTreeEnrichment(
      long treeId, TreeEnrichmentReplaceRequest request, Jwt jwt) {
    Ejemplar ejemplar = ejemplarEnrichmentAccessService.requireWritableEjemplar(treeId, jwt);
    TreeEnrichmentValidator.validateMeasurements(
        request != null ? request.measurements() : null);
    EjemplarDetalleDocument document =
        TreeEnrichmentMapper.toDocument(treeId, ejemplar.getEspecieId(), request);
    EjemplarDetalleDocument saved = ejemplarDetalleMongoRepository.save(document);
    return TreeEnrichmentMapper.toResponse(treeId, ejemplar.getEspecieId(), saved);
  }
}
