package com.example.waterlevel.controller;

import com.example.waterlevel.dto.WaterLevelDataResponse;
import com.example.waterlevel.entity.WaterLevelData;
import com.example.waterlevel.service.WaterLevelDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devices/{deviceId}/water-level-data")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Water Level Data", description = "Read-only endpoints for water level data")
public class WaterLevelDataController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WaterLevelDataController.class);

  private final WaterLevelDataService waterLevelDataService;

  public WaterLevelDataController(final WaterLevelDataService waterLevelDataService) {
    this.waterLevelDataService = waterLevelDataService;
  }

  @Operation(
      summary = "Get water level data (paginated)",
      description = "Retrieves paginated historical water level data for a device (latest first)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Water level data retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid device ID or pagination parameters"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  @GetMapping
  public ResponseEntity<Page<WaterLevelDataResponse>> getWaterLevelData(
      @Parameter(description = "Device ID", example = "1") @PathVariable final Long deviceId,
      @Parameter(description = "Page number (0-indexed)", example = "0")
          @RequestParam(defaultValue = "0")
          @Min(value = 0, message = "Page number must be >= 0")
          final int page,
      @Parameter(description = "Page size", example = "20")
          @RequestParam(defaultValue = "20")
          @Min(value = 1, message = "Page size must be >= 1")
          @Max(value = 200, message = "Page size must be <= 200")
          final int size) {
    LOGGER.debug(
        "Get water level data request: deviceId={}, page={}, size={}", deviceId, page, size);

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
    Page<WaterLevelData> data =
        waterLevelDataService.getWaterLevelDataForDevice(deviceId, pageable);

    Page<WaterLevelDataResponse> response =
        data.map(
            row ->
                new WaterLevelDataResponse(
                    row.getId(),
                    row.getDevice() != null ? row.getDevice().getId() : deviceId,
                    row.getWaterLevel(),
                    row.getPumpStatus(),
                    row.getTimestamp()));

    return ResponseEntity.ok(response);
  }
}
