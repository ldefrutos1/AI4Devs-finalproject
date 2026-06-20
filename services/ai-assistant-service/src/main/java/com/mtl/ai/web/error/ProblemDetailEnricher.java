package com.mtl.ai.web.error;

import com.mtl.ai.web.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;

public final class ProblemDetailEnricher {

  private ProblemDetailEnricher() {}

  public static void enrichWithCorrelationId(ProblemDetail problemDetail) {
    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
    if (correlationId != null && !correlationId.isBlank()) {
      problemDetail.setProperty("correlationId", correlationId);
    }
  }
}
