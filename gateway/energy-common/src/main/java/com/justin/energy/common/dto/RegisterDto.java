/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common.dto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class RegisterDto {
  private int registerId;
  private int[] registerValues;

  public RegisterDto() {}

  public RegisterDto(final int registerId, final int[] energyUsageResponse) {
    this.registerId = registerId;
    this.registerValues = energyUsageResponse;
  }

  public int getRegisterId() {
    return registerId;
  }

  public int[] getRegisterValues() {
    return registerValues;
  }

  public void setRegisterId(final int registerId) {
    this.registerId = registerId;
  }

  public void setRegisterValues(final int[] registerValues) {
    this.registerValues = registerValues;
  }
}
