package com.example.waterlevel.dto.mqtt;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PumpStartCommand {
  private String command;
  private String timestamp;
  private Long initiatedBy;

  /**
   * Creates a pump start command.
   *
   * @param command the command (typically "START")
   * @param timestamp the timestamp when command was initiated
   * @param initiatedBy the user ID who initiated the command
   */
  public PumpStartCommand(final String command, final Instant timestamp, final Long initiatedBy) {
    this.command = command;
    this.timestamp = timestamp.toString();
    this.initiatedBy = initiatedBy;
  }
}
