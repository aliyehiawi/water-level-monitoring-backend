package com.example.waterlevel.config;

import com.example.waterlevel.constants.SecurityConstants;
import com.example.waterlevel.filter.JwtAuthenticationFilter;
import com.example.waterlevel.filter.RateLimitingFilter;
import com.example.waterlevel.filter.RequestLoggingFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final RequestLoggingFilter requestLoggingFilter;
  private final RateLimitingFilter rateLimitingFilter;
  private final Environment environment;

  @Value("${cors.allowed-origins:}")
  private String allowedOrigins;

  @Value("${cors.max-age-seconds:" + SecurityConstants.DEFAULT_CORS_MAX_AGE_SECONDS + "}")
  private long corsMaxAgeSeconds;

  @Value("${spring.h2.console.enabled:false}")
  private boolean h2ConsoleEnabled;

  @Value("${springdoc.swagger-ui.enabled:true}")
  private boolean swaggerUiEnabled;

  public SecurityConfig(
      final JwtAuthenticationFilter jwtAuthenticationFilter,
      final RequestLoggingFilter requestLoggingFilter,
      final RateLimitingFilter rateLimitingFilter,
      final Environment environment) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.requestLoggingFilter = requestLoggingFilter;
    this.rateLimitingFilter = rateLimitingFilter;
    this.environment = environment;
  }

  @Bean
  @SuppressWarnings("java:S112") // Spring Security API requires Exception
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    boolean isProduction = isProductionProfile();

    LOGGER.info(
        "Security configuration - H2 Console Enabled: {}, Is Production: {}, Will Allow H2: {}",
        h2ConsoleEnabled,
        isProduction,
        h2ConsoleEnabled && !isProduction);

    http.csrf(csrf -> csrf.disable())
        .headers(
            headers ->
                headers.frameOptions(
                    frameOptions ->
                        frameOptions.sameOrigin())) // Allow frames for H2 console and Swagger UI
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth -> {
              auth.requestMatchers("/auth/register", "/auth/login").permitAll();
              auth.requestMatchers("/ws/**").permitAll();
              auth.requestMatchers("/actuator/health").permitAll();

              if (h2ConsoleEnabled && !isProduction) {
                auth.requestMatchers("/h2-console/**").permitAll();
              }

              if (swaggerUiEnabled) {
                auth.requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**")
                    .permitAll();
              }

              auth.anyRequest().authenticated();
            })
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED, "Authentication required"))
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) ->
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(rateLimitingFilter, RequestLoggingFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  private boolean isProductionProfile() {
    String[] activeProfiles = environment.getActiveProfiles();
    if (activeProfiles.length == 0) {
      activeProfiles = environment.getDefaultProfiles();
    }
    for (String profile : activeProfiles) {
      if (profile != null && (profile.contains("prod") || profile.contains("production"))) {
        return true;
      }
    }
    return false;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    String corsOrigins =
        allowedOrigins != null && !allowedOrigins.trim().isEmpty()
            ? allowedOrigins
            : System.getenv("CORS_ALLOWED_ORIGINS");
    if (corsOrigins != null && !corsOrigins.trim().isEmpty()) {
      configuration.setAllowedOrigins(Arrays.asList(corsOrigins.split(",")));
    }
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(corsMaxAgeSeconds);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  @SuppressWarnings("java:S112") // Spring Security API requires Exception
  public AuthenticationManager authenticationManager(
      final AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
