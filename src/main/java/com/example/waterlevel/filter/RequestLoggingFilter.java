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

    String path = request.getServletPath();
    if (path != null
        && (path.startsWith("/actuator")
            || path.startsWith("/h2-console")
            || path.startsWith("/swagger")
            || path.startsWith("/api-docs"))) {
      filterChain.doFilter(request, response);
      return;
    }

    String traceId = UUID.randomUUID().toString();
    MDC.put(TRACE_ID_KEY, traceId);

    long startTime = System.currentTimeMillis();
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
