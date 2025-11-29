package com.example.waterlevel.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
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
public class MqttConfig {

  @Value("${mqtt.broker.url:tcp://test.mosquitto.org:1883}")
  private String brokerUrl;

  @Value("${mqtt.client.id:water-level-backend}")
  private String clientId;

  @Value("${mqtt.username:}")
  private String username;

  @Value("${mqtt.password:}")
  private String password;

  @Bean
  public MqttPahoClientFactory mqttClientFactory() {
    DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
    MqttConnectOptions options = new MqttConnectOptions();
    options.setServerURIs(new String[] {brokerUrl});
    options.setCleanSession(true);
    options.setConnectionTimeout(30);
    options.setKeepAliveInterval(60);
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
            clientId + "-" + System.currentTimeMillis(), mqttClientFactory());
    messageHandler.setAsync(true);
    messageHandler.setDefaultQos(1);
    messageHandler.setConverter(new DefaultPahoMessageConverter());
    return messageHandler;
  }

  @Bean
  public MessageChannel mqttInboundChannel() {
    return new DirectChannel();
  }

  @Bean
  public org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
      mqttInboundAdapter() {
    org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter adapter =
        new org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter(
            clientId + "-inbound-" + System.currentTimeMillis(),
            mqttClientFactory(),
            "devices/+/sensor/data");
    adapter.setQos(1);
    adapter.setOutputChannel(mqttInboundChannel());
    adapter.setConverter(new DefaultPahoMessageConverter());
    return adapter;
  }
}
