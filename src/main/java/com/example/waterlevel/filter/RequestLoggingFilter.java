package com.example.waterlevel.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Filter for logging HTTP requests and responses.
 *
 * <p>Logs request method, URI, status code, and execution time. Adds trace ID to MDC for
 * correlation.
 */
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);
  private static final String TRACE_ID_KEY = "traceId";

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {

    // Skip logging for actuator endpoints and static resources
    String path = request.getRequestURI();
    if (path.startsWith("/api/actuator")
        || path.startsWith("/api/h2-console")
        || path.startsWith("/api/swagger")
        || path.startsWith("/api/api-docs")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Generate trace ID for request correlation
    String traceId = UUID.randomUUID().toString();
    MDC.put(TRACE_ID_KEY, traceId);

    long startTime = System.currentTimeMillis();

    // Wrap response to enable content caching for logging
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

    try {
      filterChain.doFilter(request, wrappedResponse);

      long duration = System.currentTimeMillis() - startTime;
      LOGGER.info(
          "{} {} - Status: {} - Duration: {}ms",
          request.getMethod(),
          request.getRequestURI(),
          wrappedResponse.getStatus(),
          duration);
    } finally {
      wrappedResponse.copyBodyToResponse();
      MDC.clear();
    }
  }
}
