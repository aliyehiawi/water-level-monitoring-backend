package com.example.waterlevel.filter;

import com.example.waterlevel.constants.RateLimitConstants;
import com.example.waterlevel.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limiting filter to prevent API abuse.
 *
 * <p>Implements token bucket algorithm with per-IP rate limiting. Uses Caffeine cache with TTL to
 * prevent memory leaks from unbounded bucket storage.
 */
@Component
@Order(2)
public class RateLimitingFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingFilter.class);

  @Value("${rate.limit.requests-per-minute:" + RateLimitConstants.DEFAULT_REQUESTS_PER_MINUTE + "}")
  private int requestsPerMinute;

  @Value("${rate.limit.cache-max-size:" + RateLimitConstants.DEFAULT_CACHE_MAX_SIZE + "}")
  private int cacheMaxSize;

  private Cache<String, Bucket> buckets;
  private final ObjectMapper objectMapper;

  public RateLimitingFilter(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void init() {
    this.buckets =
        Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(RateLimitConstants.BUCKET_EXPIRATION_MINUTES))
            .maximumSize(cacheMaxSize)
            .build();
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getServletPath();
    if (path != null && (path.startsWith("/actuator") || path.startsWith("/h2-console"))) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientIp = getClientIp(request);
    Bucket bucket = buckets.get(clientIp, this::createBucket);

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
    } else {
      LOGGER.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      ErrorResponse errorResponse =
          new ErrorResponse("Rate limit exceeded. Please try again later.");
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }

  private Bucket createBucket(final String key) {
    return Bucket.builder()
        .addLimit(
            Bandwidth.classic(
                requestsPerMinute, Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))))
        .build();
  }

  private String getClientIp(final HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }
}
