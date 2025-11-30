package com.example.waterlevel.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI/Swagger configuration for API documentation. */
@Configuration
public class OpenApiConfig {

  @Value("${server.servlet.context-path:/api}")
  private String contextPath;

  @Value("${springdoc.server-url:}")
  private String serverUrl;

  @Value("${springdoc.contact.name:Water Level Monitoring Team}")
  private String contactName;

  @Value("${springdoc.contact.email:}")
  private String contactEmail;

  @Bean
  public OpenAPI customOpenAPI() {
    final String securitySchemeName = "bearerAuth";

    return new OpenAPI()
        .info(
            new Info()
                .title("Water Level Monitoring Backend API")
                .description(
                    "A comprehensive backend system for monitoring water levels with automated"
                        + " pump control, user management, and real-time data monitoring."
                        + "\n\n"
                        + "## Features\n"
                        + "- User Management: Registration, authentication, and role-based access"
                        + " control (USER/ADMIN)\n"
                        + "- Device Management: Device registration with secure key-based"
                        + " authentication\n"
                        + "- Real-time Monitoring: WebSocket-based live data streaming\n"
                        + "- Automated Control: Threshold-based pump automation\n"
                        + "- Manual Control: Admin override for pump operations\n"
                        + "\n"
                        + "## Authentication\n"
                        + "Most endpoints require JWT authentication. Register or login to get a"
                        + " token, then include it in the Authorization header as: `Bearer"
                        + " <token>`")
                .version("1.0.0")
                .contact(
                    new Contact()
                        .name(contactName)
                        .email(
                            contactEmail != null && !contactEmail.isEmpty() ? contactEmail : null))
                .license(
                    new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
        .servers(
            List.of(
                new Server()
                    .url((serverUrl != null && !serverUrl.isEmpty() ? serverUrl : "") + contextPath)
                    .description("API Server")))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "JWT token obtained from /api/auth/login or /api/auth/register")));
  }
}
