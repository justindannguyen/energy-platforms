/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pmw.tinylog.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import com.justin.energy.common.config.LocalStorage;
import com.justin.energy.common.exception.ShutdownException;
import com.justin.energy.common.exception.StartupException;
import com.justin.energy.reader.config.ApplicationProperties;
import com.justin.energy.reader.config.KafkaConfiguration;
import com.justin.energy.reader.config.MeterConfiguration;
import com.justin.energy.reader.config.RunConfiguration;
import com.justin.energy.reader.transmission.KafkaClient;
import com.justin.energy.reader.transmission.MeterModbusMaster;
import com.justin.energy.reader.transmission.dto.DeviceStatusDto;
import com.justin.energy.reader.transmission.dto.EnergyUsageDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class EnergyReader implements Runnable {
  private class EnergyWorker implements Runnable {
    @Override
    public void run() {
      // Skip execution if no meters configured/connect to modbus
      final List<MeterConfiguration> meterConfigurations =
          runConfiguration.getMeterConfigurations();
      if (meterConfigurations.isEmpty()) {
        return;
      }

      // Read the energy sample from modbus, skip error to read till last meters
      final Stream<EnergyUsageDto> energyReportStream = readFromMeters();

      // Convert energy sample to JSON then share to cloud, only error items are retained
      final Stream<EnergyUsageDto> errorStream = sendToServer(energyReportStream);

      // Backup the items which can't transfer to server due to network issues
      final List<EnergyUsageDto> errorItems = errorStream.collect(Collectors.toList());
      if (!errorItems.isEmpty()) {
        storeErrorData(errorItems);
      } else {
        // If previous items can be sent to server then try to send backup items as well.
        sendBackupReading();
      }
    }

    private Stream<EnergyUsageDto> readFromMeters() {
      return runConfiguration.getMeterConfigurations().stream().flatMap(config -> {
        return config.getEnergyReportRegisters().stream().map(register -> {
          final int meterId = config.getMeterId();
          final int[] registerData = meterModbusMaster.readHoldingRegister(meterId, register);
          if (registerData != null) {
            return new EnergyUsageDto().setEnergyUsageResponse(registerData).setGatewayId(gatewayId)
                .setMeterId(meterId).setRegisterId(register.getRegisterId());
          }
          return null;
        });
      }).filter(report -> report != null);
    }

    private void sendBackupReading() {
      final File energyDataRoot = LocalStorage.getEnergyDataRoot();
      for (final File energyFile : energyDataRoot.listFiles()) {
        try {
          Logger.info("Uploading backup data...");
          final List<EnergyUsageDto> localEnergy =
              LocalStorage.loadEnergyData(energyFile, EnergyUsageDto.class);
          energyFile.delete();

          final Stream<EnergyUsageDto> errorStream = sendToServer(localEnergy.stream());
          final List<EnergyUsageDto> errorItems = errorStream.collect(Collectors.toList());
          if (!errorItems.isEmpty()) {
            storeErrorData(errorItems);
            break;
          }
        } catch (final Exception ex) {
          Logger.error(ex, "Can't send backup data");
          break;
        }
      }
    }

    private Stream<EnergyUsageDto> sendToServer(final Stream<EnergyUsageDto> reportStream) {
      return reportStream.filter(report -> {
        final String topic = properties.getEnergyReportTopic();
        if (report != null && kafkaClient.publish(topic, report)) {
          Logger.info("Energy report (meter {}) sample are uploaded to cloud", report.getMeterId());
          return false;
        }
        return true;
      });
    }

    private void storeErrorData(final List<EnergyUsageDto> errors) {
      try {
        LocalStorage.storeEnergyData(errors);
        Logger.info("Backup reading data due to network issues");
      } catch (final Exception ex) {
        Logger.error(ex, "Can't backup the energy data");
      }
    }
  }

  private static final int initialReportDelay = 20;
  private static final int initialDeviceStatusDelay = 60;

  public static void main(final String[] args) throws StartupException {
    new EnergyReader().start();
  }

  private final ScheduledExecutorService mainExecutor = Executors.newScheduledThreadPool(1);
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ApplicationProperties properties = new ApplicationProperties();
  private final String gatewayId = ApplicationProperties.getConfiguredDeviceId();
  private KafkaClient kafkaClient;
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
      final Thread shutdownThread = new Thread(this);
      shutdownThread.setName("Shutdown Listener");
      shutdownThread.setDaemon(true);
      shutdownThread.start();
    } catch (final IOException ex) {
      throw new StartupException("Application is already running");
    }
  }

  public String getGatewayId() {
    return gatewayId;
  }

  @Override
  public void run() {
    // Wait on shutdown port
    try {
      boolean shutdown = false;
      do {
        final Socket bootloader = applicationLock.accept();
        try (BufferedReader br =
            new BufferedReader(new InputStreamReader(bootloader.getInputStream()))) {
          final String shutdownPassword = br.readLine();
          shutdown = getGatewayId().equals(shutdownPassword);
        }
      } while (shutdown == false);
    } catch (final IOException ex) {
      Logger.error(ex, "Could not start shutdown thread");
    }

    Logger.info("Receive shutdown request from boot loader, terminating");
    try {
      stop();
    } catch (final ShutdownException ex) {
      Logger.error(ex, "Errors encountered during shutdown process, force stop");
    }
    System.exit(0);
  }

  public void start() throws StartupException {
    loadRunConfigurations();
    connectWithBroker();
    connectWithMeters();
    mainExecutor.scheduleWithFixedDelay(new EnergyWorker(), initialReportDelay,
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
      kafkaClient.disconnect();
    } catch (final InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }
    // Shutdown the tasks and wait for uncompleted tasks
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
    final KafkaConfiguration kafkaConfiguration = runConfiguration.getKafkaConfiguration();
    kafkaClient = new KafkaClient(kafkaConfiguration,
        String.format("%s-%s", getClass().getSimpleName(), gatewayId));
    Logger.info("Connected to broker: " + kafkaConfiguration.getBootstrapServers());
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
      LocalStorage.storeConfigs(config);
      return config;
    } catch (final Exception ex) {
      throw new StartupException("Could not download the run configuration from cloud", ex);
    }
  }

  private RunConfiguration loadLocalConfigurations() throws StartupException {
    Logger.info("Loading configuration from local {}",
        LocalStorage.getRunReaderConfigurationFile());
    try {
      return LocalStorage.loadConfigs(RunConfiguration.class);
    } catch (final Exception ex) {
      final String message = "Local configuration file is damaged";
      Logger.error(message, ex);
      throw new StartupException(message, ex);
    }
  }

  private void loadRunConfigurations() throws StartupException {
    final boolean localConfigurationExist = LocalStorage.doesRunReaderConfigurationExist();
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
    if (meterModbusMaster.getLastError() != null) {
      status.setLastModbusError(meterModbusMaster.getLastError().getMessage());
    }
    if (kafkaClient.getCurrentException() != null) {
      status.setLastKafkaError(kafkaClient.getCurrentException().getMessage());
    }
    final String topic = properties.getDeviceStatusTopic();
    if (kafkaClient.publish(topic, status)) {
      Logger.info("Device status are sent to cloud");
    }
  }
}
