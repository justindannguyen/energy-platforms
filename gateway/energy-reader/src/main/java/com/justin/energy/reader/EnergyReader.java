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

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import com.justin.energy.common.config.LocalStorage;
import com.justin.energy.common.dto.DeviceStatusDto;
import com.justin.energy.common.dto.GatewayUsageDto;
import com.justin.energy.common.dto.MeterUsageDto;
import com.justin.energy.common.dto.RegisterDto;
import com.justin.energy.common.exception.ShutdownException;
import com.justin.energy.common.exception.StartupException;
import com.justin.energy.reader.config.ApplicationProperties;
import com.justin.energy.reader.config.KafkaConfiguration;
import com.justin.energy.reader.config.MeterConfiguration;
import com.justin.energy.reader.config.MeterConfiguration.HoldingRegister;
import com.justin.energy.reader.config.RunConfiguration;
import com.justin.energy.reader.transmission.KafkaClient;
import com.justin.energy.reader.transmission.MeterModbusMaster;

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
      final GatewayUsageDto gatewayUsage = readFromMeters();

      // Convert energy sample to JSON then share to cloud
      final boolean uploadSuccess = sendToServer(gatewayUsage);

      // Backup the items which can't transfer to server due to network issues
      if (!uploadSuccess) {
        backupReadingLocally(gatewayUsage);
      } else {
        // If previous items can be sent to server then try to send backup items as well.
        sendBackupReading();
      }
    }

    private void backupReadingLocally(final GatewayUsageDto gatewayUsage) {
      try {
        LocalStorage.storeEnergyData(gatewayUsage);
        Logger.info("Backup the reading data due-to network issues");
      } catch (final Exception ex) {
        Logger.error(ex, "Can't backup the reading data");
      }
    }

    private MeterUsageDto readFromMeter(final MeterConfiguration config) {
      final int meterId = config.getMeterId();
      final Stream<RegisterDto> readingFromRegisters = config.getEnergyReportRegisters().stream()
          .map(register -> readFromRegister(register, meterId)).filter(report -> report != null);

      final MeterUsageDto meterUsage = new MeterUsageDto();
      meterUsage.setMeterId(meterId);
      meterUsage.setEnergyUsages(readingFromRegisters.collect(Collectors.toList()));
      return meterUsage;
    }

    private GatewayUsageDto readFromMeters() {
      final Stream<MeterUsageDto> meterUsages =
          runConfiguration.getMeterConfigurations().stream().map(this::readFromMeter);
      final GatewayUsageDto gatewayUsage = new GatewayUsageDto();
      gatewayUsage.setGatewayId(gatewayId);
      gatewayUsage.setMeterUsages(meterUsages.collect(Collectors.toList()));
      return gatewayUsage;
    }

    private RegisterDto readFromRegister(final HoldingRegister register, final int meterId) {
      final int[] registerData = meterModbusMaster.readHoldingRegister(meterId, register);
      return registerData == null ? null
          : new RegisterDto(register.getRegisterId(), registerData);
    }

    private void sendBackupReading() {
      final File energyDataRoot = LocalStorage.getEnergyDataRoot();
      final File[] backupFiles = energyDataRoot.listFiles();
      if (backupFiles.length == 0) {
        return;
      }
      Logger.info("Found {} backup samples, try to upload backup data...", backupFiles.length);
      for (final File energyFile : backupFiles) {
        try {
          final GatewayUsageDto localEnergy =
              LocalStorage.loadEnergyData(energyFile, GatewayUsageDto.class);

          final boolean uploadSuccess = sendToServer(localEnergy);
          if (uploadSuccess) {
            energyFile.delete();
          }
        } catch (final Exception ex) {
          Logger.error(ex, "Can't upload backup data");
          break;
        }
      }
    }

    /**
     * Upload energy usages to cloud and return the result. <code>true</code> mean successful.
     */
    private boolean sendToServer(final GatewayUsageDto gatewayUsage) {
      final String topic = properties.getEnergyReportTopic();
      if (kafkaClient.publish(topic, gatewayId, gatewayUsage)) {
        Logger.info("Energy reading (gateway {}, samples count {}) are uploaded to cloud",
            gatewayUsage.getGatewayId(), gatewayUsage.getMeterUsages().size());
        return true;
      }
      return false;
    }
  }

  private static final int initialReportDelay = 2;
  private static final int initialDeviceStatusDelay = 60;

  public static void main(final String[] args) {
    try {
      new EnergyReader().start();
    } catch (final StartupException ex) {
      Logger.error(ex, "Could not start the energy reader");
    }
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
    final int applicationLockPort = properties.getApplicationLockPort();
    try {
      applicationLock = new ServerSocket(applicationLockPort);
      final Thread shutdownThread = new Thread(this);
      shutdownThread.setName("Shutdown Listener");
      shutdownThread.setDaemon(true);
      shutdownThread.start();
      Logger.info("Application is now running at {}", applicationLockPort);
    } catch (final IOException ex) {
      throw new StartupException(
          String.format("Application is already running at port %s", applicationLockPort), ex);
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
        Logger.info("Receive directive from bootloader");
        try (BufferedReader br =
            new BufferedReader(new InputStreamReader(bootloader.getInputStream()))) {
          final String shutdownPassword = br.readLine();
          shutdown = getGatewayId().equals(shutdownPassword);
          if (!shutdown) {
            Logger.warn("Receive shutdown request {}, but with incorrect data {}", shutdownPassword,
                getGatewayId());
          }
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
        runConfiguration.getEnergyReportSecondInterval(), TimeUnit.SECONDS);
    mainExecutor.scheduleWithFixedDelay(() -> reportDeviceStatus(), initialDeviceStatusDelay,
        runConfiguration.getDeviceStatusReportSecondInterval(), TimeUnit.SECONDS);
  }

  public void stop() throws ShutdownException {
    boolean error = false;
    // Shutdown the tasks and wait for uncompleted tasks
    mainExecutor.shutdown();
    try {
      mainExecutor.awaitTermination(30, TimeUnit.SECONDS);
    } catch (final InterruptedException ex) {
      error = true;
      Logger.error(ex);
    }

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
    // Release application lock
    try {
      applicationLock.close();
    } catch (final IOException ex) {
      error = true;
      Logger.error(ex);
    }

    // if tinylog.writingthread = true then we have to shutdown it manually
    Configurator.shutdownWritingThread(true);
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
      final File configFile = LocalStorage.storeConfigs(config);
      Logger.info("Stored configuration file at {}", configFile.getAbsolutePath());
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
    if (kafkaClient.publish(topic, gatewayId, status)) {
      Logger.info("Device status are sent to cloud");
    }
  }
}
