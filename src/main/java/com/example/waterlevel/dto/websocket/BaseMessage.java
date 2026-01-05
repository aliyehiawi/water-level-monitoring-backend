package com.example.waterlevel.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseMessage {
  private String type;
  private Long deviceId;
  private String timestamp;
}
