package com.mtl.catalog.application;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoEjemplarEnrichmentDeletionPortTest {

  @Mock private EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository;

  @InjectMocks private MongoEjemplarEnrichmentDeletionPort port;

  @Test
  void deleteEnrichmentForEjemplar_documentoExistente_eliminaFisicamente() {
    when(ejemplarDetalleMongoRepository.existsById(847L)).thenReturn(true);

    port.deleteEnrichmentForEjemplar(847L);

    verify(ejemplarDetalleMongoRepository).deleteById(847L);
  }

  @Test
  void deleteEnrichmentForEjemplar_sinDocumento_noInvocaDelete() {
    when(ejemplarDetalleMongoRepository.existsById(847L)).thenReturn(false);

    port.deleteEnrichmentForEjemplar(847L);

    verify(ejemplarDetalleMongoRepository, never()).deleteById(847L);
  }
}
