/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energyprocessor.dto.cloud;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class RegisterValueDto {
  private String dataType;
  private String symbol;

  public RegisterValueDto() {}

  public RegisterValueDto(final String dataType, final String symbol) {
    this.dataType = dataType;
    this.symbol = symbol;
  }

  public String getDataType() {
    return dataType;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setDataType(final String dataType) {
    this.dataType = dataType;
  }

  public void setSymbol(final String symbol) {
    this.symbol = symbol;
  }
}
