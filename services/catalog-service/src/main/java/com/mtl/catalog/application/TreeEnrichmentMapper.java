package com.mtl.catalog.application;

import com.mtl.catalog.dto.FieldObservationDto;
import com.mtl.catalog.dto.TreeEnrichmentReplaceRequest;
import com.mtl.catalog.dto.TreeEnrichmentResponse;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.document.ObservacionEmbeddable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TreeEnrichmentMapper {

  private TreeEnrichmentMapper() {}

  static TreeEnrichmentResponse toResponse(
      long treeId, Long speciesId, EjemplarDetalleDocument document) {
    if (document == null) {
      return new TreeEnrichmentResponse(treeId, speciesId, null, null, null, null);
    }
    return new TreeEnrichmentResponse(
        treeId,
        document.getEspeciePgId() != null ? document.getEspeciePgId() : speciesId,
        EnrichmentPersistenceMapping.measurementsToApi(document.getMedidas()),
        emptyToNull(document.getEstadoSanitario()),
        emptyToNull(document.getEtiquetas()),
        toObservationDtos(document.getObservaciones()));
  }

  static EjemplarDetalleDocument toDocument(
      long treeId, long speciesId, TreeEnrichmentReplaceRequest request) {
    EjemplarDetalleDocument document = new EjemplarDetalleDocument();
    document.assignEjemplarPgId(treeId);
    document.setEspeciePgId(speciesId);
    if (request == null) {
      document.setMedidas(new LinkedHashMap<>());
      document.setEstadoSanitario(new LinkedHashMap<>());
      document.setEtiquetas(new ArrayList<>());
      document.setObservaciones(new ArrayList<>());
      return document;
    }
    document.setMedidas(EnrichmentPersistenceMapping.measurementsToMongo(request.measurements()));
    document.setEstadoSanitario(
        request.healthStatus() != null
            ? new LinkedHashMap<>(request.healthStatus())
            : new LinkedHashMap<>());
    document.setEtiquetas(
        request.tags() != null ? new ArrayList<>(request.tags()) : new ArrayList<>());
    document.setObservaciones(toObservationEmbeddables(request.observations()));
    return document;
  }

  private static List<FieldObservationDto> toObservationDtos(
      List<ObservacionEmbeddable> observaciones) {
    if (observaciones == null || observaciones.isEmpty()) {
      return null;
    }
    return observaciones.stream()
        .map(
            obs ->
                new FieldObservationDto(
                    obs.getFecha(),
                    obs.getTexto(),
                    obs.getAutor(),
                    emptyToNull(obs.getCondiciones())))
        .toList();
  }

  private static List<ObservacionEmbeddable> toObservationEmbeddables(
      List<FieldObservationDto> observations) {
    if (observations == null) {
      return new ArrayList<>();
    }
    List<ObservacionEmbeddable> embeddables = new ArrayList<>();
    for (FieldObservationDto dto : observations) {
      ObservacionEmbeddable embeddable = new ObservacionEmbeddable();
      embeddable.setFecha(dto.date());
      embeddable.setTexto(dto.text());
      embeddable.setAutor(dto.author());
      embeddable.setCondiciones(
          dto.conditions() != null ? new LinkedHashMap<>(dto.conditions()) : new LinkedHashMap<>());
      embeddables.add(embeddable);
    }
    return embeddables;
  }

  private static <T> List<T> emptyToNull(List<T> values) {
    return values == null || values.isEmpty() ? null : values;
  }

  private static <K, V> Map<K, V> emptyToNull(Map<K, V> values) {
    return values == null || values.isEmpty() ? null : values;
  }
}
