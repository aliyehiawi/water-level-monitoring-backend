package com.example.waterlevel.config;

import com.example.waterlevel.constants.SecurityConstants;
import com.example.waterlevel.filter.JwtAuthenticationFilter;
import com.example.waterlevel.filter.RateLimitingFilter;
import com.example.waterlevel.filter.RequestLoggingFilter;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final RequestLoggingFilter requestLoggingFilter;
  private final RateLimitingFilter rateLimitingFilter;

  @Value("${spring.websocket.allowed-origins:}")
  private String allowedOrigins;

  @Value("${cors.max-age-seconds:" + SecurityConstants.DEFAULT_CORS_MAX_AGE_SECONDS + "}")
  private long corsMaxAgeSeconds;

  public SecurityConfig(
      final JwtAuthenticationFilter jwtAuthenticationFilter,
      final RequestLoggingFilter requestLoggingFilter,
      final RateLimitingFilter rateLimitingFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.requestLoggingFilter = requestLoggingFilter;
    this.rateLimitingFilter = rateLimitingFilter;
  }

  @Bean
  @SuppressWarnings("java:S112") // Spring Security API requires Exception
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/ws/**")
                    .permitAll()
                    .requestMatchers("/api/data/**")
                    .permitAll()
                    .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/devices/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(rateLimitingFilter, RequestLoggingFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
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
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }

  @Bean
  @SuppressWarnings("java:S112") // Spring Security API requires Exception
  public AuthenticationManager authenticationManager(
      final AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
