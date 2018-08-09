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
  private int energyReportInterval;
  private int deviceStatusReportInterval;
  private String schemaVersion;
  private KafkaConfiguration kafkaConfiguration;
  private List<MeterConfiguration> meterConfigurations;

  public int getBaudRate() {
    return baudRate;
  }

  public int getDataBits() {
    return dataBits;
  }

  public int getDeviceStatusReportInterval() {
    return deviceStatusReportInterval;
  }

  public int getEnergyReportInterval() {
    return energyReportInterval;
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

  public void setDeviceStatusReportInterval(final int deviceStatusReportInterval) {
    this.deviceStatusReportInterval = deviceStatusReportInterval;
  }

  public void setEnergyReportInterval(final int energyReportInterval) {
    this.energyReportInterval = energyReportInterval;
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
