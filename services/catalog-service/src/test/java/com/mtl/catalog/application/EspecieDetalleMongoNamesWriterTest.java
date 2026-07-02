package com.mtl.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.catalog.infrastructure.persistence.mongo.document.EspecieDetalleDocument;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EspecieDetalleMongoNamesWriterTest {

  @Mock private EspecieDetalleMongoRepository especieDetalleMongoRepository;

  @InjectMocks private EspecieDetalleMongoNamesWriter writer;

  @Test
  void upsertNames_creaDocumentoConNombres() {
    when(especieDetalleMongoRepository.findById(12L)).thenReturn(Optional.empty());
    when(especieDetalleMongoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    writer.upsertNames(12L, "Quercus ilex", "Encina");

    ArgumentCaptor<EspecieDetalleDocument> captor =
        ArgumentCaptor.forClass(EspecieDetalleDocument.class);
    verify(especieDetalleMongoRepository).save(captor.capture());
    assertThat(captor.getValue().getId()).isEqualTo(12L);
    assertThat(captor.getValue().getNombreCientifico()).isEqualTo("Quercus ilex");
    assertThat(captor.getValue().getNombreComun()).isEqualTo("Encina");
  }

  @Test
  void updateNamesIfPresent_actualizaNombresPreservandoEnriquecimiento() {
    EspecieDetalleDocument existing = new EspecieDetalleDocument();
    existing.assignEspeciePgId(12L);
    existing.setNombreCientifico("Quercus ilex");
    existing.setNombreComun("Encina");
    existing.setSinonimos(List.of("Encina mediterránea"));
    when(especieDetalleMongoRepository.findById(12L)).thenReturn(Optional.of(existing));
    when(especieDetalleMongoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    writer.updateNamesIfPresent(12L, "Quercus robur", "Roble común");

    ArgumentCaptor<EspecieDetalleDocument> captor =
        ArgumentCaptor.forClass(EspecieDetalleDocument.class);
    verify(especieDetalleMongoRepository).save(captor.capture());
    assertThat(captor.getValue().getNombreCientifico()).isEqualTo("Quercus robur");
    assertThat(captor.getValue().getNombreComun()).isEqualTo("Roble común");
    assertThat(captor.getValue().getSinonimos()).containsExactly("Encina mediterránea");
  }

  @Test
  void updateNamesIfPresent_sinDocumento_noPersiste() {
    when(especieDetalleMongoRepository.findById(12L)).thenReturn(Optional.empty());

    writer.updateNamesIfPresent(12L, "Quercus robur", "Roble común");

    verify(especieDetalleMongoRepository, never()).save(any());
  }
}

@ExtendWith(MockitoExtension.class)
class MongoEspecieDetalleNamesSyncServiceTest {

  @Mock private EspecieDetalleMongoNamesWriter especieDetalleMongoNamesWriter;

  @InjectMocks private MongoEspecieDetalleNamesSyncService syncService;

  @Test
  void syncNamesAfterMasterUpdate_delegaEnWriter() {
    syncService.syncNamesAfterMasterUpdate(9L, "Quercus robur", "Roble común");

    verify(especieDetalleMongoNamesWriter)
        .updateNamesIfPresent(9L, "Quercus robur", "Roble común");
  }

  @Test
  void syncNamesAfterMasterUpdate_falloWriter_noPropagaExcepcion() {
    doThrow(new RuntimeException("mongo down"))
        .when(especieDetalleMongoNamesWriter)
        .updateNamesIfPresent(9L, "Quercus robur", "Roble común");

    assertThatCode(
            () ->
                syncService.syncNamesAfterMasterUpdate(9L, "Quercus robur", "Roble común"))
        .doesNotThrowAnyException();
  }
}
