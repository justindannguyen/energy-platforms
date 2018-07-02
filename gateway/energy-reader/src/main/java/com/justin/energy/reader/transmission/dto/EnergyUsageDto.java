/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader.transmission.dto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class EnergyUsageDto {
  private int registerId;
  private int meterId;
  private String gatewayId;
  private int[] energyUsageResponse;

  public int[] getEnergyUsageResponse() {
    return energyUsageResponse;
  }

  public String getGatewayId() {
    return gatewayId;
  }

  public int getMeterId() {
    return meterId;
  }

  public int getRegisterId() {
    return registerId;
  }

  public EnergyUsageDto setEnergyUsageResponse(final int[] energyUsageResponse) {
    this.energyUsageResponse = energyUsageResponse;
    return this;
  }

  public EnergyUsageDto setGatewayId(final String gatewayId) {
    this.gatewayId = gatewayId;
    return this;
  }

  public EnergyUsageDto setMeterId(final int meterId) {
    this.meterId = meterId;
    return this;
  }

  public EnergyUsageDto setRegisterId(final int registerId) {
    this.registerId = registerId;
    return this;
  }
}
