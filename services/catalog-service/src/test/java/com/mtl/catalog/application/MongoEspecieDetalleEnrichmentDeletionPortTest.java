package com.mtl.catalog.application;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoEspecieDetalleEnrichmentDeletionPortTest {

  @Mock private EspecieDetalleMongoRepository especieDetalleMongoRepository;

  @InjectMocks private MongoEspecieDetalleEnrichmentDeletionPort port;

  @Test
  void deleteEnrichmentForSpecies_documentoExistente_eliminaFisicamente() {
    when(especieDetalleMongoRepository.existsById(9L)).thenReturn(true);

    port.deleteEnrichmentForSpecies(9L);

    verify(especieDetalleMongoRepository).deleteById(9L);
  }

  @Test
  void deleteEnrichmentForSpecies_sinDocumento_noInvocaDelete() {
    when(especieDetalleMongoRepository.existsById(9L)).thenReturn(false);

    port.deleteEnrichmentForSpecies(9L);

    verify(especieDetalleMongoRepository, never()).deleteById(9L);
  }
}
