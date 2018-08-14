/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.pmw.tinylog.Logger;

import com.justin.energy.bootloader.fota.ParameterFotaHandler;
import com.justin.energy.bootloader.fota.SoftwareFotaHandler;
import com.justin.energy.common.WebsocketOverMqttClient;
import com.justin.energy.common.WebsocketOverMqttClient.QOS;
import com.justin.energy.common.config.MqttConfiguration;
import com.justin.energy.common.exception.ShutdownException;
import com.justin.energy.common.exception.StartupException;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class BootLoader {
  public static void main(final String[] args) {
    try {
      new BootLoader().start();
    } catch (final StartupException ex) {
      Logger.error(ex, "Could not start the energy bootloader");
    }
  }

  private final String gatewayId = ApplicationProperties.getConfiguredDeviceId();
  private final ApplicationProperties properties = new ApplicationProperties();
  private WebsocketOverMqttClient mqttClient;
  private final EnergyReaderWatchDog energyReaderWatchDog;
  /**
   * Software upgrades, parameter upgrades and start-reader-application can't run at same time.
   */
  private final Object softwareUpgradeLock = new Object();

  public BootLoader() throws StartupException {
    if (System.getenv("JAVA_HOME") == null) {
      throw new StartupException("JAVA_HOME environment variables is not set");
    }
    if (gatewayId == null) {
      throw new StartupException("ENERGY_GATEWAY_ID environment variables is not set");
    }
    try {
      properties.load();
    } catch (final IOException ex) {
      throw new StartupException("Could not load the application properties file", ex);
    }
    energyReaderWatchDog =
        new EnergyReaderWatchDog(properties.getReaderApplicationLockPort(), softwareUpgradeLock);
  }

  public void start() throws StartupException {
    try {
      final MqttConfiguration mqttConfig = new MqttConfiguration();
      mqttConfig.setBrokerPassword(properties.getBrokerPassword());
      mqttConfig.setBrokerUrl(properties.getBrokerUrl());
      mqttConfig.setBrokerUsername(properties.getBrokerUsername());
      mqttClient = new WebsocketOverMqttClient(UUID.randomUUID().toString(), mqttConfig);
      mqttClient.connect();

      final String fotaParameterTopic =
          String.format(properties.getFotaParameterTopic(), gatewayId);
      mqttClient.subscribe(fotaParameterTopic, QOS.AT_LEAST_ONE, new ParameterFotaHandler(gatewayId,
          softwareUpgradeLock, properties.getReaderApplicationLockPort()));
      final String fotaSoftwareTopic = String.format(properties.getFotaSoftwareTopic(), gatewayId);
      mqttClient.subscribe(fotaSoftwareTopic, QOS.AT_LEAST_ONE, new SoftwareFotaHandler(gatewayId,
          softwareUpgradeLock, properties.getReaderApplicationLockPort()));

      Logger.info("Connected to broker: {}, topics [{}, {}]", mqttConfig.getBrokerUrl(),
          fotaParameterTopic, fotaSoftwareTopic);
    } catch (final Exception ex) {
      throw new StartupException("Could not initialize MQTT client", ex);
    }

    energyReaderWatchDog.start();
  }

  public void stop() throws ShutdownException {
    boolean error = false;
    try {
      energyReaderWatchDog.waitAndStop();
    } catch (final InterruptedException ex) {
      Logger.error(ex);
      error = true;
    }
    try {
      mqttClient.disconnect();
    } catch (MqttException | InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }
    if (error) {
      throw new ShutdownException("Could not fully stop bootloader due to exceptions");
    }
  }
}
