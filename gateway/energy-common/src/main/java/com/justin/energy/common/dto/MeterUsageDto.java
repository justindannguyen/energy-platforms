/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common.dto;

import java.util.List;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class MeterUsageDto {
  private int meterId;

  private List<RegistryDto> energyUsages;

  public List<RegistryDto> getEnergyUsages() {
    return energyUsages;
  }

  public int getMeterId() {
    return meterId;
  }

  public void setEnergyUsages(final List<RegistryDto> energyUsages) {
    this.energyUsages = energyUsages;
  }

  public void setMeterId(final int meterId) {
    this.meterId = meterId;
  }
}
