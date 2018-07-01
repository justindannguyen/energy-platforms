/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.transmission.dto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class DeviceStatusDto {
  private String gatewayId;
  private int flashUsage;
  private int sdCardUsage;
  private String lastGatewayError;
  private String lastModbusError;
  private String lastMqttError;

  public int getFlashUsage() {
    return flashUsage;
  }

  public String getGatewayId() {
    return gatewayId;
  }

  public String getLastGatewayError() {
    return lastGatewayError;
  }

  public String getLastModbusError() {
    return lastModbusError;
  }

  public String getLastMqttError() {
    return lastMqttError;
  }

  public int getSdCardUsage() {
    return sdCardUsage;
  }

  public void setFlashUsage(final int flashUsage) {
    this.flashUsage = flashUsage;
  }

  public void setGatewayId(final String gatewayId) {
    this.gatewayId = gatewayId;
  }

  public void setLastGatewayError(final String lastGatewayError) {
    this.lastGatewayError = lastGatewayError;
  }

  public void setLastModbusError(final String lastModbusError) {
    this.lastModbusError = lastModbusError;
  }

  public void setLastMqttError(final String lastMqttError) {
    this.lastMqttError = lastMqttError;
  }

  public void setSdCardUsage(final int sdCardUsage) {
    this.sdCardUsage = sdCardUsage;
  }
}
