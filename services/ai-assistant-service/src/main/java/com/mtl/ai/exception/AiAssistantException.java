package com.mtl.ai.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class AiAssistantException extends RuntimeException {

  private final HttpStatus status;
  private final String title;

  public AiAssistantException(HttpStatus status, String title, String detail) {
    super(detail);
    this.status = status;
    this.title = title;
  }

  public AiAssistantException(HttpStatus status, String title, String detail, Throwable cause) {
    super(detail, cause);
    this.status = status;
    this.title = title;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return getMessage();
  }

  public ProblemDetail toProblemDetail(URI instance) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, getDetail());
    pd.setTitle(title);
    pd.setInstance(instance);
    return pd;
  }
}
