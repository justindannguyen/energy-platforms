/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energyprocessor.dto.cloud;

import java.util.List;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class RegisterDefinitionDto {
  private int registerId;
  private List<RegisterValueDto> values;

  public int getRegisterId() {
    return registerId;
  }

  public List<RegisterValueDto> getValues() {
    return values;
  }

  public void setRegisterId(final int registerId) {
    this.registerId = registerId;
  }

  public void setValues(final List<RegisterValueDto> values) {
    this.values = values;
  }
}
