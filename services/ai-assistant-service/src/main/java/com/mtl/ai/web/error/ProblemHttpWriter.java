package com.mtl.ai.web.error;

import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

public final class ProblemHttpWriter {

  private ProblemHttpWriter() {}

  public static void write(
      HttpServletResponse response, JsonMapper jsonMapper, ProblemDetail problemDetail)
      throws IOException {
    response.setStatus(problemDetail.getStatus());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    jsonMapper.writeValue(response.getWriter(), problemDetail);
  }
}
