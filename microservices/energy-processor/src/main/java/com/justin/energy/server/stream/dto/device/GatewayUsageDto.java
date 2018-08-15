/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.dto.device;

import java.util.List;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class GatewayUsageDto {
  private String gatewayId;

  private List<MeterUsageDto> meterUsages;

  public String getGatewayId() {
    return gatewayId;
  }

  public List<MeterUsageDto> getMeterUsages() {
    return meterUsages;
  }

  public void setGatewayId(final String gatewayId) {
    this.gatewayId = gatewayId;
  }

  public void setMeterUsages(final List<MeterUsageDto> meterUsages) {
    this.meterUsages = meterUsages;
  }

}
