/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
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
import com.justin.energy.common.WebsocketOverMqttClient;
import com.justin.energy.common.WebsocketOverMqttClient.QOS;
import com.justin.energy.common.config.LocalStorage;
import com.justin.energy.common.exception.ShutdownException;
import com.justin.energy.common.exception.StartupException;
import com.justin.energy.reader.config.ApplicationProperties;
import com.justin.energy.reader.config.MeterConfiguration;
import com.justin.energy.reader.config.RunConfiguration;
import com.justin.energy.reader.transmission.MeterModbusMaster;
import com.justin.energy.reader.transmission.dto.DeviceStatusDto;
import com.justin.energy.reader.transmission.dto.EnergyUsageDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class EnergyReader {
  private static final int initialReportDelay = 20;
  private static final int initialDeviceStatusDelay = 60;

  public static void main(final String[] args) throws StartupException {
    new EnergyReader().start();
  }

  private final ScheduledExecutorService mainExecutor = Executors.newScheduledThreadPool(1);
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ApplicationProperties properties = new ApplicationProperties();
  private final LocalStorage<RunConfiguration> localStorage = new LocalStorage<>();
  private final String gatewayId = System.getenv("ENERGY_GATEWAY_ID");
  private WebsocketOverMqttClient cloudClient;
  private MeterModbusMaster meterModbusMaster;
  private RunConfiguration runConfiguration;
  private Exception lastError;
  private final ServerSocket applicationLock;

  public EnergyReader() throws StartupException {
    if (gatewayId == null) {
      throw new StartupException("ENERGY_GATEWAY_ID environment variables is not set");
    }
    try {
      properties.load();
    } catch (final IOException ex) {
      throw new StartupException("Could not load the application properties file", ex);
    }
    try {
      applicationLock = new ServerSocket(properties.getApplicationLockPort());
    } catch (final IOException ex) {
      throw new StartupException("Application is already running");
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
    // Disconnect modbus
    try {
      meterModbusMaster.disconnect();
    } catch (ModbusIOException | InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }
    // Disconnect from MQTT
    try {
      cloudClient.disconnect();
    } catch (MqttException | InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }
    // Shutdown the TODO tasks and wait for incompleted tasks
    mainExecutor.shutdown();
    try {
      mainExecutor.awaitTermination(30, TimeUnit.SECONDS);
    } catch (final InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }
    // Release application lock
    try {
      applicationLock.close();
    } catch (final IOException ex) {
      error = true;
      Logger.error(ex);
    }
    if (error) {
      throw new ShutdownException("Fail during shutdown process");
    }
  }

  private void connectWithBroker() throws StartupException {
    try {
      cloudClient = new WebsocketOverMqttClient(gatewayId, runConfiguration);
      cloudClient.connect();

      final String fotaParameterTopic =
          String.format(properties.getFotaParameterTopic(), gatewayId);
      cloudClient.subscribe(fotaParameterTopic, QOS.AT_LEAST_ONE, new FotaHandler(this));
      final String fotaSoftwareTopic = String.format(properties.getFotaSoftwareTopic(), gatewayId);
      cloudClient.subscribe(fotaSoftwareTopic, QOS.AT_LEAST_ONE, new FotaHandler(this));

      Logger.info("Connected to broker: " + runConfiguration.getBrokerUrl());
    } catch (final MqttException ex) {
      throw new StartupException("Can't connect to Mqtt broker", ex);
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
      localStorage.store(config);
      return config;
    } catch (final Exception ex) {
      throw new StartupException("Could not download the run configuration from cloud", ex);
    }
  }

  private RunConfiguration loadLocalConfigurations() throws StartupException {
    Logger.info("Loading configuration from local {}", LocalStorage.getRunConfigurationFile());
    try {
      return localStorage.load(RunConfiguration.class);
    } catch (final Exception ex) {
      final String message = "Local configuration file is damaged";
      Logger.error(message, ex);
      throw new StartupException(message, ex);
    }
  }

  private void loadRunConfigurations() throws StartupException {
    final boolean localConfigurationExist = localStorage.doesConfigurationExist();
    runConfiguration =
        localConfigurationExist ? loadLocalConfigurations() : downloadAndSaveConfigurations();

    // Validate the configuration
    if (runConfiguration == null) {
      throw new StartupException("Configuration is not valid", null);
    }
    if (!runConfiguration.getSchemaVersion().equals(properties.getApplicationVersion())) {
      Logger.warn("Version mismatch between run-configuration and software, expect {} but found {}",
          properties.getApplicationVersion(), runConfiguration.getSchemaVersion());
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
