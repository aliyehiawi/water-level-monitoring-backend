package com.example.waterlevel.config;

import com.example.waterlevel.constants.MqttConstants;
import com.example.waterlevel.constants.MqttTopics;
import java.util.UUID;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * MQTT configuration for publishing messages to hardware devices.
 *
 * <p>Uses Eclipse Paho MQTT client for publishing pump commands and threshold updates.
 */
@Configuration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttConfig {

  @Value("${mqtt.broker.url:tcp://localhost:1883}")
  private String brokerUrl;

  @Value("${mqtt.client.id:test-client}")
  private String clientId;

  @Value("${mqtt.username:}")
  private String username;

  @Value("${mqtt.password:}")
  private String password;

  @Value(
      "${mqtt.connection.timeout-seconds:" + MqttConstants.DEFAULT_CONNECTION_TIMEOUT_SECONDS + "}")
  private int connectionTimeoutSeconds;

  @Value(
      "${mqtt.keep-alive.interval-seconds:"
          + MqttConstants.DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS
          + "}")
  private int keepAliveIntervalSeconds;

  @Bean
  public MqttPahoClientFactory mqttClientFactory() {
    DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
    MqttConnectOptions options = new MqttConnectOptions();
    options.setServerURIs(new String[] {brokerUrl});
    options.setCleanSession(true);
    options.setConnectionTimeout(connectionTimeoutSeconds);
    options.setKeepAliveInterval(keepAliveIntervalSeconds);
    options.setAutomaticReconnect(true);

    if (username != null && !username.isEmpty()) {
      options.setUserName(username);
    }
    if (password != null && !password.isEmpty()) {
      options.setPassword(password.toCharArray());
    }

    factory.setConnectionOptions(options);
    return factory;
  }

  @Bean
  public MessageChannel mqttOutboundChannel() {
    return new DirectChannel();
  }

  @Bean
  @ServiceActivator(inputChannel = "mqttOutboundChannel")
  public MessageHandler mqttOutboundHandler() {
    MqttPahoMessageHandler messageHandler =
        new MqttPahoMessageHandler(
            clientId + "-" + UUID.randomUUID().toString(), mqttClientFactory());
    messageHandler.setAsync(true);
    messageHandler.setDefaultQos(MqttConstants.DEFAULT_QOS_LEVEL);
    messageHandler.setConverter(new DefaultPahoMessageConverter());
    return messageHandler;
  }

  @Bean
  public MessageChannel mqttInboundChannel() {
    return new DirectChannel();
  }

  @Bean
  @ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
  public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter() {
    MqttPahoMessageDrivenChannelAdapter adapter =
        new MqttPahoMessageDrivenChannelAdapter(
            clientId + "-inbound-" + UUID.randomUUID().toString(),
            mqttClientFactory(),
            MqttTopics.SENSOR_DATA_PATTERN);
    adapter.setQos(MqttConstants.DEFAULT_QOS_LEVEL);
    adapter.setOutputChannel(mqttInboundChannel());
    DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
    converter.setPayloadAsBytes(true);
    adapter.setConverter(converter);
    return adapter;
  }
}
