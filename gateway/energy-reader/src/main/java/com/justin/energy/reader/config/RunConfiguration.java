/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader.config;

import java.util.List;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class RunConfiguration {
  private int baudRate;
  private int dataBits;
  private String parity;
  private int stopBits;
  private String serialPort;
  private String gatewayId;
  private int energyReportSecondInterval;
  private int deviceStatusReportSecondInterval;
  private int energyReportUploadSecondInterval;
  private String schemaVersion;
  private KafkaConfiguration kafkaConfiguration;
  private List<MeterConfiguration> meterConfigurations;

  public int getBaudRate() {
    return baudRate;
  }

  public int getDataBits() {
    return dataBits;
  }

  public int getDeviceStatusReportSecondInterval() {
    return deviceStatusReportSecondInterval;
  }

  public int getEnergyReportSecondInterval() {
    return energyReportSecondInterval;
  }

  public int getEnergyReportUploadSecondInterval() {
    return energyReportUploadSecondInterval;
  }

  public String getGatewayId() {
    return gatewayId;
  }

  public KafkaConfiguration getKafkaConfiguration() {
    return kafkaConfiguration;
  }

  public List<MeterConfiguration> getMeterConfigurations() {
    return meterConfigurations;
  }

  public String getParity() {
    return parity;
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }

  public String getSerialPort() {
    return serialPort;
  }

  public int getStopBits() {
    return stopBits;
  }

  public void setBaudRate(final int baudRate) {
    this.baudRate = baudRate;
  }

  public void setDataBits(final int dataBits) {
    this.dataBits = dataBits;
  }

  public void setDeviceStatusReportSecondInterval(final int deviceStatusReportSecondInterval) {
    this.deviceStatusReportSecondInterval = deviceStatusReportSecondInterval;
  }

  public void setEnergyReportSecondInterval(final int energyReportSecondInterval) {
    this.energyReportSecondInterval = energyReportSecondInterval;
  }

  public void setEnergyReportUploadSecondInterval(final int energyReportUploadSecondInterval) {
    this.energyReportUploadSecondInterval = energyReportUploadSecondInterval;
  }

  public void setGatewayId(final String gatewayId) {
    this.gatewayId = gatewayId;
  }

  public void setKafkaConfiguration(final KafkaConfiguration kafkaConfiguration) {
    this.kafkaConfiguration = kafkaConfiguration;
  }

  public void setMeterConfigurations(final List<MeterConfiguration> meterConfigurations) {
    this.meterConfigurations = meterConfigurations;
  }

  public void setParity(final String parity) {
    this.parity = parity;
  }

  public void setSchemaVersion(final String schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public void setSerialPort(final String serialPort) {
    this.serialPort = serialPort;
  }

  public void setStopBits(final int stopBits) {
    this.stopBits = stopBits;
  }
}
