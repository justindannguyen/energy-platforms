/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.pmw.tinylog.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import com.justin.config.ApplicationProperties;
import com.justin.config.LocalStorage;
import com.justin.config.MeterConfiguration;
import com.justin.config.RunConfiguration;
import com.justin.exception.ShutdownException;
import com.justin.exception.StartupException;
import com.justin.fota.ParameterFotaHandler;
import com.justin.fota.SoftwareFotaHandler;
import com.justin.transmission.CloudClient;
import com.justin.transmission.CloudClient.QOS;
import com.justin.transmission.MeterModbusMaster;
import com.justin.transmission.dto.DeviceStatusDto;
import com.justin.transmission.dto.EnergyUsageDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class EnergyGateway {
  private static final int initialReportDelay = 20;
  private static final int initialDeviceStatusDelay = 60;

  private final ScheduledExecutorService mainExecutor = Executors.newScheduledThreadPool(1);
  private final ObjectMapper objectMapper = new ObjectMapper();
  private CloudClient cloudClient;
  private MeterModbusMaster meterModbusMaster;
  private RunConfiguration runConfiguration;
  private Exception lastError;
  private final String gatewayId;
  private final ApplicationProperties properties = new ApplicationProperties();

  public EnergyGateway() throws StartupException {
    gatewayId = System.getenv("ENERGY_GATEWAY_ID");
    if (gatewayId == null) {
      throw new StartupException("ENERGY_GATEWAY_ID environment variables is not set", null);
    }
    try {
      properties.load();
    } catch (final IOException ex) {
      throw new StartupException("Could not load the application properties file", ex);
    }
  }

  public String getGatewayId() {
    return gatewayId;
  }

  public void start() throws StartupException {
    loadRunConfigurations();
    connectWithBroker();
    connectWithMeters();
    mainExecutor.scheduleWithFixedDelay(() -> reportEnergyData(), initialReportDelay,
        runConfiguration.getEnergyReportInterval(), TimeUnit.SECONDS);
    mainExecutor.scheduleWithFixedDelay(() -> reportDeviceStatus(), initialDeviceStatusDelay,
        runConfiguration.getDeviceStatusReportInterval(), TimeUnit.SECONDS);
  }

  public void stop() throws ShutdownException {
    boolean error = false;
    try {
      meterModbusMaster.disconnect();
    } catch (ModbusIOException | InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }
    try {
      cloudClient.disconnect();
    } catch (MqttException | InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }
    mainExecutor.shutdown();
    try {
      mainExecutor.awaitTermination(30, TimeUnit.SECONDS);
    } catch (final InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }
    if (error) {
      throw new ShutdownException("Fail during shutdown process");
    }
  }

  private void connectWithBroker() throws StartupException {
    try {
      cloudClient = new CloudClient(runConfiguration);
      cloudClient.connect();

      final String fotaParameterTopic =
          String.format(properties.getFotaParameterTopic(), gatewayId);
      cloudClient.subscribe(fotaParameterTopic, QOS.AT_LEAST_ONE, new ParameterFotaHandler(this));
      final String fotaSoftwareTopic = String.format(properties.getFotaSoftwareTopic(), gatewayId);
      cloudClient.subscribe(fotaSoftwareTopic, QOS.AT_LEAST_ONE, new SoftwareFotaHandler(this));

      Logger.info("Connected to broker: " + runConfiguration.getBrokerUrl());
    } catch (final MqttException ex) {
      final String message = "Can't connect to Mqtt broker";
      Logger.error(ex, message);
      throw new StartupException(message, ex);
    }
  }

  private void connectWithMeters() throws StartupException {
    try {
      meterModbusMaster = new MeterModbusMaster(runConfiguration);
      meterModbusMaster.connect();

      Logger.info("Connected to modbus, serial {} speed {} parity {} databits {} stopbits {}",
          runConfiguration.getSerialPort(), runConfiguration.getBaudRate(),
          runConfiguration.getParity(), runConfiguration.getDataBits(),
          runConfiguration.getStopBits());
    } catch (SerialPortException | ModbusIOException ex) {
      final String message = "Can't connect to Modbus";
      Logger.error(ex, message);
      throw new StartupException(message, ex);
    }
  }

  private RunConfiguration downloadAndSaveConfigurations() throws StartupException {
    try {
      // Download configuration from server
      final String configApiPath = String.format(properties.getConfigApiPath(), gatewayId);
      final URLConnection connection = new URL(configApiPath).openConnection();
      final boolean httpConnection = configApiPath.startsWith("http");
      if (httpConnection) {
        ((HttpURLConnection) connection).setRequestMethod("GET");
      }
      Logger.info("Downloading configuration from {}", configApiPath);
      final RunConfiguration config =
          objectMapper.readValue(connection.getInputStream(), RunConfiguration.class);
      if (httpConnection) {
        ((HttpURLConnection) connection).disconnect();
      }

      // Store configuration at local for next run
      LocalStorage.store(config);
      return config;
    } catch (final Exception ex) {
      throw new StartupException("Could not download the run configuration from cloud", ex);
    }
  }

  private RunConfiguration loadLocalConfigurations() throws StartupException {
    Logger.info("Loading configuration from local {}", LocalStorage.getRunConfigurationFile());
    try {
      return LocalStorage.load();
    } catch (final Exception ex) {
      final String message = "Local configuration file is damaged";
      Logger.error(message, ex);
      throw new StartupException(message, ex);
    }
  }

  private void loadRunConfigurations() throws StartupException {
    final boolean localConfigurationExist = LocalStorage.doesConfigurationExist();
    runConfiguration =
        localConfigurationExist ? loadLocalConfigurations() : downloadAndSaveConfigurations();

    // Validate the configuration
    if (runConfiguration == null) {
      throw new StartupException("Configuration is not valid", null);
    }
    if (!properties.getSchemaVersion().equals(runConfiguration.getSchemaVersion())) {
      Logger.warn("Run configuration schema mismatch, expect {} but found {}.",
          properties.getSchemaVersion(), runConfiguration.getSchemaVersion());
    }
    Logger.info("Run-configuration is now available");
  }

  private void reportDeviceStatus() {
    final DeviceStatusDto status = new DeviceStatusDto();
    status.setGatewayId(gatewayId);
    if (lastError != null) {
      status.setLastGatewayError(lastError.getMessage());
    }
    if (cloudClient.getLastError() != null) {
      status.setLastMqttError(cloudClient.getLastError().getMessage());
    }
    if (meterModbusMaster.getLastError() != null) {
      status.setLastModbusError(meterModbusMaster.getLastError().getMessage());
    }
    final String topic = String.format(properties.getDeviceStatusTopic(), gatewayId);
    cloudClient.publish(topic, QOS.AT_MOST_ONE, status);
    Logger.info("Device status are sent to cloud");
  }

  private void reportEnergyData() {
    // Skip execution if no meters configured/connect to modbus
    final List<MeterConfiguration> meterConfigurations = runConfiguration.getMeterConfigurations();
    if (meterConfigurations.isEmpty()) {
      return;
    }

    // Read the energy sample from modbus, skip error to read till last meters
    final List<EnergyUsageDto> energyReports = new ArrayList<>();
    meterConfigurations.forEach(config -> {
      config.getEnergyReportRegisters().forEach(register -> {
        final int meterId = config.getMeterId();
        final int[] registerData = meterModbusMaster.readHoldingRegister(meterId, register);
        if (registerData != null) {
          energyReports.add(new EnergyUsageDto().setEnergyUsageResponse(registerData)
              .setGatewayId(gatewayId).setMeterId(meterId).setRegisterId(register.getRegisterId()));
        }
      });
    });

    // Convert energy sample to JSON then share to cloud, skip error to continue with next sample
    final String topic = String.format(properties.getEnergyReportTopic(), gatewayId);
    energyReports.forEach(report -> {
      if (cloudClient.publish(topic, QOS.AT_MOST_ONE, report)) {
        Logger.info("Energy report (meter {}) sample are sent to cloud", report.getMeterId());
      }
    });
  }
}
