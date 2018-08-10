/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader.transmission.dto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class EnergyUsageDto {
  private int registerId;
  private int[] energyUsageResponse;

  public EnergyUsageDto() {}

  public EnergyUsageDto(final int registerId, final int[] energyUsageResponse) {
    this.registerId = registerId;
    this.energyUsageResponse = energyUsageResponse;
  }

  public int[] getEnergyUsageResponse() {
    return energyUsageResponse;
  }

  public int getRegisterId() {
    return registerId;
  }

  public void setEnergyUsageResponse(final int[] energyUsageResponse) {
    this.energyUsageResponse = energyUsageResponse;
  }

  public void setRegisterId(final int registerId) {
    this.registerId = registerId;
  }
}
