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

  private List<EnergyUsageDto> energyUsages;

  public List<EnergyUsageDto> getEnergyUsages() {
    return energyUsages;
  }

  public int getMeterId() {
    return meterId;
  }

  public void setEnergyUsages(final List<EnergyUsageDto> energyUsages) {
    this.energyUsages = energyUsages;
  }

  public void setMeterId(final int meterId) {
    this.meterId = meterId;
  }
}
