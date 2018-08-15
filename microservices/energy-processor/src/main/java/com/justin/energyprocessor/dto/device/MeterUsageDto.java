/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energyprocessor.dto.device;

import java.util.List;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class MeterUsageDto {
  private int meterId;

  private List<RegisterDto> energyUsages;

  public List<RegisterDto> getEnergyUsages() {
    return energyUsages;
  }

  public int getMeterId() {
    return meterId;
  }

  public void setEnergyUsages(final List<RegisterDto> energyUsages) {
    this.energyUsages = energyUsages;
  }

  public void setMeterId(final int meterId) {
    this.meterId = meterId;
  }
}
