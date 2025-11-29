package com.example.waterlevel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WaterLevelMonitoringBackendApplicationTests {

  /**
   * Verifies that the Spring application context loads successfully.
   *
   * <p>This is a standard Spring Boot integration test that ensures all beans are properly
   * configured and the application can start without errors.
   */
  @Test
  void contextLoads() {
    // Test passes if application context loads successfully
  }
}
