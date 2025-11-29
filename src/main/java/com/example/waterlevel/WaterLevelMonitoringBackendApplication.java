package com.example.waterlevel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WaterLevelMonitoringBackendApplication {

  WaterLevelMonitoringBackendApplication() {
    // Package-private constructor to allow Spring to create proxies
    // while still preventing direct instantiation from other packages
  }

  public static void main(final String[] args) {
    SpringApplication.run(WaterLevelMonitoringBackendApplication.class, args);
  }
}
