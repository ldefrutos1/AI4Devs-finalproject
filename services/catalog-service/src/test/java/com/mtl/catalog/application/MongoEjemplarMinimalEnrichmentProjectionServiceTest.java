package com.mtl.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.catalog.domain.Especie;
import com.mtl.catalog.infrastructure.persistence.jpa.repository.EspecieRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.document.EjemplarDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoEjemplarMinimalEnrichmentProjectionServiceTest {

  @Mock private EspecieRepository especieRepository;
  @Mock private EspecieDetalleMongoNamesWriter especieDetalleMongoNamesWriter;
  @Mock private EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository;

  @InjectMocks private MongoEjemplarMinimalEnrichmentProjectionService service;

  @Test
  void projectAfterEjemplarSqlPersisted_upsertMinimoEnAmbasColecciones() {
    Especie especie = new Especie();
    especie.setNombreCientifico("Quercus ilex");
    especie.setNombreComun("Encina");
    when(especieRepository.findById(12L)).thenReturn(Optional.of(especie));
    when(ejemplarDetalleMongoRepository.findById(847L)).thenReturn(Optional.empty());
    when(ejemplarDetalleMongoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertThat(service.projectAfterEjemplarSqlPersisted(847L, 12L)).isEmpty();

    verify(especieDetalleMongoNamesWriter).upsertNames(12L, "Quercus ilex", "Encina");

    ArgumentCaptor<EjemplarDetalleDocument> ejemplarCaptor =
        ArgumentCaptor.forClass(EjemplarDetalleDocument.class);
    verify(ejemplarDetalleMongoRepository).save(ejemplarCaptor.capture());
    assertThat(ejemplarCaptor.getValue().getId()).isEqualTo(847L);
    assertThat(ejemplarCaptor.getValue().getEspeciePgId()).isEqualTo(12L);
  }

  @Test
  void projectAfterEjemplarSqlPersisted_actualizaEspecieEnEjemplarExistente() {
    Especie especie = new Especie();
    especie.setNombreCientifico("Quercus robur");
    especie.setNombreComun("Roble");
    when(especieRepository.findById(99L)).thenReturn(Optional.of(especie));

    EjemplarDetalleDocument existing = new EjemplarDetalleDocument();
    existing.assignEjemplarPgId(847L);
    existing.setEspeciePgId(12L);
    existing.getMedidas().put("altura_m", 10.0);
    when(ejemplarDetalleMongoRepository.findById(847L)).thenReturn(Optional.of(existing));
    when(ejemplarDetalleMongoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertThat(service.projectAfterEjemplarSqlPersisted(847L, 99L)).isEmpty();

    verify(especieDetalleMongoNamesWriter).upsertNames(99L, "Quercus robur", "Roble");

    ArgumentCaptor<EjemplarDetalleDocument> ejemplarCaptor =
        ArgumentCaptor.forClass(EjemplarDetalleDocument.class);
    verify(ejemplarDetalleMongoRepository).save(ejemplarCaptor.capture());
    assertThat(ejemplarCaptor.getValue().getEspeciePgId()).isEqualTo(99L);
    assertThat(ejemplarCaptor.getValue().getMedidas()).containsEntry("altura_m", 10.0);
  }

  @Test
  void projectAfterEjemplarSqlPersisted_siMongoFalla_devuelveAviso() {
    when(especieRepository.findById(12L)).thenThrow(new RuntimeException("mongo down"));

    Optional<String> warning = service.projectAfterEjemplarSqlPersisted(847L, 12L);

    assertThat(warning).contains(MongoEjemplarMinimalEnrichmentProjectionService.ENRICHMENT_WARNING_MESSAGE);
  }
}
