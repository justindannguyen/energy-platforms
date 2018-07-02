/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader.config;

import java.util.List;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class MeterConfiguration {
  public static class HoldingRegister {
    private int offset;
    private int quantity;
    private int registerId;

    public int getOffset() {
      return offset;
    }
    public int getQuantity() {
      return quantity;
    }
    public int getRegisterId() {
      return registerId;
    }
    public void setOffset(final int offset) {
      this.offset = offset;
    }
    public void setQuantity(final int quantity) {
      this.quantity = quantity;
    }
    public void setRegisterId(final int registerId) {
      this.registerId = registerId;
    }
  }

  private int meterId;
  private String meterName;
  private List<HoldingRegister> energyReportRegisters;
  private List<HoldingRegister> deviceStatusReportRegisters;

  public List<HoldingRegister> getDeviceStatusReportRegisters() {
    return deviceStatusReportRegisters;
  }
  public List<HoldingRegister> getEnergyReportRegisters() {
    return energyReportRegisters;
  }

  public int getMeterId() {
    return meterId;
  }

  public String getMeterName() {
    return meterName;
  }

  public void setDeviceStatusReportRegisters(final List<HoldingRegister> deviceStatusReportRegisters) {
    this.deviceStatusReportRegisters = deviceStatusReportRegisters;
  }

  public void setEnergyReportRegisters(final List<HoldingRegister> energyReportRegisters) {
    this.energyReportRegisters = energyReportRegisters;
  }

  public void setMeterId(final int meterId) {
    this.meterId = meterId;
  }

  public void setMeterName(final String meterName) {
    this.meterName = meterName;
  }
}
