/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common.dto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class RegistryDto {
  private int registerId;
  private int[] registryValues;

  public RegistryDto() {}

  public RegistryDto(final int registerId, final int[] energyUsageResponse) {
    this.registerId = registerId;
    this.registryValues = energyUsageResponse;
  }

  public int[] getEnergyUsageResponse() {
    return registryValues;
  }

  public int getRegisterId() {
    return registerId;
  }

  public void setEnergyUsageResponse(final int[] energyUsageResponse) {
    this.registryValues = energyUsageResponse;
  }

  public void setRegisterId(final int registerId) {
    this.registerId = registerId;
  }
}
