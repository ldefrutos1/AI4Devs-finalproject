package com.mtl.ai.web.error;

import com.mtl.ai.exception.AiAssistantException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AiAssistantExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(AiAssistantExceptionHandler.class);
  private static final String TITLE_BAD_REQUEST = "Petición inválida";

  @ExceptionHandler(AiAssistantException.class)
  public ResponseEntity<ProblemDetail> handleAiAssistant(
      AiAssistantException ex, HttpServletRequest request) {
    log.warn("Error de asistente IA: {} - {}", ex.getTitle(), ex.getDetail());
    ProblemDetail pd = ex.toProblemDetail(URI.create(request.getRequestURI()));
    ProblemDetailEnricher.enrichWithCorrelationId(pd);
    return ResponseEntity.status(ex.getStatus()).body(pd);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    String detail =
        ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining("; "));
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    pd.setTitle(TITLE_BAD_REQUEST);
    pd.setInstance(URI.create(request.getRequestURI()));
    ProblemDetailEnricher.enrichWithCorrelationId(pd);
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String detail =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    pd.setTitle(TITLE_BAD_REQUEST);
    pd.setInstance(URI.create(request.getRequestURI()));
    ProblemDetailEnricher.enrichWithCorrelationId(pd);
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleAny(Exception ex, HttpServletRequest request) {
    log.error("Error no controlado en {} {}", request.getMethod(), request.getRequestURI(), ex);
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ha ocurrido un error interno. Inténtelo de nuevo más tarde.");
    pd.setTitle("Error interno");
    pd.setInstance(URI.create(request.getRequestURI()));
    ProblemDetailEnricher.enrichWithCorrelationId(pd);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
  }
}
