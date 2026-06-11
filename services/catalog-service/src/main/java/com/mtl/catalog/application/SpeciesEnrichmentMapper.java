package com.mtl.catalog.application;

import com.mtl.catalog.dto.BibliographicReferenceDto;
import com.mtl.catalog.dto.SpeciesDistributionDto;
import com.mtl.catalog.dto.SpeciesEnrichmentReplaceRequest;
import com.mtl.catalog.dto.SpeciesEnrichmentResponse;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.document.ReferenciaBibliograficaEmbeddable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class SpeciesEnrichmentMapper {

  private SpeciesEnrichmentMapper() {}

  static SpeciesEnrichmentResponse toResponse(
      long speciesId, String scientificName, String commonName, EspecieDetalleDocument document) {
    if (document == null) {
      return new SpeciesEnrichmentResponse(
          speciesId, scientificName, commonName, null, null, null, null);
    }
    return new SpeciesEnrichmentResponse(
        speciesId,
        document.getNombreCientifico() != null ? document.getNombreCientifico() : scientificName,
        document.getNombreComun() != null ? document.getNombreComun() : commonName,
        emptyToNull(document.getSinonimos()),
        toDistributionDto(document.getDistribucion()),
        emptyToNull(document.getDatosEcologicos()),
        toReferenceDtos(document.getReferencias()));
  }

  static EspecieDetalleDocument toDocument(
      long speciesId,
      String scientificName,
      String commonName,
      SpeciesEnrichmentReplaceRequest request) {
    EspecieDetalleDocument document = new EspecieDetalleDocument();
    document.assignEspeciePgId(speciesId);
    document.setNombreCientifico(scientificName);
    document.setNombreComun(commonName);
    if (request == null) {
      document.setSinonimos(new ArrayList<>());
      document.setDistribucion(new LinkedHashMap<>());
      document.setDatosEcologicos(new LinkedHashMap<>());
      document.setReferencias(new ArrayList<>());
      return document;
    }
    document.setSinonimos(
        request.synonyms() != null ? new ArrayList<>(request.synonyms()) : new ArrayList<>());
    document.setDistribucion(
        request.distribution() != null
            ? EnrichmentPersistenceMapping.distributionToMongo(
                request.distribution().continents(),
                request.distribution().countries(),
                request.distribution().description())
            : new LinkedHashMap<>());
    document.setDatosEcologicos(
        request.ecologicalData() != null
            ? new LinkedHashMap<>(request.ecologicalData())
            : new LinkedHashMap<>());
    document.setReferencias(toReferenceEmbeddables(request.references()));
    return document;
  }

  private static SpeciesDistributionDto toDistributionDto(Map<String, Object> distribucion) {
    if (distribucion == null || distribucion.isEmpty()) {
      return null;
    }
    return new SpeciesDistributionDto(
        EnrichmentPersistenceMapping.stringList(distribucion.get("continentes")),
        EnrichmentPersistenceMapping.stringList(distribucion.get("paises")),
        distribucion.get("descripcion") != null
            ? distribucion.get("descripcion").toString()
            : null);
  }

  private static List<BibliographicReferenceDto> toReferenceDtos(
      List<ReferenciaBibliograficaEmbeddable> referencias) {
    if (referencias == null || referencias.isEmpty()) {
      return null;
    }
    return referencias.stream()
        .map(
            ref ->
                new BibliographicReferenceDto(
                    ref.getTitulo(),
                    emptyToNull(ref.getAutores()),
                    ref.getFuente(),
                    ref.getAnio(),
                    ref.getUrl()))
        .toList();
  }

  private static List<ReferenciaBibliograficaEmbeddable> toReferenceEmbeddables(
      List<BibliographicReferenceDto> references) {
    if (references == null) {
      return new ArrayList<>();
    }
    List<ReferenciaBibliograficaEmbeddable> embeddables = new ArrayList<>();
    for (BibliographicReferenceDto dto : references) {
      ReferenciaBibliograficaEmbeddable embeddable = new ReferenciaBibliograficaEmbeddable();
      embeddable.setTitulo(dto.title());
      embeddable.setAutores(dto.authors() != null ? new ArrayList<>(dto.authors()) : new ArrayList<>());
      embeddable.setFuente(dto.source());
      embeddable.setAnio(dto.year());
      embeddable.setUrl(dto.url());
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
